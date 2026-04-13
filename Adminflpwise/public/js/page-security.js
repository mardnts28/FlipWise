function actionLogIcon(action) {
  const a = (action || '').toUpperCase();
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
  return 'bi-journal-text';
}

function formatLogDetailsPreview(detailsObj, maxLen) {
  const raw = JSON.stringify(detailsObj || {});
  if (raw.length <= maxLen) return raw;
  return raw.slice(0, maxLen - 1) + '…';
}

function auditTitleAttr(text, maxLen) {
  const s = String(text ?? '');
  const t = s.length > maxLen ? s.slice(0, maxLen) + '…' : s;
  return esc(t);
}

function renderSecurity(container) {
  container.innerHTML = `
  <div class="page-header mb-4">
    <h2 class="d-flex align-items-center gap-2"><i class="bi bi-shield-lock text-grape"></i> Security Audit</h2>
    <p class="text-muted mb-0"><i class="bi bi-database me-1"></i>Live audit log — every admin action is recorded to Firestore (STRIDE: Repudiation).</p>
  </div>

  <!-- Search & Filter Bar -->
  <div class="fw-card mb-4">
    <div class="row g-3 align-items-end">
      <div class="col-md-4">
        <label class="fw-label"><i class="bi bi-search me-1"></i>Search Logs</label>
        <input type="search" id="log-search" class="fw-input" placeholder="Search action, user, or details…" />
      </div>
      <div class="col-md-3">
        <label class="fw-label"><i class="bi bi-funnel me-1"></i>Filter by Action</label>
        <select id="log-action-filter" class="fw-select">
          <option value="All">All Actions</option>
          <option value="ADMIN_LOGIN">Admin Login</option>
          <option value="ADMIN_LOGOUT">Admin Logout</option>
          <option value="PASSWORD_RESET_REQUESTED">Password Reset</option>
          <option value="ACCESS_USER_CREATED">Access User Created</option>
          <option value="ACCESS_USER_UPDATED">Access User Updated</option>
          <option value="ACCESS_USER_REMOVED">Access User Removed</option>
          <option value="USER_DELETED">User Deleted</option>
          <option value="USER_STATUS_CHANGED">Status Changed</option>
          <option value="USER_BAN_TOGGLED">Ban Toggled</option>
          <option value="USER_PASSWORD_RESET">Password Reset (Admin)</option>
          <option value="SETTING_TOGGLED">Setting Toggled</option>
          <option value="ENV_VAR_ROTATED">Env Var Rotated</option>
          <option value="EMERGENCY_AUTH_FAILED">Emergency Auth Failed</option>
          <option value="DECK_DELETED">Deck Deleted</option>
          <option value="DECK_FEATURED">Deck Featured</option>
          <option value="FLAG_DISMISSED">Flag Dismissed</option>
          <option value="CHALLENGE_CREATED">Challenge Created</option>
          <option value="ACHIEVEMENT_CREATED">Achievement Created</option>
          <option value="FEEDBACK_REPLIED">Feedback Replied</option>
          <option value="REPORT_REVIEWED">Report Reviewed</option>
        </select>
      </div>
      <div class="col-md-3">
        <label class="fw-label"><i class="bi bi-calendar3 me-1"></i>Filter by Date</label>
        <input type="date" id="log-date-filter" class="fw-input" />
      </div>
      <div class="col-md-2">
        <label class="fw-label visually-hidden">Reset filters</label>
        <button type="button" class="btn btn-ghost w-100 py-2 fw-semibold" id="log-clear-btn">
          <i class="bi bi-x-circle me-1"></i>Clear
        </button>
      </div>
    </div>
    <!-- Active filter tags -->
    <div id="active-filters" class="d-flex gap-2 flex-wrap mt-3" style="display:none;"></div>
  </div>

  <div class="row g-4">
    <div class="col-12">
      <div class="fw-card p-0 overflow-hidden">
        <div class="d-flex justify-content-between align-items-center px-4 py-3"
             style="background:var(--surface);border-bottom:1px solid var(--border);">
          <div class="d-flex align-items-center gap-3">
            <h6 class="fw-bold mb-0 d-flex align-items-center gap-2" style="color:var(--ink);">
              <i class="bi bi-terminal text-grape"></i> Audit Log
            </h6>
            <span class="fw-badge badge-active"><i class="bi bi-broadcast me-1"></i><span class="animate-pulse-dot me-1"></span>Live</span>
          </div>
          <div class="d-flex align-items-center gap-3">
            <span class="text-muted small" id="log-count"><i class="bi bi-list-ul me-1"></i>0 entries</span>
            <button type="button" class="btn btn-ghost px-3 py-2 small fw-semibold" id="export-log-btn">
              <i class="bi bi-download me-1"></i>Export CSV
            </button>
          </div>
        </div>
        <div class="terminal-box" id="terminal-log" role="log" aria-live="polite">
          <div class="text-muted"><i class="bi bi-hourglass-split me-2"></i>Loading audit logs from Firestore…</div>
        </div>
        <!-- No results message -->
        <div id="log-empty" style="display:none;" class="text-center py-5 text-muted small">
          <i class="bi bi-inbox fs-1 d-block mb-2 opacity-50"></i>
          <i class="bi bi-search me-1"></i>No logs match your search or filter.
        </div>
      </div>
    </div>

    <div class="col-12">
      <div class="fw-card">
        <h6 class="fw-bold mb-3 d-flex align-items-center gap-2" style="color:var(--ink);">
          <i class="bi bi-diagram-3 text-grape"></i> STRIDE Threat Model Status
        </h6>
        <div class="row g-3">
          ${[
            { threat:'Spoofing',          icon:'bi-person-bounding-box', control:'Firebase Auth + Admin role verification',          status:'Active' },
            { threat:'Tampering',         icon:'bi-wrench-adjustable',   control:'Firestore Security Rules + audit on all writes',   status:'Active' },
            { threat:'Repudiation',       icon:'bi-journal-check',       control:'auditLogs collection with server timestamp + uid', status:'Active' },
            { threat:'Info Disclosure',   icon:'bi-eye-slash',           control:'Generic error messages, no raw Firebase errors',   status:'Active' },
            { threat:'Denial of Service', icon:'bi-speedometer2',        control:'Client-side rate limiter (5 attempts / 5 min)',    status:'Active' },
            { threat:'Elevation',         icon:'bi-shield-exclamation',  control:'Role checked from Firestore, not token alone',     status:'Active' },
          ].map(s => `
            <div class="col-md-6">
              <div class="d-flex align-items-start gap-3 p-3 rounded-3"
                   style="background:var(--surface);border:1px solid var(--border);">
                <span class="d-flex align-items-center justify-content-center flex-shrink-0 rounded-3"
                      style="width:40px;height:40px;background:rgba(109,40,217,.1);color:var(--grape);">
                  <i class="bi ${esc(s.icon)} fs-5"></i>
                </span>
                <div class="min-w-0 flex-grow-1">
                  <div class="fw-semibold small" style="color:var(--ink);">${esc(s.threat)}</div>
                  <div class="text-muted" style="font-size:.75rem;">${esc(s.control)}</div>
                </div>
                <span class="fw-badge badge-active ms-auto flex-shrink-0"><i class="bi bi-check2-circle me-1"></i>${esc(s.status)}</span>
              </div>
            </div>`).join('')}
        </div>
      </div>
    </div>
  </div>`;

  /* ── State ── */
  let allLogs       = [];
  let searchTerm    = '';
  let actionFilter  = 'All';
  let dateFilter    = '';

  /* ── Render filtered logs ── */
  function renderLogs() {
    const logEl   = document.getElementById('terminal-log');
    const emptyEl = document.getElementById('log-empty');
    const countEl = document.getElementById('log-count');

    const filtered = allLogs.filter(log => {
      const ts      = log.timestamp?.toDate?.() ?? null;
      const dateStr = ts ? ts.toISOString().slice(0, 10) : '';

      const matchSearch = !searchTerm || [
        log.action,
        log.performedBy,
        JSON.stringify(log.details || {}),
      ].some(v => (v || '').toLowerCase().includes(searchTerm));

      const matchAction = actionFilter === 'All' || log.action === actionFilter;
      const matchDate   = !dateFilter  || dateStr === dateFilter;

      return matchSearch && matchAction && matchDate;
    });

    countEl.innerHTML = `<i class="bi bi-list-ul me-1"></i>${filtered.length} entr${filtered.length === 1 ? 'y' : 'ies'}`;
    updateFilterTags();

    if (filtered.length === 0) {
      logEl.style.display   = 'none';
      emptyEl.style.display = 'block';
      return;
    }

    logEl.style.display   = 'block';
    emptyEl.style.display = 'none';

    const detailMax = 320;
    logEl.innerHTML =
      filtered
        .map(log => {
          const ts = log.timestamp?.toDate?.();
          const timeStr = ts ? ts.toLocaleString() : '—';
          const detailsRaw = log.details || {};
          const detailsPreview = formatLogDetailsPreview(detailsRaw, detailMax);
          const detailsFull = JSON.stringify(detailsRaw);
          const badgeColor = actionBadgeColor(log.action);
          const ic = actionLogIcon(log.action);

          return `<div class="log-row">
        <span class="log-time"><i class="bi bi-clock me-1 opacity-75"></i>[${esc(timeStr)}]</span>
        <span class="fw-badge ${badgeColor}" style="font-size:.58rem;white-space:nowrap;max-width:100%;overflow:hidden;text-overflow:ellipsis;" title="${esc(log.action || '')}"><i class="bi ${ic} me-1"></i>${esc(log.action || '—')}</span>
        <span class="log-user" title="${auditTitleAttr(log.performedBy, 200)}"><i class="bi bi-person me-1 opacity-75"></i>${esc(log.performedBy || '—')}</span>
        <span class="log-ip" title="${auditTitleAttr(detailsFull, 1800)}"><i class="bi bi-braces me-1 opacity-75"></i>${esc(detailsPreview)}</span>
      </div>`;
        })
        .join('') + '<span class="animate-pulse text-muted mt-2 d-block ps-1"><i class="bi bi-chevron-right"></i></span>';
  }

  /* ── Badge color per action type ── */
  function actionBadgeColor(action) {
    if (!action) return '';
    if (action.includes('FAILED') || action.includes('DELETED') || action.includes('BAN'))
      return 'badge-banned';
    if (action.includes('LOGIN') || action.includes('LOGOUT') || action.includes('RESET'))
      return 'badge-suspended';
    if (action.includes('CREATED') || action.includes('TOGGLED') || action.includes('FEATURED'))
      return 'badge-active';
    return 'badge-user';
  }

  /* ── Active filter tags ── */
  function updateFilterTags() {
    const tagsEl = document.getElementById('active-filters');
    const tags   = [];

    if (searchTerm)               tags.push(`Search: "${searchTerm}"`);
    if (actionFilter !== 'All')   tags.push(`Action: ${actionFilter}`);
    if (dateFilter)               tags.push(`Date: ${dateFilter}`);

    if (tags.length === 0) {
      tagsEl.style.display = 'none';
      return;
    }

    tagsEl.style.display = 'flex';
    tagsEl.innerHTML = tags
      .map(t => {
        const icon = t.startsWith('Search')
          ? 'bi-search'
          : t.startsWith('Date')
            ? 'bi-calendar3'
            : t.startsWith('Action')
              ? 'bi-tag'
              : 'bi-funnel-fill';
        return `<span class="fw-badge badge-ai" style="font-size:.7rem;padding:.3rem .6rem;">
        <i class="bi ${icon} me-1" style="font-size:.6rem;"></i>${esc(t)}
      </span>`;
      })
      .join('');
  }

  /* ── Export to CSV ── */
  function exportCSV() {
    const filtered = allLogs.filter(log => {
      const ts      = log.timestamp?.toDate?.() ?? null;
      const dateStr = ts ? ts.toISOString().slice(0, 10) : '';
      const matchSearch = !searchTerm || [log.action, log.performedBy, JSON.stringify(log.details||{})].some(v => (v||'').toLowerCase().includes(searchTerm));
      const matchAction = actionFilter === 'All' || log.action === actionFilter;
      const matchDate   = !dateFilter  || dateStr === dateFilter;
      return matchSearch && matchAction && matchDate;
    });

    const rows = [
      ['Timestamp', 'Action', 'Performed By', 'UID', 'Details'],
      ...filtered.map(log => {
        const ts = log.timestamp?.toDate?.()?.toISOString() ?? '—';
        return [
          ts,
          log.action || '—',
          log.performedBy || '—',
          log.uid || '—',
          JSON.stringify(log.details || {}),
        ].map(v => `"${String(v).replace(/"/g, '""')}"`);
      })
    ];

    const csv  = rows.map(r => r.join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url  = URL.createObjectURL(blob);
    const a    = document.createElement('a');
    a.href     = url;
    a.download = `flipwise-audit-${new Date().toISOString().slice(0,10)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
    showToast('Audit log exported as CSV.');
  }

  /* ── Event listeners ── */
  document.getElementById('log-search').addEventListener('input', e => {
    searchTerm = e.target.value.trim().toLowerCase();
    renderLogs();
  });

  document.getElementById('log-action-filter').addEventListener('change', e => {
    actionFilter = e.target.value;
    renderLogs();
  });

  document.getElementById('log-date-filter').addEventListener('change', e => {
    dateFilter = e.target.value;
    renderLogs();
  });

  document.getElementById('log-clear-btn').addEventListener('click', () => {
    searchTerm   = '';
    actionFilter = 'All';
    dateFilter   = '';
    document.getElementById('log-search').value        = '';
    document.getElementById('log-action-filter').value = 'All';
    document.getElementById('log-date-filter').value   = '';
    renderLogs();
  });

  document.getElementById('export-log-btn').addEventListener('click', exportCSV);

  /* ── Live Firestore listener ── */
  const unsub = db.collection('auditLogs')
    .orderBy('timestamp', 'desc')
    .limit(200)
    .onSnapshot(snap => {
      allLogs = snap.docs.map(d => ({ id: d.id, ...d.data() }));
      renderLogs();
    }, err => {
      document.getElementById('terminal-log').innerHTML =
        `<span class="text-danger"><i class="bi bi-exclamation-triangle me-2"></i>Error loading logs: ${esc(err.message)}</span>`;
    });

  pageCleanups.push(unsub);
}
