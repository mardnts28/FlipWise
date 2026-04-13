/**
 * Full-screen confirm with a 5-second enforced wait before Confirm enables.
 * @returns {Promise<boolean>} true if user confirmed, false if cancelled
 */
function requestActionConfirmation({ title, body, confirmBtnClass = 'btn-grape' }) {
  return new Promise(resolve => {
    const backdrop = document.createElement('div');
    backdrop.className = 'fw-modal-backdrop';
    backdrop.style.zIndex = '1055';
    backdrop.setAttribute('role', 'dialog');
    backdrop.setAttribute('aria-modal', 'true');
    backdrop.innerHTML = `
      <div class="fw-modal animate-modal" style="max-width:420px;">
        <h4 class="fw-bold mb-2" style="color:var(--ink);font-size:1.1rem;">${esc(title)}</h4>
        <div class="text-muted small mb-3">${body}</div>
        <p class="small mb-4" style="color:var(--text-muted);">Please wait 5 seconds before you can confirm.</p>
        <div class="d-flex justify-content-end gap-2">
          <button type="button" class="btn btn-ghost px-4 py-2" data-uac-cancel>Cancel</button>
          <button type="button" class="btn px-4 py-2 fw-semibold ${confirmBtnClass}" data-uac-confirm disabled>Confirm (5)</button>
        </div>
      </div>`;
    document.body.appendChild(backdrop);

    const confirmBtn = backdrop.querySelector('[data-uac-confirm]');
    let sec = 5;
    let timer = null;
    let settled = false;

    function finish(value) {
      if (settled) return;
      settled = true;
      if (timer) clearInterval(timer);
      document.removeEventListener('keydown', onKey);
      backdrop.remove();
      resolve(value);
    }

    function onKey(e) {
      if (e.key === 'Escape') finish(false);
    }
    document.addEventListener('keydown', onKey);

    timer = setInterval(() => {
      sec--;
      if (sec > 0) {
        confirmBtn.textContent = `Confirm (${sec})`;
      } else {
        clearInterval(timer);
        timer = null;
        confirmBtn.textContent = 'Confirm';
        confirmBtn.disabled = false;
      }
    }, 1000);

    backdrop.querySelector('[data-uac-cancel]').onclick = () => finish(false);
    confirmBtn.onclick = () => {
      if (confirmBtn.disabled) return;
      finish(true);
    };
    backdrop.addEventListener('click', e => {
      if (e.target === backdrop) finish(false);
    });
  });
}

function renderUsers(container) {
  container.innerHTML = `
  <div class="page-header mb-4">
    <h2>User Directory</h2>
    <p>Search, filter, and moderate FlipWise accounts.</p>
  </div>
  <div class="row g-3 mb-4">
    ${statCardHTML('Registered Users','…','text-primary','','','All accounts')}
    ${statCardHTML('Active','…','text-success','','','Status: Active')}
    ${statCardHTML('Suspended / Banned','…','text-warning','','','Needs attention')}
  </div>
  <div class="d-flex gap-2 flex-wrap mb-3" id="status-filters">
    ${['All','Active','Suspended','Banned'].map((f,i) =>
      `<button class="filter-pill${i===0?' active':''}" data-filter="${f}">${f}</button>`).join('')}
  </div>
  <div class="d-flex gap-3 mb-4 flex-wrap">
    <input type="search" id="user-search" class="fw-input" style="max-width:320px;" placeholder="Search name, email, or plan…" />
  </div>
  <div id="users-tables">
    <div class="text-center py-5 text-muted"><div class="spinner-border spinner-border-sm me-2"></div>Loading from Firestore…</div>
  </div>`;

  let allUsers = [], activeFilter = 'All', searchTerm = '';

  const unsub = db.collection('users').orderBy('joined','desc').onSnapshot(snap => {
    allUsers = snap.docs.map(d => ({ id: d.id, ...d.data() }));
    updateStats();
    renderTables();
  }, err => { console.error(err); });
  pageCleanups.push(unsub);

  function updateStats() {
    const statVals = container.querySelectorAll('.stat-value');
    if (statVals[0]) statVals[0].textContent = allUsers.length;
    if (statVals[1]) statVals[1].textContent = allUsers.filter(u => u.status === 'Active').length;
    if (statVals[2]) {
      statVals[2].textContent = allUsers.filter(
        u => u.status === 'Suspended' || u.status === 'Banned'
      ).length;
    }
  }

  function filteredUsers() {
    const q = searchTerm.toLowerCase();
    return allUsers.filter(u => {
      const matchSearch = !q || [u.name,u.email,u.role,u.status].some(v => (v||'').toLowerCase().includes(q));
      let matchFilter = true;
      if (activeFilter === 'Active')     matchFilter = u.status === 'Active';
      if (activeFilter === 'Suspended')  matchFilter = u.status === 'Suspended';
      if (activeFilter === 'Banned')     matchFilter = u.status === 'Banned';
      return matchSearch && matchFilter;
    });
  }

  function renderTables() {
    const users  = filteredUsers();
    const admins = users.filter(u => /(admin|moderator)/i.test(u.role||''));
    const regs   = users.filter(u => !/(admin|moderator)/i.test(u.role||''));
    document.getElementById('users-tables').innerHTML =
      userTableHTML('Admin Accounts', admins, 'No admin accounts match.') +
      '<div class="mt-4">' +
      userTableHTML('Registered Users', regs, 'No users match.') +
      '</div>';
    document.querySelectorAll('[data-user-action]').forEach(btn => {
      btn.addEventListener('click', () => {
        const uid = btn.dataset.uid;
        const u   = allUsers.find(x => x.id === uid);
        if (u) openUserModal(u);
      });
    });
  }

  function userTableHTML(title, rows, emptyMsg) {
    return `
    <div class="fw-card p-0 overflow-hidden">
      <div class="px-4 py-3" style="background:var(--surface);border-bottom:1px solid var(--border);">
        <h6 class="fw-bold mb-0" style="color:var(--ink);">${esc(title)}</h6>
      </div>
      <div class="table-responsive">
        <table class="fw-table">
          <thead><tr>
            <th>Identity</th><th>Plan</th><th>Status</th><th>Last Active</th><th class="text-end">Action</th>
          </tr></thead>
          <tbody>
          ${rows.length === 0
            ? `<tr><td colspan="5" class="text-center text-muted py-4">${esc(emptyMsg)}</td></tr>`
            : rows.map(u => `
              <tr>
                <td>
                  <div class="fw-semibold" style="color:var(--ink);">${esc(u.name||'—')}</div>
                  <div class="text-muted" style="font-size:.75rem;">${esc(u.email||'')}</div>
                </td>
                <td>${esc(u.role||'—')}</td>
                <td><span class="fw-badge ${statusBadgeClass(u.status)}">${esc(u.status||'—')}</span></td>
                <td class="text-muted small">${esc(u.lastActive||'—')}</td>
                <td class="text-end">
                  <button class="btn btn-link text-decoration-none fw-semibold p-0"
                          style="color:var(--grape);font-size:.85rem;"
                          data-user-action data-uid="${esc(u.id)}">View</button>
                </td>
              </tr>`).join('')}
          </tbody>
        </table>
      </div>
    </div>`;
  }

  /* Filters */
  document.getElementById('status-filters').addEventListener('click', e => {
    const pill = e.target.closest('.filter-pill');
    if (!pill) return;
    document.querySelectorAll('.filter-pill').forEach(p => p.classList.remove('active'));
    pill.classList.add('active');
    activeFilter = pill.dataset.filter;
    renderTables();
  });
  document.getElementById('user-search').addEventListener('input', e => {
    searchTerm = e.target.value;
    renderTables();
  });

  /* User detail modal */
  function openUserModal(u) {
    const isSuspended = u.status === 'Suspended';
    const isBanned    = u.status === 'Banned';
    document.getElementById('user-detail-content').innerHTML = `
    <div class="d-flex justify-content-between align-items-start mb-4 gap-3">
      <div class="d-flex gap-3 align-items-center">
        <div class="rounded-3 d-flex align-items-center justify-content-center flex-shrink-0"
             style="width:56px;height:56px;background:var(--surface);font-size:1.75rem;">👤</div>
        <div>
          <h3 class="fw-bold mb-0" style="font-size:1.3rem;color:var(--ink);">${esc(u.name||'—')}</h3>
          <p class="text-muted small mb-0">${esc(u.email||'')} · Joined ${esc(u.joined||'—')}</p>
        </div>
      </div>
      <button class="btn btn-link text-muted p-0" onclick="document.getElementById('user-detail-modal').style.display='none'">
        <i class="bi bi-x-lg fs-5"></i>
      </button>
    </div>
    <div class="row g-3 mb-4">
      ${[
        ['Account Status', u.status, statusBadgeClass(u.status)],
        ['Created On', u.joined, ''],
        ['Last Active', u.lastActive, ''],
        ['Decks Created', u.decks??0, ''],
        ['Study Sessions', u.sessions??0, ''],
        ['AI Generations', u.aiGenerations??0, ''],
        ['Plan', u.role, 'badge-ai'],
      ].map(([lbl,val,bc]) => `
        <div class="col-6 col-md-3">
          <div class="p-3 rounded-3" style="background:var(--surface);">
            <div style="font-size:.6rem;font-weight:800;text-transform:uppercase;letter-spacing:.1em;color:var(--text-muted);">${esc(lbl)}</div>
            ${bc ? `<span class="fw-badge ${bc} mt-1">${esc(val)}</span>` : `<div class="fw-semibold mt-1 small" style="color:var(--ink);">${esc(val)}</div>`}
          </div>
        </div>`).join('')}
    </div>
    <div class="border-top pt-4">
      <div style="font-size:.65rem;font-weight:800;text-transform:uppercase;letter-spacing:.12em;color:var(--text-muted);" class="mb-3">Moderation Controls</div>
      <div class="d-flex flex-wrap gap-2">
        <button class="btn btn-ghost px-3 py-2 small fw-semibold" id="modal-reset-btn">
          <i class="bi bi-key me-1"></i>Force Password Reset
        </button>
        <button class="btn px-3 py-2 small fw-semibold ${isSuspended?'btn-success':'btn-warning'}" id="modal-suspend-btn">
          ${isSuspended?'<i class="bi bi-check-circle me-1"></i>Unsuspend':'<i class="bi bi-pause-circle me-1"></i>Suspend'}
        </button>
        <button class="btn px-3 py-2 small fw-semibold ${isBanned?'btn-success':'btn-danger'}" id="modal-ban-btn">
          ${isBanned?'<i class="bi bi-check-circle me-1"></i>Unban':'<i class="bi bi-x-circle me-1"></i>Permanent Ban'}
        </button>
      </div>
    </div>`;

    document.getElementById('modal-reset-btn').onclick = async () => {
      const ok = await requestActionConfirmation({
        title: 'Force password reset',
        body: `Send a password reset email to <span class="fw-semibold" style="color:var(--ink);">${esc(u.email || '')}</span>?`,
        confirmBtnClass: 'btn-grape',
      });
      if (!ok) return;
      try {
        await auth.sendPasswordResetEmail(u.email);
        await logAudit('USER_PASSWORD_RESET', { targetEmail: u.email });
        showToast(`Reset link sent to ${u.email}`);
      } catch (e) { showToast('Failed to send reset email.', 'danger'); }
    };
    document.getElementById('modal-suspend-btn').onclick = async () => {
      const next = isSuspended ? 'Active' : 'Suspended';
      const ok = await requestActionConfirmation({
        title: isSuspended ? 'Unsuspend account' : 'Suspend account',
        body: isSuspended
          ? `Restore full access for <span class="fw-semibold" style="color:var(--ink);">${esc(u.name || '—')}</span> (${esc(u.email || '')})?`
          : `Suspend <span class="fw-semibold" style="color:var(--ink);">${esc(u.name || '—')}</span> (${esc(u.email || '')})? They will be blocked from the app until unsuspended.`,
        confirmBtnClass: isSuspended ? 'btn-success' : 'btn-warning',
      });
      if (!ok) return;
      await db.collection('users').doc(u.id).update({ status: next });
      await logAudit('USER_STATUS_CHANGED', { targetEmail: u.email, newStatus: next });
      showToast(`${u.name} is now ${next}.`);
      document.getElementById('user-detail-modal').style.display = 'none';
    };
    document.getElementById('modal-ban-btn').onclick = async () => {
      const next = isBanned ? 'Active' : 'Banned';
      const ok = await requestActionConfirmation({
        title: isBanned ? 'Remove ban' : 'Permanent ban',
        body: isBanned
          ? `Unban <span class="fw-semibold" style="color:var(--ink);">${esc(u.name || '—')}</span> and set status to Active?`
          : `Permanently ban <span class="fw-semibold" style="color:var(--ink);">${esc(u.name || '—')}</span> (${esc(u.email || '')})? This is a severe moderation action.`,
        confirmBtnClass: isBanned ? 'btn-success' : 'btn-danger',
      });
      if (!ok) return;
      await db.collection('users').doc(u.id).update({ status: next });
      await logAudit('USER_BAN_TOGGLED', { targetEmail: u.email, newStatus: next });
      showToast(`${u.name}: status → ${next}.`);
      document.getElementById('user-detail-modal').style.display = 'none';
    };

    document.getElementById('user-detail-modal').style.display = 'flex';
  }
}

function statusBadgeClass(s) {
  if (s === 'Active')    return 'badge-active';
  if (s === 'Suspended') return 'badge-suspended';
  if (s === 'Banned')    return 'badge-banned';
  return '';
}
