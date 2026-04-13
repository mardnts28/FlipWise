function overviewAuditIcon(action) {
  const a = (String(action || '').toUpperCase());
  if (a.includes('LOGIN')) return 'bi-box-arrow-in-right';
  if (a.includes('LOGOUT')) return 'bi-box-arrow-left';
  if (a.includes('PASSWORD') || a.includes('RESET')) return 'bi-key';
  if (a.includes('ACCESS_USER')) return 'bi-person-gear';
  if (a.includes('USER_DELETED') || a.includes('REMOVED')) return 'bi-person-x';
  if (a.includes('STATUS') || a.includes('BAN')) return 'bi-slash-circle';
  if (a.includes('SETTING')) return 'bi-toggle2-on';
  if (a.includes('ENV_VAR')) return 'bi-hdd-network';
  if (a.includes('EMERGENCY')) return 'bi-exclamation-octagon';
  if (a.includes('DECK')) return 'bi-collection';
  if (a.includes('FLAG')) return 'bi-flag';
  if (a.includes('CHALLENGE') || a.includes('ACHIEVEMENT')) return 'bi-trophy';
  if (a.includes('FEEDBACK') || a.includes('REPORT')) return 'bi-chat-dots';
  if (a.includes('QUOTA') || a.includes('PROMPT')) return 'bi-stars';
  return 'bi-journal-text';
}

function renderOverview(container) {
  container.innerHTML = `
  <div class="stride-banner">
    <i class="bi bi-shield-lock-fill fs-5" aria-hidden="true"></i>
    <div>
      <div class="d-flex gap-2 flex-wrap">
        ${['S–Spoofing','T–Tampering','R–Repudiation','I–Info Disc.','D–DoS','E–Elevation']
          .map(l => `<span class="stride-pill">${esc(l)}</span>`).join('')}
      </div>
      <div style="margin-top:.35rem;opacity:.7;font-size:.72rem;">STRIDE security model active · All sensitive actions are audited</div>
    </div>
  </div>

  <div class="page-header mb-4">
    <h2>Dashboard Overview</h2>
    <p class="text-muted mb-0">Live platform metrics and system health.</p>
  </div>

  <div class="row g-3 mb-4" id="overview-stats">
    ${statCardHTML('Registered Users', '…', 'text-primary', '', '', 'All user profiles')}
    ${statCardHTML('Active', '…', 'text-success', '', '', 'status: Active')}
    ${statCardHTML('Suspended / Banned', '…', 'text-warning', '', '', 'Moderation states')}
    ${statCardHTML('Total Decks', '…', 'text-grape', '', '', 'Sum of decks on profiles')}
    ${statCardHTML('AI Generations', '…', 'text-danger', '', '', 'Sum of aiGenerations field')}
  </div>

  <div class="row g-4">
    <div class="col-xl-8">
      <div class="fw-card">
        <div class="d-flex justify-content-between align-items-start mb-4 flex-wrap gap-2">
          <div>
            <h5 class="fw-bold mb-1" style="color:var(--ink);">System Health</h5>
            <p class="text-muted small mb-0">High-level status only — this console does not poll external uptime APIs.</p>
          </div>
          <span class="fw-badge" style="background:#EDE9FE;color:#5B21B6;font-size:.65rem;">
            Client-side dashboard
          </span>
        </div>
        <div class="row g-3">
          ${[
            {
              svc: 'Firebase',
              stat: 'Connected',
              detail: 'Using your configured web app · Auth + Firestore from this browser',
              meta: 'See Firebase Console for quotas & errors',
            },
            {
              svc: 'Gemini AI',
              stat: 'Config',
              detail: 'Model usage and quotas are edited on the Gemini AI page',
              meta: 'No live billing or latency feed here',
            },
            {
              svc: 'Audit trail',
              stat: 'Active',
              detail: 'Sensitive actions write to auditLogs when rules allow',
              meta: 'Open System Logs for full history',
            },
          ]
            .map(
              h => `
            <div class="col-md-4">
              <div class="p-3 rounded-3" style="background:var(--surface);border:1px solid var(--border);">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <span style="font-size:.65rem;font-weight:800;text-transform:uppercase;letter-spacing:.1em;color:var(--text-muted);">${esc(h.svc)}</span>
                  <span class="fw-badge badge-active">${esc(h.stat)}</span>
                </div>
                <p class="fw-semibold small mb-1" style="color:var(--ink);">${esc(h.detail)}</p>
                <p class="text-muted mb-0" style="font-size:.72rem;">${esc(h.meta)}</p>
              </div>
            </div>`
            )
            .join('')}
        </div>
      </div>
    </div>
    <div class="col-xl-4">
      <div class="fw-card">
        <h5 class="fw-bold mb-2" style="color:var(--ink);font-size:1rem;">Recent audit activity</h5>
        <div id="recent-activity-list" style="max-height:11rem;overflow-y:auto;">
          <div class="text-muted small py-1"><span class="spinner-border spinner-border-sm me-2"></span>Loading…</div>
        </div>
      </div>
    </div>
  </div>`;

  function updateUserStats(snap) {
    const users = snap.docs.map(d => ({ id: d.id, ...d.data() }));
    const statEls = container.querySelectorAll('#overview-stats .stat-value');
    const n = users.length;
    const active = users.filter(u => u.status === 'Active').length;
    const restricted = users.filter(u => u.status === 'Suspended' || u.status === 'Banned').length;
    const totalDecks = users.reduce((sum, u) => sum + (Number(u.decks) || 0), 0);
    const totalAi = users.reduce((sum, u) => sum + (Number(u.aiGenerations) || 0), 0);
    if (statEls[0]) statEls[0].textContent = n.toLocaleString();
    if (statEls[1]) statEls[1].textContent = active.toLocaleString();
    if (statEls[2]) statEls[2].textContent = restricted.toLocaleString();
    if (statEls[3]) statEls[3].textContent = totalDecks.toLocaleString();
    if (statEls[4]) statEls[4].textContent = totalAi.toLocaleString();
  }

  function renderAuditList(docs) {
    const listEl = container.querySelector('#recent-activity-list');
    if (!listEl) return;
    if (docs.length === 0) {
      listEl.innerHTML = '<p class="text-muted small mb-0" style="font-size:.72rem;">No entries yet.</p>';
      return;
    }
    const trunc = (s, n) => {
      const t = String(s ?? '');
      return t.length <= n ? t : `${t.slice(0, n - 1)}…`;
    };
    listEl.innerHTML = docs
      .map(log => {
        const ts = log.timestamp?.toDate?.() ?? null;
        const timeStr = ts
          ? ts.toLocaleString(undefined, { month: 'numeric', day: 'numeric', hour: 'numeric', minute: '2-digit' })
          : '—';
        const ic = overviewAuditIcon(log.action);
        const who = log.performedBy || '—';
        const act = log.action || '—';
        const tip = `${act}\n${who}`;
        return `
            <div class="d-flex align-items-start gap-2 py-1" style="border-bottom:1px solid var(--border);font-size:.72rem;line-height:1.25;">
              <i class="bi ${ic} text-grape flex-shrink-0" style="margin-top:.1rem;font-size:.85rem;" aria-hidden="true"></i>
              <div class="flex-grow-1 min-w-0">
                <div class="d-flex justify-content-between align-items-baseline gap-2">
                  <span class="fw-semibold text-truncate" style="color:var(--ink);" title="${esc(tip)}">${esc(trunc(act, 32))}</span>
                  <span class="text-muted flex-shrink-0" style="font-size:.65rem;">${esc(timeStr)}</span>
                </div>
                <div class="text-muted text-truncate" style="font-size:.68rem;" title="${esc(who)}">${esc(trunc(who, 36))}</div>
              </div>
            </div>`;
      })
      .join('');
  }

  const unsubUsers = db.collection('users').onSnapshot(
    snap => updateUserStats(snap),
    err => {
      console.error(err);
      showToast('Could not load user stats.', 'warning');
    }
  );

  const unsubAudit = db
    .collection('auditLogs')
    .orderBy('timestamp', 'desc')
    .limit(5)
    .onSnapshot(
      snap => {
        const rows = snap.docs.map(d => ({ id: d.id, ...d.data() }));
        renderAuditList(rows);
      },
      err => {
        console.error(err);
        const listEl = container.querySelector('#recent-activity-list');
        if (listEl) {
          listEl.innerHTML =
            '<p class="text-danger small mb-0" style="font-size:.72rem;">Could not load audit log.</p>';
        }
        showToast('Audit log unavailable.', 'warning');
      }
    );

  pageCleanups.push(() => {
    unsubUsers();
    unsubAudit();
  });
}
