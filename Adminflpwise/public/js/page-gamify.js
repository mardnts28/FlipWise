function renderGamify(container) {
  let gamifyModalBackdrop = null;
  let gamifyModalRemoveEscape = null;

  function closeGamifyModal() {
    if (gamifyModalRemoveEscape) {
      gamifyModalRemoveEscape();
      gamifyModalRemoveEscape = null;
    }
    if (gamifyModalBackdrop) {
      gamifyModalBackdrop.remove();
      gamifyModalBackdrop = null;
    }
  }

  function openGamifyModal(innerHtml, onMount) {
    closeGamifyModal();
    const backdrop = document.createElement('div');
    backdrop.className = 'fw-modal-backdrop';
    backdrop.style.zIndex = '1060';
    backdrop.setAttribute('role', 'dialog');
    backdrop.setAttribute('aria-modal', 'true');
    backdrop.innerHTML = innerHtml;
    document.body.appendChild(backdrop);
    gamifyModalBackdrop = backdrop;

    const close = () => closeGamifyModal();
    const onKey = e => {
      if (e.key === 'Escape') close();
    };
    document.addEventListener('keydown', onKey);
    gamifyModalRemoveEscape = () => document.removeEventListener('keydown', onKey);

    backdrop.querySelectorAll('.gamify-modal-close, .gamify-modal-cancel').forEach(el => {
      el.addEventListener('click', close);
    });
    backdrop.addEventListener('click', e => {
      if (e.target === backdrop) close();
    });

    if (typeof onMount === 'function') onMount(backdrop, close);
  }

  container.innerHTML = `
  <div class="page-header mb-4">
    <h2>Gamification Engine</h2>
    <p class="text-muted mb-0">Weekly challenges and achievements are stored in Firestore collections <code class="small">challenges</code> and <code class="small">achievements</code>.</p>
  </div>
  <div class="fw-tabs mb-4 d-flex">
    <button type="button" class="fw-tab active" data-gtab="challenges">Challenges</button>
    <button type="button" class="fw-tab" data-gtab="achievements">Achievements</button>
  </div>
  <div id="gamify-content">
    <div class="text-center py-5 text-muted"><div class="spinner-border spinner-border-sm"></div> Loading…</div>
  </div>`;

  const contentEl = () => container.querySelector('#gamify-content');
  let activeGTab = 'challenges';
  let challenges = [];
  let achievements = [];

  function renderChallenges() {
    const el = contentEl();
    if (!el) return;
    el.innerHTML = `
    <div class="fw-card mb-4">
      <h6 class="fw-bold mb-3" style="color:var(--ink);">Launch New Challenge</h6>
      <div class="row g-3">
        <div class="col-md-5">
          <label class="fw-label" for="ch-title">Challenge Title</label>
          <input type="text" id="ch-title" class="fw-input" placeholder="e.g. Study Streak Week" maxlength="80" autocomplete="off" />
        </div>
        <div class="col-md-5">
          <label class="fw-label" for="ch-goal">Target Goal</label>
          <input type="text" id="ch-goal" class="fw-input" placeholder="e.g. Study 7 days in a row" maxlength="120" autocomplete="off" />
        </div>
        <div class="col-md-2 d-flex align-items-end">
          <button type="button" class="btn btn-grape w-100 py-2 fw-semibold" id="ch-launch-btn">
            <i class="bi bi-plus-lg me-1" aria-hidden="true"></i>Launch
          </button>
        </div>
      </div>
    </div>
    <div class="fw-card p-0 overflow-hidden">
      <div class="px-4 py-3" style="background:var(--surface);border-bottom:1px solid var(--border);">
        <h6 class="fw-bold mb-0" style="color:var(--ink);">Active Challenges</h6>
      </div>
      ${challenges.length === 0
        ? '<p class="text-muted small p-4 mb-0">No challenges yet.</p>'
        : challenges.map(ch => `
          <div class="d-flex justify-content-between align-items-center p-4"
               style="border-bottom:1px solid var(--border);">
            <div>
              <div class="d-flex align-items-center gap-2 mb-1">
                <span class="fw-bold" style="color:var(--ink);">${esc(ch.name || '—')}</span>
                <span class="fw-badge badge-active" style="font-size:.6rem;">${esc(ch.status || 'Weekly')}</span>
              </div>
              <div class="text-muted small">${esc(ch.goal || '—')} · <span style="color:var(--grape);">${esc(ch.reward || '250 XP')}</span></div>
            </div>
            <div class="d-flex gap-2">
              <button type="button" class="btn btn-ghost px-3 py-2 small fw-semibold"
                      data-action="edit-challenge" data-id="${esc(ch.id)}">Edit</button>
              <button type="button" class="btn btn-danger px-3 py-2 small fw-semibold"
                      data-action="remove-challenge" data-id="${esc(ch.id)}">Remove</button>
            </div>
          </div>`).join('')}
    </div>`;
  }

  function renderAchievements() {
    const el = contentEl();
    if (!el) return;
    el.innerHTML = `
    <div class="fw-card p-0 overflow-hidden mb-4">
      <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 px-4 py-3"
           style="background:var(--surface);border-bottom:1px solid var(--border);">
        <h6 class="fw-bold mb-0" style="color:var(--ink);">Achievements</h6>
        <button type="button" class="btn btn-grape px-3 py-2 small fw-semibold" id="ach-open-add-modal">
          <i class="bi bi-plus-lg me-1" aria-hidden="true"></i>Add achievement
        </button>
      </div>
      <div class="table-responsive">
        <table class="fw-table">
          <thead><tr><th>Badge</th><th>Unlock Criteria</th><th>Season</th><th>Status</th><th class="text-end">Action</th></tr></thead>
          <tbody>
          ${achievements.length === 0
            ? '<tr><td colspan="5" class="text-center text-muted py-4">No achievements yet. Use <strong>Add achievement</strong> to create one.</td></tr>'
            : achievements.map(ac => `
              <tr>
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <div class="rounded-circle d-flex align-items-center justify-content-center text-warning"
                         style="width:32px;height:32px;background:#FEF3C7;" aria-hidden="true">
                      <i class="bi bi-trophy-fill"></i>
                    </div>
                    <span class="fw-semibold" style="color:var(--ink);">${esc(ac.title || '—')}</span>
                  </div>
                </td>
                <td class="text-muted small">${esc(ac.criteria || '—')}</td>
                <td class="text-muted small">${esc(ac.season || '—')}</td>
                <td><span class="fw-badge badge-active">${esc(ac.status || 'Active')}</span></td>
                <td class="text-end">
                  <button type="button" class="btn btn-link fw-semibold p-0" style="color:var(--grape);font-size:.85rem;"
                          data-action="edit-achievement" data-id="${esc(ac.id)}">Adjust criteria</button>
                </td>
              </tr>`).join('')}
          </tbody>
        </table>
      </div>
    </div>`;
  }

  function refreshActiveTab() {
    if (activeGTab === 'challenges') renderChallenges();
    else renderAchievements();
  }

  async function handleLaunchChallenge() {
    const titleInp = container.querySelector('#ch-title');
    const goalInp = container.querySelector('#ch-goal');
    const btn = container.querySelector('#ch-launch-btn');
    const title = titleInp?.value.trim();
    const goal = goalInp?.value.trim();
    if (!title || !goal) {
      showToast('Enter both a title and goal.', 'warning');
      return;
    }
    const orig = btn?.innerHTML;
    if (btn) {
      btn.disabled = true;
      btn.textContent = 'Launching…';
    }
    try {
      await db.collection('challenges').add({
        name: title,
        goal,
        reward: '250 XP',
        status: 'Weekly',
        createdAt: firebase.firestore.FieldValue.serverTimestamp(),
      });
      await logAudit('CHALLENGE_CREATED', { title, goal });
      showToast('Challenge launched.');
      if (titleInp) titleInp.value = '';
      if (goalInp) goalInp.value = '';
    } catch (err) {
      console.error(err);
      showToast(err?.message || 'Could not create challenge.', 'danger');
    } finally {
      if (btn) {
        btn.disabled = false;
        if (orig) btn.innerHTML = orig;
      }
    }
  }

  function openEditChallengeModal(id) {
    const ch = challenges.find(c => c.id === id);
    if (!ch) return;
    openGamifyModal(
      `
      <div class="fw-modal animate-modal" role="document" style="max-width:440px;">
        <div class="d-flex justify-content-between align-items-start mb-3 gap-2">
          <div class="min-w-0">
            <h4 class="fw-bold mb-1" style="color:var(--ink);font-size:1.15rem;">Edit challenge</h4>
            <p class="text-muted small mb-0 text-truncate">${esc(ch.name || '—')}</p>
          </div>
          <button type="button" class="btn btn-link text-muted p-0 flex-shrink-0 gamify-modal-close" aria-label="Close">
            <i class="bi bi-x-lg fs-5"></i>
          </button>
        </div>
        <label class="fw-label" for="gamify-modal-ch-goal">Target goal</label>
        <textarea id="gamify-modal-ch-goal" class="fw-input" rows="4" style="font-size:.875rem;"></textarea>
        <div class="d-flex justify-content-end gap-2 mt-4">
          <button type="button" class="btn btn-ghost px-4 py-2 gamify-modal-cancel">Cancel</button>
          <button type="button" class="btn btn-grape px-4 py-2 fw-semibold" id="gamify-modal-ch-save">Save changes</button>
        </div>
      </div>`,
      (backdrop, close) => {
        const goalTa = backdrop.querySelector('#gamify-modal-ch-goal');
        if (goalTa) {
          goalTa.value = ch.goal || '';
          requestAnimationFrame(() => goalTa.focus());
        }
        const saveBtn = backdrop.querySelector('#gamify-modal-ch-save');
        saveBtn.addEventListener('click', async () => {
          const v = backdrop.querySelector('#gamify-modal-ch-goal')?.value.trim();
          if (!v) {
            showToast('Goal cannot be empty.', 'warning');
            return;
          }
          saveBtn.disabled = true;
          const orig = saveBtn.textContent;
          saveBtn.textContent = 'Saving…';
          try {
            await db.collection('challenges').doc(id).update({ goal: v });
            await logAudit('CHALLENGE_UPDATED', { id });
            showToast('Challenge updated.');
            close();
          } catch (err) {
            console.error(err);
            showToast(err?.message || 'Update failed.', 'danger');
          } finally {
            saveBtn.disabled = false;
            saveBtn.textContent = orig;
          }
        });
      }
    );
  }

  function openRemoveChallengeModal(id) {
    const ch = challenges.find(c => c.id === id);
    const name = ch?.name || 'this challenge';
    openGamifyModal(
      `
      <div class="fw-modal animate-modal" role="document" style="max-width:420px;border-top:3px solid var(--danger);">
        <div class="d-flex justify-content-between align-items-start mb-3 gap-2">
          <div>
            <h4 class="fw-bold mb-1" style="color:var(--ink);font-size:1.1rem;">Remove challenge</h4>
            <p class="text-muted small mb-0">This cannot be undone.</p>
          </div>
          <button type="button" class="btn btn-link text-muted p-0 flex-shrink-0 gamify-modal-close" aria-label="Close">
            <i class="bi bi-x-lg fs-5"></i>
          </button>
        </div>
        <p class="small mb-4" style="color:var(--ink);">Remove <span class="fw-semibold">${esc(name)}</span> from Firestore?</p>
        <div class="d-flex justify-content-end gap-2">
          <button type="button" class="btn btn-ghost px-4 py-2 gamify-modal-cancel">Cancel</button>
          <button type="button" class="btn btn-danger px-4 py-2 fw-semibold" id="gamify-modal-ch-remove">Remove</button>
        </div>
      </div>`,
      (backdrop, close) => {
        backdrop.querySelector('#gamify-modal-ch-remove').addEventListener('click', async () => {
          const rm = backdrop.querySelector('#gamify-modal-ch-remove');
          rm.disabled = true;
          rm.textContent = 'Removing…';
          try {
            await db.collection('challenges').doc(id).delete();
            await logAudit('CHALLENGE_REMOVED', { id });
            showToast('Challenge removed.', 'warning');
            close();
          } catch (err) {
            console.error(err);
            showToast(err?.message || 'Could not remove challenge.', 'danger');
            rm.disabled = false;
            rm.textContent = 'Remove';
          }
        });
      }
    );
  }

  function openAddAchievementModal() {
    openGamifyModal(
      `
      <div class="fw-modal animate-modal" role="document" style="max-width:480px;">
        <div class="d-flex justify-content-between align-items-start mb-3 gap-2">
          <div>
            <h4 class="fw-bold mb-1" style="color:var(--ink);font-size:1.15rem;">Add achievement</h4>
            <p class="text-muted small mb-0">Creates a document in <code class="small">achievements</code>.</p>
          </div>
          <button type="button" class="btn btn-link text-muted p-0 flex-shrink-0 gamify-modal-close" aria-label="Close">
            <i class="bi bi-x-lg fs-5"></i>
          </button>
        </div>
        <div class="mb-3">
          <label class="fw-label" for="gamify-modal-ach-title">Title</label>
          <input type="text" id="gamify-modal-ach-title" class="fw-input" maxlength="80" placeholder="e.g. Seasonal Scholar" autocomplete="off" />
        </div>
        <div class="mb-3">
          <label class="fw-label" for="gamify-modal-ach-criteria">Unlock criteria</label>
          <textarea id="gamify-modal-ach-criteria" class="fw-input" rows="3" style="font-size:.875rem;" placeholder="e.g. Complete 3 weekly challenges"></textarea>
        </div>
        <div class="mb-4">
          <label class="fw-label" for="gamify-modal-ach-season">Season</label>
          <input type="text" id="gamify-modal-ach-season" class="fw-input" maxlength="40" placeholder="Seasonal" value="Seasonal" autocomplete="off" />
        </div>
        <div class="d-flex justify-content-end gap-2">
          <button type="button" class="btn btn-ghost px-4 py-2 gamify-modal-cancel">Cancel</button>
          <button type="button" class="btn btn-grape px-4 py-2 fw-semibold" id="gamify-modal-ach-save">Save achievement</button>
        </div>
      </div>`,
      (backdrop, close) => {
        const titleInp = backdrop.querySelector('#gamify-modal-ach-title');
        requestAnimationFrame(() => titleInp?.focus());
        backdrop.querySelector('#gamify-modal-ach-save').addEventListener('click', async () => {
          const title = backdrop.querySelector('#gamify-modal-ach-title')?.value.trim();
          const criteria = backdrop.querySelector('#gamify-modal-ach-criteria')?.value.trim();
          const season = backdrop.querySelector('#gamify-modal-ach-season')?.value.trim() || 'Seasonal';
          if (!title || !criteria) {
            showToast('Title and unlock criteria are required.', 'warning');
            return;
          }
          const saveBtn = backdrop.querySelector('#gamify-modal-ach-save');
          saveBtn.disabled = true;
          const orig = saveBtn.textContent;
          saveBtn.textContent = 'Saving…';
          try {
            await db.collection('achievements').add({
              title,
              criteria,
              season,
              status: 'Active',
              createdAt: firebase.firestore.FieldValue.serverTimestamp(),
            });
            await logAudit('ACHIEVEMENT_CREATED', { title, criteria, season });
            showToast('Achievement added.');
            close();
          } catch (err) {
            console.error(err);
            showToast(err?.message || 'Could not add achievement.', 'danger');
          } finally {
            saveBtn.disabled = false;
            saveBtn.textContent = orig;
          }
        });
      }
    );
  }

  function openEditAchievementModal(id) {
    const ac = achievements.find(a => a.id === id);
    if (!ac) return;
    openGamifyModal(
      `
      <div class="fw-modal animate-modal" role="document" style="max-width:440px;">
        <div class="d-flex justify-content-between align-items-start mb-3 gap-2">
          <div class="min-w-0">
            <h4 class="fw-bold mb-1" style="color:var(--ink);font-size:1.15rem;">Adjust criteria</h4>
            <p class="text-muted small mb-0 text-truncate">${esc(ac.title || '—')}</p>
          </div>
          <button type="button" class="btn btn-link text-muted p-0 flex-shrink-0 gamify-modal-close" aria-label="Close">
            <i class="bi bi-x-lg fs-5"></i>
          </button>
        </div>
        <label class="fw-label" for="gamify-modal-ac-criteria">Unlock criteria</label>
        <textarea id="gamify-modal-ac-criteria" class="fw-input" rows="4" style="font-size:.875rem;"></textarea>
        <div class="d-flex justify-content-end gap-2 mt-4">
          <button type="button" class="btn btn-ghost px-4 py-2 gamify-modal-cancel">Cancel</button>
          <button type="button" class="btn btn-grape px-4 py-2 fw-semibold" id="gamify-modal-ac-save">Save changes</button>
        </div>
      </div>`,
      (backdrop, close) => {
        const critTa = backdrop.querySelector('#gamify-modal-ac-criteria');
        if (critTa) {
          critTa.value = ac.criteria || '';
          requestAnimationFrame(() => critTa.focus());
        }
        backdrop.querySelector('#gamify-modal-ac-save').addEventListener('click', async () => {
          const v = backdrop.querySelector('#gamify-modal-ac-criteria')?.value.trim();
          if (!v) {
            showToast('Criteria cannot be empty.', 'warning');
            return;
          }
          const saveBtn = backdrop.querySelector('#gamify-modal-ac-save');
          saveBtn.disabled = true;
          const orig = saveBtn.textContent;
          saveBtn.textContent = 'Saving…';
          try {
            await db.collection('achievements').doc(id).update({ criteria: v });
            await logAudit('ACHIEVEMENT_CRITERIA_UPDATED', { id });
            showToast('Criteria updated.');
            close();
          } catch (err) {
            console.error(err);
            showToast(err?.message || 'Update failed.', 'danger');
          } finally {
            saveBtn.disabled = false;
            saveBtn.textContent = orig;
          }
        });
      }
    );
  }

  function onContainerClick(e) {
    if (e.target.closest('#ch-launch-btn')) {
      void handleLaunchChallenge();
      return;
    }
    if (e.target.closest('#ach-open-add-modal')) {
      openAddAchievementModal();
      return;
    }
    const edCh = e.target.closest('[data-action="edit-challenge"]');
    if (edCh?.dataset?.id) {
      openEditChallengeModal(edCh.dataset.id);
      return;
    }
    const rmCh = e.target.closest('[data-action="remove-challenge"]');
    if (rmCh?.dataset?.id) {
      openRemoveChallengeModal(rmCh.dataset.id);
      return;
    }
    const edAc = e.target.closest('[data-action="edit-achievement"]');
    if (edAc?.dataset?.id) {
      openEditAchievementModal(edAc.dataset.id);
      return;
    }
  }

  container.addEventListener('click', onContainerClick);
  pageCleanups.push(() => container.removeEventListener('click', onContainerClick));
  pageCleanups.push(closeGamifyModal);

  container.querySelector('.fw-tabs').addEventListener('click', e => {
    const btn = e.target.closest('.fw-tab');
    if (!btn) return;
    container.querySelectorAll('.fw-tab').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    activeGTab = btn.dataset.gtab;
    refreshActiveTab();
  });

  const unsubC = db
    .collection('challenges')
    .orderBy('createdAt', 'desc')
    .onSnapshot(
      snap => {
        challenges = snap.docs.map(d => ({ id: d.id, ...d.data() }));
        if (activeGTab === 'challenges') renderChallenges();
      },
      err => {
        console.error(err);
        showToast('Could not load challenges.', 'warning');
      }
    );

  const unsubA = db
    .collection('achievements')
    .orderBy('createdAt', 'desc')
    .onSnapshot(
      snap => {
        achievements = snap.docs.map(d => ({ id: d.id, ...d.data() }));
        if (activeGTab === 'achievements') renderAchievements();
      },
      err => {
        console.error(err);
        showToast('Could not load achievements.', 'warning');
      }
    );

  pageCleanups.push(() => {
    unsubC && unsubC();
    unsubA && unsubA();
  });

  renderChallenges();
}
