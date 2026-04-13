/**
 * Super Admin only — manage Firestore user profiles: role, terminal permissions, add/remove.
 * New Auth users: secondary Firebase app + setup email (auth-action.html). Remove = Firestore only (see toast).
 */

const ACCESS_PERMISSION_DEFS = [
  { key: 'overview', label: 'Overview' },
  { key: 'users', label: 'User Directory' },
  { key: 'moderation', label: 'Content Quality' },
  { key: 'ai', label: 'Gemini AI' },
  { key: 'gamify', label: 'Gamification' },
  { key: 'reports', label: 'Reports & Feedback' },
  { key: 'security', label: 'System Logs' },
  { key: 'settings', label: 'Settings' },
];

const ACCESS_ROLE_OPTIONS = [
  'Support Admin',
  'Content Moderator',
  'Super Admin',
];

/** Profiles listed in Access Control: terminal staff only (not end-user accounts). */
function isAccessControlStaffRole(role) {
  const r = String(role ?? '').trim();
  if (!r) return false;
  if (/^user$/i.test(r)) return false;
  return /(super\s*)?admin|moderator/i.test(r);
}

function mergeDefaultPermissions(raw) {
  const out = {};
  ACCESS_PERMISSION_DEFS.forEach(({ key }) => {
    out[key] = raw && raw[key] === false ? false : true;
  });
  return out;
}

function provisioningPassword() {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*-_';
  const arr = new Uint8Array(32);
  crypto.getRandomValues(arr);
  let s = '';
  for (let i = 0; i < 32; i++) s += chars[arr[i] % chars.length];
  return s;
}

function getProvisionSecondaryApp() {
  try {
    return firebase.initializeApp(firebase.app().options, 'fwProvisionSecondary');
  } catch (e) {
    return firebase.app('fwProvisionSecondary');
  }
}

function openAccessControlModal(html) {
  const body = document.getElementById('access-control-modal-body');
  const shell = document.getElementById('access-control-modal');
  body.innerHTML = html;
  shell.style.display = 'flex';
}

window.closeAccessControlModal = function closeAccessControlModal() {
  document.getElementById('access-control-modal').style.display = 'none';
  document.getElementById('access-control-modal-body').innerHTML = '';
};

function permissionCheckboxesHtml(permsObj) {
  const merged = mergeDefaultPermissions(permsObj);
  return ACCESS_PERMISSION_DEFS.map(({ key, label }) => `
    <label class="d-flex align-items-center gap-2 mb-2 small">
      <input type="checkbox" class="form-check-input m-0" data-perm-key="${esc(key)}"
        ${merged[key] ? 'checked' : ''} />
      <span>${esc(label)}</span>
    </label>
  `).join('');
}

function readPermissionsFromModal(container) {
  const o = {};
  ACCESS_PERMISSION_DEFS.forEach(({ key }) => { o[key] = true; });
  container.querySelectorAll('[data-perm-key]').forEach(chk => {
    o[chk.dataset.permKey] = chk.checked;
  });
  return o;
}

const ACCESS_COUNTDOWN_SECONDS = 5;

/**
 * Modal: wait ACCESS_COUNTDOWN_SECONDS, then enable Confirm. Resolve on Confirm, reject on Cancel.
 * @param {string} detailHtml — safe HTML (use esc() for user strings).
 */
function confirmAccessAction(detailHtml) {
  return new Promise((resolve, reject) => {
    const backdrop = document.createElement('div');
    backdrop.id = 'access-countdown-confirm';
    backdrop.className = 'fw-modal-backdrop';
    backdrop.style.cssText = 'display:flex;z-index:10050;';
    backdrop.setAttribute('role', 'dialog');
    backdrop.setAttribute('aria-modal', 'true');
    backdrop.innerHTML = `
      <div class="fw-modal animate-modal" style="max-width:420px;">
        <h4 class="fw-bold mb-2" style="color:var(--ink);font-size:1.1rem;">Confirm action</h4>
        <div class="text-muted small mb-3">${detailHtml}</div>
        <p class="mb-1 fw-semibold text-center" style="color:var(--grape);font-size:2.25rem;font-family:var(--font-mono,monospace);letter-spacing:.05em;"
           id="access-cc-display" aria-live="polite">${ACCESS_COUNTDOWN_SECONDS}</p>
        <p class="text-muted small text-center mb-4">You can confirm after the countdown.</p>
        <div class="d-flex justify-content-end gap-2">
          <button type="button" class="btn btn-ghost px-4 py-2" id="access-cc-cancel">Cancel</button>
          <button type="button" class="btn btn-grape px-4 py-2 fw-semibold" id="access-cc-ok" disabled>Confirm</button>
        </div>
      </div>`;
    document.body.appendChild(backdrop);

    const display = backdrop.querySelector('#access-cc-display');
    const btnOk = backdrop.querySelector('#access-cc-ok');
    let left = ACCESS_COUNTDOWN_SECONDS;
    let iv = setInterval(() => {
      left--;
      if (left <= 0) {
        clearInterval(iv);
        iv = null;
        display.textContent = '✓';
        btnOk.disabled = false;
        return;
      }
      display.textContent = String(left);
    }, 1000);

    function teardown() {
      if (iv != null) clearInterval(iv);
      backdrop.remove();
    }

    function onCancel() {
      teardown();
      reject(new Error('cancel'));
    }

    backdrop.querySelector('#access-cc-cancel').addEventListener('click', onCancel);
    backdrop.addEventListener('click', e => {
      if (e.target === backdrop) onCancel();
    });
    btnOk.addEventListener('click', () => {
      teardown();
      resolve();
    });
  });
}

function renderAccessControl(container) {
  if (!isSuperAdmin()) {
    container.innerHTML = `
      <div class="fw-card">
        <p class="text-danger mb-0 fw-semibold">Access Control is restricted to Super Admins.</p>
      </div>`;
    return;
  }

  let allUsers = [];

  function superAdminCount() {
    return allUsers.filter(u => String(u.role || '').trim() === 'Super Admin').length;
  }

  function renderTable() {
    const rows = allUsers.filter(u => isAccessControlStaffRole(u.role));
    document.getElementById('access-users-body').innerHTML =
      rows.length === 0
        ? `<tr><td colspan="5" class="text-center text-muted py-4">No admin or moderator profiles match this list.</td></tr>`
        : rows
            .map(u => {
              const p = mergeDefaultPermissions(u.permissions);
              const enabled = ACCESS_PERMISSION_DEFS.filter(({ key }) => p[key]).length;
              return `
          <tr>
            <td>
              <div class="fw-semibold" style="color:var(--ink);">${esc(u.name || '—')}</div>
              <div class="text-muted small">${esc(u.email || '')}</div>
            </td>
            <td><span class="fw-badge badge-ai">${esc(u.role || '—')}</span></td>
            <td class="text-muted small">${esc(u.status || '—')}</td>
            <td class="text-muted small">${enabled}/${ACCESS_PERMISSION_DEFS.length} sections</td>
            <td class="text-end">
              <button type="button" class="btn btn-link fw-semibold p-0 me-2" style="color:var(--grape);font-size:.85rem;"
                data-ac-edit="${esc(u.id)}">Edit</button>
              <button type="button" class="btn btn-link text-danger fw-semibold p-0" style="font-size:.85rem;"
                data-ac-remove="${esc(u.id)}">Remove</button>
            </td>
          </tr>`;
            })
            .join('');
  }

  container.innerHTML = `
  <div class="page-header mb-4">
    <h2>Access Control</h2>
    <p class="text-muted mb-0">Super Admin only — manage <strong>admin, super admin, and moderator</strong> profiles (terminal access). End-user accounts are not listed here.</p>
  </div>
  <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-3">
    <p class="text-muted small mb-0">Removing a row deletes that staff member's Firestore profile only. Disable sign-in in Firebase Authentication if needed.</p>
    <button type="button" class="btn btn-grape px-4 py-2 fw-semibold" id="access-btn-add">
      <i class="bi bi-person-plus me-1"></i>Add user
    </button>
  </div>
  <div class="fw-card p-0 overflow-hidden">
    <div class="table-responsive">
      <table class="fw-table">
        <thead><tr>
          <th>Staff</th><th>Role</th><th>Status</th><th>Permissions</th><th class="text-end">Actions</th>
        </tr></thead>
        <tbody id="access-users-body"></tbody>
      </table>
    </div>
  </div>`;

  const unsub = db
    .collection('users')
    .orderBy('joined', 'desc')
    .onSnapshot(snap => {
      allUsers = snap.docs.map(d => ({ id: d.id, ...d.data() }));
      renderTable();
    }, err => console.error(err));
  pageCleanups.push(unsub);

  function openEditModal(u) {
    const perms = mergeDefaultPermissions(u.permissions);
    const roleOpts = ACCESS_ROLE_OPTIONS.map(r => `<option value="${esc(r)}" ${u.role === r ? 'selected' : ''}>${esc(r)}</option>`).join('');
    openAccessControlModal(`
      <div class="d-flex justify-content-between align-items-start mb-4 gap-3">
        <div>
          <h3 class="fw-bold mb-1" style="color:var(--ink);font-size:1.2rem;">Edit user</h3>
          <p class="text-muted small mb-0">${esc(u.email || '')}</p>
        </div>
        <button type="button" class="btn btn-link text-muted p-0" onclick="closeAccessControlModal()"><i class="bi bi-x-lg fs-5"></i></button>
      </div>
      <div class="mb-3">
        <label class="fw-label">Display name</label>
        <input type="text" id="ac-edit-name" class="fw-input" maxlength="80" value="${esc(u.name || '')}" />
      </div>
      <div class="mb-3">
        <label class="fw-label">Role</label>
        <select id="ac-edit-role" class="fw-select">${roleOpts}</select>
      </div>
      <div class="mb-3">
        <label class="fw-label">Account status</label>
        <select id="ac-edit-status" class="fw-select">
          ${['Active', 'Suspended', 'Banned'].map(s => `<option value="${esc(s)}" ${u.status === s ? 'selected' : ''}>${esc(s)}</option>`).join('')}
        </select>
      </div>
      <div class="mb-4">
        <div class="fw-label mb-2">Terminal sections</div>
        <div class="p-3 rounded-3" style="background:var(--surface);border:1px solid var(--border);max-height:220px;overflow-y:auto;">
          ${permissionCheckboxesHtml(perms)}
        </div>
      </div>
      <div class="d-flex justify-content-end gap-2">
        <button type="button" class="btn btn-ghost px-4 py-2" onclick="closeAccessControlModal()">Cancel</button>
        <button type="button" class="btn btn-grape px-4 py-2 fw-semibold" id="ac-edit-save">Save changes</button>
      </div>`);

    document.getElementById('ac-edit-save').onclick = async () => {
      const name = document.getElementById('ac-edit-name').value.trim() || u.email;
      const role = document.getElementById('ac-edit-role').value;
      const status = document.getElementById('ac-edit-status').value;
      const modalRoot = document.getElementById('access-control-modal-body');
      const permissions = readPermissionsFromModal(modalRoot);

      const wasSuper = String(u.role || '').trim() === 'Super Admin';
      const nowSuper = String(role).trim() === 'Super Admin';
      if (wasSuper && !nowSuper && superAdminCount() <= 1) {
        showToast('Keep at least one Super Admin.', 'danger');
        return;
      }

      try {
        await confirmAccessAction(
          `<p class="mb-0">Apply changes to <span class="fw-semibold" style="color:var(--ink);">${esc(u.email || '')}</span>?</p>`
        );
      } catch {
        return;
      }

      try {
        await db
          .collection('users')
          .doc(u.id)
          .update({ name, role, status, permissions, updatedAt: firebase.firestore.FieldValue.serverTimestamp() });
        await logAudit('ACCESS_USER_UPDATED', { targetUid: u.id, email: u.email, role, status });
        showToast('User updated.');
        closeAccessControlModal();
        if (u.id === auth.currentUser?.uid) await window.refreshSessionProfile();
      } catch (err) {
        showToast(err.message || 'Update failed.', 'danger');
      }
    };
  }

  function openAddModal() {
    openAccessControlModal(`
      <div class="d-flex justify-content-between align-items-start mb-4 gap-3">
        <div>
          <h3 class="fw-bold mb-1" style="color:var(--ink);font-size:1.2rem;">Add user</h3>
          <p class="text-muted small mb-0">Creates Firebase Authentication sign-in and a Firestore profile. An email is sent to set their password.</p>
        </div>
        <button type="button" class="btn btn-link text-muted p-0" onclick="closeAccessControlModal()"><i class="bi bi-x-lg fs-5"></i></button>
      </div>
      <div class="mb-3">
        <label class="fw-label">Email</label>
        <input type="email" id="ac-add-email" class="fw-input" maxlength="254" autocomplete="off" required />
      </div>
      <div class="mb-3">
        <label class="fw-label">Display name</label>
        <input type="text" id="ac-add-name" class="fw-input" maxlength="80" autocomplete="off" />
      </div>
      <div class="mb-3">
        <label class="fw-label">Role</label>
        <select id="ac-add-role" class="fw-select">
          ${ACCESS_ROLE_OPTIONS.map(r => `<option value="${esc(r)}">${esc(r)}</option>`).join('')}
        </select>
      </div>
      <div class="mb-3">
        <label class="fw-label">Account status</label>
        <select id="ac-add-status" class="fw-select">
          <option value="Active">Active</option>
          <option value="Suspended">Suspended</option>
          <option value="Banned">Banned</option>
        </select>
      </div>
      <div class="mb-4">
        <div class="fw-label mb-2">Terminal sections</div>
        <div class="p-3 rounded-3" style="background:var(--surface);border:1px solid var(--border);max-height:220px;overflow-y:auto;">
          ${permissionCheckboxesHtml({})}
        </div>
      </div>
      <div class="d-flex justify-content-end gap-2">
        <button type="button" class="btn btn-ghost px-4 py-2" onclick="closeAccessControlModal()">Cancel</button>
        <button type="button" class="btn btn-grape px-4 py-2 fw-semibold" id="ac-add-save">Create &amp; send setup email</button>
      </div>`);

    document.getElementById('ac-add-save').onclick = async () => {
      const email = document.getElementById('ac-add-email').value.trim().toLowerCase();
      const name = document.getElementById('ac-add-name').value.trim() || email;
      const role = document.getElementById('ac-add-role').value;
      const status = document.getElementById('ac-add-status').value;
      const modalRoot = document.getElementById('access-control-modal-body');
      const permissions = readPermissionsFromModal(modalRoot);

      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        showToast('Enter a valid email.', 'warning');
        return;
      }
      if (!/(admin|moderator)/i.test(role)) {
        showToast('Terminal users need a role that includes Admin or Moderator.', 'warning');
        return;
      }

      try {
        await confirmAccessAction(
          `<p class="mb-0">Create account for <span class="fw-semibold" style="color:var(--ink);">${esc(email)}</span> and send the setup email?</p>`
        );
      } catch {
        return;
      }

      const pw = provisioningPassword();
      const secondaryApp = getProvisionSecondaryApp();
      const sAuth = secondaryApp.auth();
      const sDb = secondaryApp.firestore();
      const setupUrl = new URL('auth-action.html', window.location.href).href;

      try {
        const { user } = await sAuth.createUserWithEmailAndPassword(email, pw);
        const newUid = user.uid;
        await sDb
          .collection('users')
          .doc(newUid)
          .set({
            name,
            email,
            role,
            status,
            emailVerified: false,
            setupPending: true,
            permissions,
            joined: new Date().toISOString().slice(0, 10),
            lastActive: 'Just now',
            decks: 0,
            sessions: 0,
            aiGenerations: 0,
            createdAt: firebase.firestore.FieldValue.serverTimestamp(),
          });
        try {
          await sAuth.sendPasswordResetEmail(email, { url: setupUrl, handleCodeInApp: false });
        } catch (e) {
          console.warn(e);
        }
        await sAuth.signOut();
        await logAudit('ACCESS_USER_CREATED', { email, role });
        showToast(`Created ${email}. They must finish setup from the email before signing in.`);
        closeAccessControlModal();
      } catch (err) {
        const map = {
          'auth/email-already-in-use': 'That email already has an account.',
          'auth/invalid-email': 'Invalid email address.',
          'auth/weak-password': 'Internal password error — try again.',
        };
        showToast(map[err.code] || err.message || 'Create failed.', 'danger');
        try {
          await sAuth.signOut();
        } catch (_) {}
      }
    };
  }

  async function onAccessPageClick(e) {
    const editBtn = e.target.closest('[data-ac-edit]');
    const remBtn = e.target.closest('[data-ac-remove]');
    if (!editBtn && !remBtn) return;
    if (!e.target.closest('#access-users-body')) return;

    if (editBtn) {
      const u = allUsers.find(x => x.id === editBtn.dataset.acEdit);
      if (u) openEditModal(u);
      return;
    }
    if (remBtn) {
      const uid = remBtn.dataset.acRemove;
      const u = allUsers.find(x => x.id === uid);
      if (!u) return;
      if (uid === auth.currentUser?.uid) {
        showToast('You cannot remove your own account.', 'danger');
        return;
      }
      if (String(u.role || '').trim() === 'Super Admin' && superAdminCount() <= 1) {
        showToast('You cannot remove the only Super Admin.', 'danger');
        return;
      }
      try {
        await confirmAccessAction(
          `<p class="mb-0">Remove Firestore profile for <span class="fw-semibold" style="color:var(--ink);">${esc(u.name || u.email || '')}</span>? This does not delete Firebase Authentication.</p>`
        );
      } catch {
        return;
      }
      try {
        await db.collection('users').doc(uid).delete();
        await logAudit('ACCESS_USER_REMOVED', { targetUid: uid, email: u.email });
        showToast('User profile removed.', 'warning');
      } catch (err) {
        showToast(err.message || 'Remove failed.', 'danger');
      }
    }
  }

  container.addEventListener('click', onAccessPageClick);
  pageCleanups.push(() => container.removeEventListener('click', onAccessPageClick));

  container.querySelector('#access-btn-add').addEventListener('click', () => openAddModal());
}
