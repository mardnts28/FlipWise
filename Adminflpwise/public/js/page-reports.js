function renderReports(container) {
  container.innerHTML = `
  <div class="page-header mb-4">
    <h2>Reports & Feedback</h2>
    <p>Central inbox for user support, bug reports, and community moderation alerts.</p>
  </div>
  <div class="row g-3 mb-4">
    ${statCardHTML('Open Tickets','…','text-primary','','','Needs admin replies')}
    ${statCardHTML('Urgent Reports','…','text-danger','Priority','','')}
    ${statCardHTML('Avg Response','1.6h','text-success','','','Last 7 days')}
  </div>
  <div class="fw-tabs mb-4 d-flex">
    <button class="fw-tab active" data-rtab="feedback">User Feedback</button>
    <button class="fw-tab" data-rtab="reports">Flagged Content / Reports</button>
  </div>
  <div id="reports-content">
    <div class="text-center py-5 text-muted"><div class="spinner-border spinner-border-sm"></div></div>
  </div>`;

  let activeRTab = 'feedback', feedbackItems = [], reportQueue = [];
  let unsubF, unsubR;

  unsubF = db.collection('feedback').orderBy('createdAt','desc').onSnapshot(snap => {
    feedbackItems = snap.docs.map(d => ({ id: d.id, ...d.data() }));
    container.querySelectorAll('.stat-value')[0].textContent = feedbackItems.length;
    if (activeRTab === 'feedback') renderFeedback();
  });
  unsubR = db.collection('reports').orderBy('createdAt','desc').onSnapshot(snap => {
    reportQueue = snap.docs.map(d => ({ id: d.id, ...d.data() }));
    container.querySelectorAll('.stat-value')[1].textContent = reportQueue.filter(r => r.priority === 'Urgent').length;
    if (activeRTab === 'reports') renderReportQueue();
  });
  pageCleanups.push(() => { unsubF && unsubF(); unsubR && unsubR(); });

  container.querySelector('.fw-tabs').addEventListener('click', e => {
    const btn = e.target.closest('.fw-tab');
    if (!btn) return;
    container.querySelectorAll('.fw-tab').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    activeRTab = btn.dataset.rtab;
    if (activeRTab === 'feedback') renderFeedback();
    if (activeRTab === 'reports')  renderReportQueue();
  });

  function priorityClass(p) {
    if (p === 'Urgent') return 'badge-banned';
    if (p === 'High')   return 'badge-suspended';
    return 'badge-user';
  }

  function renderFeedback() {
    const el = document.getElementById('reports-content');
    if (feedbackItems.length === 0) {
      el.innerHTML = `<div class="fw-card text-muted small">No open feedback items right now.</div>`;
      return;
    }
    el.innerHTML = feedbackItems.map(item => `
    <div class="fw-card mb-3 d-flex justify-content-between align-items-center flex-wrap gap-3">
      <div>
        <div class="d-flex align-items-center gap-2 mb-1 flex-wrap">
          <span class="text-muted" style="font-size:.65rem;font-weight:800;text-transform:uppercase;">${esc(item.type||'—')}</span>
          <span class="fw-badge ${priorityClass(item.priority)}">${esc(item.priority||'—')}</span>
        </div>
        <div class="fw-bold" style="color:var(--ink);">${esc(item.subject||'—')}</div>
        <div class="text-muted small">From ${esc(item.user||'—')} · ${esc(item.time||'—')}</div>
      </div>
      <div class="d-flex gap-2">
        <button class="btn btn-ghost px-3 py-2 small fw-semibold"
                onclick="openFeedbackModal('open','${esc(item.id)}','${esc(item.subject||'')}','${esc(item.type||'')}','${esc(item.priority||'')}','${esc(item.user||'')}','${esc(item.time||'')}')">Open</button>
        <button class="btn btn-grape px-3 py-2 small fw-semibold"
                onclick="openFeedbackModal('reply','${esc(item.id)}','${esc(item.subject||'')}','${esc(item.type||'')}','${esc(item.priority||'')}','${esc(item.user||'')}','${esc(item.time||'')}')">Reply</button>
      </div>
    </div>`).join('');
  }

  function renderReportQueue() {
    const el = document.getElementById('reports-content');
    if (reportQueue.length === 0) {
      el.innerHTML = `<div class="fw-card text-muted small">No flagged reports.</div>`;
      return;
    }
    el.innerHTML = `
    <div class="fw-card p-0 overflow-hidden">
      <div class="table-responsive">
        <table class="fw-table">
          <thead><tr>
            <th>Reported Item</th><th>Reason</th><th>Community</th><th>Status</th><th class="text-end">Review</th>
          </tr></thead>
          <tbody>
          ${reportQueue.map(item => `
            <tr>
              <td class="fw-semibold" style="color:var(--ink);">${esc(item.target||'—')}</td>
              <td>
                <div class="text-muted small">${esc(item.reason||'—')}</div>
                <div class="fw-badge badge-suspended mt-1" style="font-size:.6rem;">${esc(item.priority||'Normal')} priority</div>
              </td>
              <td class="text-muted small">${esc(item.reporter||'—')}</td>
              <td><span class="fw-badge badge-user">${esc(item.status||'Pending')}</span></td>
              <td class="text-end">
                <button class="btn btn-ghost px-3 py-2 small fw-semibold"
                        onclick="markReportUnderReview('${esc(item.id)}')">Review</button>
              </td>
            </tr>`).join('')}
          </tbody>
        </table>
      </div>
    </div>`;
  }

  renderFeedback();
}

window.openFeedbackModal = (mode, id, subject, type, priority, user, time) => {
  const isReply = mode === 'reply';
  document.getElementById('feedback-modal-content').innerHTML = `
  <div class="d-flex justify-content-between align-items-start mb-4">
    <div>
      <h3 class="fw-bold mb-1" style="font-size:1.2rem;color:var(--ink);">${isReply ? 'Reply to Feedback' : 'Feedback Details'}</h3>
      <p class="text-muted small mb-0">${esc(subject)} · ${esc(user)}</p>
    </div>
    <button class="btn btn-link text-muted p-0" onclick="document.getElementById('feedback-modal').style.display='none'">
      <i class="bi bi-x-lg fs-5"></i>
    </button>
  </div>
  <div class="p-3 rounded-3 mb-3" style="background:var(--surface);border:1px solid var(--border);">
    <div class="fw-label mb-2">Ticket Summary</div>
    <div class="small text-muted">Type: ${esc(type)}</div>
    <div class="small text-muted">Priority: ${esc(priority)}</div>
    <div class="small text-muted">Submitted: ${esc(time)}</div>
  </div>
  ${!isReply ? `
    <label class="fw-label">Issue Details</label>
    <textarea class="fw-input" readonly style="height:100px;">${esc(subject)}

Reported by ${esc(user)}. Please review and respond.</textarea>` : `
    <label class="fw-label">Reply Message</label>
    <textarea class="fw-input" id="reply-textarea" style="height:100px;" placeholder="Write your response…"></textarea>`}
  <div class="d-flex justify-content-end gap-2 mt-4">
    <button class="btn btn-ghost px-4 py-2" onclick="document.getElementById('feedback-modal').style.display='none'">Close</button>
    <button class="btn btn-grape px-4 py-2 fw-semibold" onclick="submitFeedback('${esc(id)}','${isReply}')">
      ${isReply ? 'Send Reply' : 'Mark as Reviewed'}
    </button>
  </div>`;
  document.getElementById('feedback-modal').style.display = 'flex';
};
window.submitFeedback = async (id, isReply) => {
  if (isReply === 'true') {
    await db.collection('feedback').doc(id).update({ status: 'Resolved' });
    await logAudit('FEEDBACK_REPLIED', { feedbackId: id });
    showToast('Reply sent and ticket resolved.');
  } else {
    await db.collection('feedback').doc(id).update({ status: 'Reviewed' });
    showToast('Marked as reviewed.');
  }
  document.getElementById('feedback-modal').style.display = 'none';
};
window.markReportUnderReview = async (id) => {
  await db.collection('reports').doc(id).update({ status: 'Under Review' });
  await logAudit('REPORT_REVIEWED', { reportId: id });
  showToast('Report marked Under Review.');
};
