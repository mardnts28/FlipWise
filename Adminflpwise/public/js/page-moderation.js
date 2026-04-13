function renderModeration(container) {
  container.innerHTML = `
  <div class="page-header mb-4">
    <h2>Content & Quality</h2>
    <p>Manage user-created and AI-generated decks, review public reports, and feature standout content.</p>
  </div>
  <div class="fw-tabs mb-4 d-flex">
    ${['decks','queue','featured'].map((k,i) =>
      `<button class="fw-tab${i===0?' active':''}" data-tab="${k}">${k==='decks'?'Decks':k==='queue'?'Public Queue':'Featured'}</button>`
    ).join('')}
  </div>
  <div id="mod-content">
    <div class="text-center py-5 text-muted"><div class="spinner-border spinner-border-sm me-2"></div>Loading…</div>
  </div>`;

  let activeTab = 'decks', allDecks = [], flaggedQueue = [];
  let unsubDecks, unsubQueue;

  unsubDecks = db.collection('decks').orderBy('createdAt','desc').onSnapshot(snap => {
    allDecks = snap.docs.map(d => ({ id: d.id, ...d.data() }));
    if (activeTab === 'decks') renderDecksTab();
  }, err => console.error(err));

  unsubQueue = db.collection('flaggedDecks').orderBy('reports','desc').onSnapshot(snap => {
    flaggedQueue = snap.docs.map(d => ({ id: d.id, ...d.data() }));
    if (activeTab === 'queue') renderQueueTab();
  }, err => console.error(err));

  pageCleanups.push(() => { unsubDecks && unsubDecks(); unsubQueue && unsubQueue(); });

  container.querySelector('.fw-tabs').addEventListener('click', e => {
    const btn = e.target.closest('.fw-tab');
    if (!btn) return;
    container.querySelectorAll('.fw-tab').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    activeTab = btn.dataset.tab;
    if (activeTab === 'decks')    renderDecksTab();
    if (activeTab === 'queue')    renderQueueTab();
    if (activeTab === 'featured') renderFeaturedTab();
  });

  function renderDecksTab() {
    document.getElementById('mod-content').innerHTML = `
    <div class="fw-card p-0 overflow-hidden">
      <div class="table-responsive">
        <table class="fw-table">
          <thead><tr>
            <th>Deck Info</th><th>Creator</th><th>Source</th><th>Visibility</th><th class="text-end">Action</th>
          </tr></thead>
          <tbody>
          ${allDecks.length === 0
            ? '<tr><td colspan="5" class="text-center text-muted py-4">No decks found in Firestore.</td></tr>'
            : allDecks.map(d => `
              <tr>
                <td>
                  <div class="fw-semibold" style="color:var(--ink);">${esc(d.title||'Untitled')}</div>
                  <div class="text-muted" style="font-size:.72rem;">${esc(d.id)} · ${esc(d.cards??0)} cards</div>
                </td>
                <td class="text-muted small">${esc(d.creator||'—')}</td>
                <td><span class="fw-badge ${d.source==='AI Generated'?'badge-ai':'badge-user'}">${esc(d.source||'User Created')}</span></td>
                <td><span class="fw-badge badge-user">${esc(d.visibility||'Public')}</span></td>
                <td class="text-end">
                  <button class="btn btn-link fw-semibold p-0" style="color:var(--grape);font-size:.85rem;"
                          onclick="showToast('Reviewing: ${esc(d.title||'Deck')}','info')">Edit / Review</button>
                </td>
              </tr>`).join('')}
          </tbody>
        </table>
      </div>
    </div>`;
  }

  function renderQueueTab() {
    const el = document.getElementById('mod-content');
    if (flaggedQueue.length === 0) {
      el.innerHTML = `<div class="fw-card text-muted small">No flagged decks in the review queue.</div>`;
      return;
    }
    el.innerHTML = flaggedQueue.map(item => `
    <div class="fw-card mb-3 d-flex justify-content-between align-items-center flex-wrap gap-3"
         style="border-left:4px solid var(--danger);">
      <div>
        <div class="fw-bold" style="color:var(--ink);">${esc(item.title||'—')}</div>
        <div class="text-muted small">Reported deck by ${esc(item.creator||'—')}</div>
        <div class="mt-1" style="font-size:.72rem;color:var(--danger);font-weight:700;text-transform:uppercase;">
          ${esc(item.reason||'—')} · ${esc(item.reports||0)} Reports
        </div>
      </div>
      <div class="d-flex gap-2">
        <button class="btn btn-ghost px-3 py-2 small fw-semibold" onclick="dismissFlag('${esc(item.id)}')">Dismiss</button>
        <button class="btn btn-danger px-3 py-2 small fw-semibold"
                onclick="deleteFlaggedDeck('${esc(item.title||'')}','${esc(item.id)}','${esc(item.deckId||'')}')">Delete Deck</button>
      </div>
    </div>`).join('');
  }

  function renderFeaturedTab() {
    document.getElementById('mod-content').innerHTML = `
    <div class="fw-card text-center p-5">
      <div style="font-size:2.5rem;margin-bottom:1rem;">🌟</div>
      <h5 class="fw-bold mb-2" style="color:var(--ink);">Discovery Page Manager</h5>
      <p class="text-muted small mb-4">Pin high-quality decks to appear on the app's discover page.</p>
      <div class="d-flex gap-2 justify-content-center">
        <input type="text" id="feature-deck-id" class="fw-input" style="max-width:280px;" placeholder="Firestore Deck ID" />
        <button class="btn btn-grape px-4 py-2 fw-semibold" onclick="featureDeck()">Feature Deck</button>
      </div>
    </div>`;
  }

  renderDecksTab();
}

window.dismissFlag = async (id) => {
  await db.collection('flaggedDecks').doc(id).delete();
  await logAudit('FLAG_DISMISSED', { flagId: id });
  showToast('Flag dismissed.');
};
window.deleteFlaggedDeck = async (title, qid, did) => {
  if (!confirm(`Delete "${title}"?`)) return;
  const batch = db.batch();
  batch.delete(db.collection('flaggedDecks').doc(qid));
  if (did) batch.delete(db.collection('decks').doc(did));
  await batch.commit();
  await logAudit('DECK_DELETED', { title, deckId: did });
  showToast(`"${title}" deleted.`, 'warning');
};
window.featureDeck = async () => {
  const id   = document.getElementById('feature-deck-id')?.value.trim();
  if (!id) return showToast('Enter a Deck ID.','warning');
  const snap = await db.collection('decks').doc(id).get();
  if (!snap.exists) return showToast('Deck ID not found.','danger');
  await db.collection('decks').doc(id).update({ featured: true });
  await logAudit('DECK_FEATURED', { deckId: id });
  showToast(`Deck "${snap.data().title||id}" is now featured!`);
};
