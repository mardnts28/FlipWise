const AI_SETTINGS_DOC = 'platformSettings';
const DEFAULT_PROMPT =
  'You are FlipWise AI. Generate high-quality, active-recall flashcards from the provided text.';

function aiFreeBarPct(value) {
  const v = Math.min(20, Math.max(0, Number(value) || 0));
  return (v / 20) * 100;
}

function aiPremiumBarPct(value) {
  const v = Math.min(200, Math.max(20, Number(value) || 20));
  return ((v - 20) / (200 - 20)) * 100;
}

function syncFreeQuotaUi(value) {
  const elVal = document.getElementById('free-quota-val');
  const elFill = document.getElementById('free-rate-fill');
  if (elVal) elVal.textContent = `${value} gens/day`;
  if (elFill) elFill.style.width = `${aiFreeBarPct(value)}%`;
}

function syncPremiumQuotaUi(value) {
  const elVal = document.getElementById('prem-quota-val');
  const elFill = document.getElementById('prem-rate-fill');
  if (elVal) elVal.textContent = `${value} gens/day`;
  if (elFill) elFill.style.width = `${aiPremiumBarPct(value)}%`;
}

async function persistAiSettings(patch) {
  await db
    .collection('config')
    .doc(AI_SETTINGS_DOC)
    .set(
      {
        ...patch,
        updatedAt: firebase.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
}

function renderAI(container) {
  let freeQuota = 5;
  let premiumQuota = 50;
  let promptText = DEFAULT_PROMPT;

  container.innerHTML = `
  <div class="page-header mb-4">
    <h2>Gemini AI Dashboard</h2>
    <p class="text-muted mb-0">Tune generation quotas and the default system instruction.</p>
  </div>
  <div class="row g-3 mb-4">
    ${statCardHTML('Total API Calls', '—', 'text-primary', '', '', 'Connect usage export for live totals')}
    ${statCardHTML('Token Usage', '—', 'text-grape', '', '', 'Est. cost not available in-console')}
    ${statCardHTML('Avg Latency', '—', 'text-success', '', '', 'Depends on model & region')}
  </div>
  <div class="row g-4">
    <div class="col-lg-6">
      <div class="fw-card">
        <div class="d-flex align-items-center gap-2 mb-4">
          <i class="bi bi-speedometer2 text-grape fs-5" aria-hidden="true"></i>
          <h5 class="fw-bold mb-0" style="color:var(--ink);">Rate Limiting &amp; Quotas</h5>
        </div>
        <div class="mb-4">
          <div class="d-flex justify-content-between mb-2">
            <label class="fw-label mb-0" for="free-quota-range">Free Tier Limit</label>
            <span class="fw-semibold small" style="color:var(--grape);" id="free-quota-val">${freeQuota} gens/day</span>
          </div>
          <input type="range" class="form-range" min="0" max="20" value="${freeQuota}" id="free-quota-range" />
          <div class="rate-bar mt-2"><div class="rate-fill" style="width:${aiFreeBarPct(freeQuota)}%;" id="free-rate-fill"></div></div>
        </div>
        <div class="mb-4">
          <div class="d-flex justify-content-between mb-2">
            <label class="fw-label mb-0" for="prem-quota-range">Premium Tier Limit</label>
            <span class="fw-semibold small" style="color:var(--grape);" id="prem-quota-val">${premiumQuota} gens/day</span>
          </div>
          <input type="range" class="form-range" min="20" max="200" value="${premiumQuota}" id="prem-quota-range" />
          <div class="rate-bar mt-2"><div class="rate-fill" style="width:${aiPremiumBarPct(premiumQuota)}%;" id="prem-rate-fill"></div></div>
        </div>
        <button type="button" class="btn btn-ghost w-100 py-2 fw-semibold" id="ai-save-quotas-btn">Apply Quota Changes</button>
      </div>
    </div>
    <div class="col-lg-6">
      <div class="fw-card">
        <div class="d-flex align-items-center justify-content-between mb-4">
          <div class="d-flex align-items-center gap-2">
            <i class="bi bi-chat-left-text text-grape fs-5" aria-hidden="true"></i>
            <h5 class="fw-bold mb-0" style="color:var(--ink);">Prompt Management</h5>
          </div>
          <span class="fw-badge badge-active">LIVE</span>
        </div>
        <label class="fw-label" for="prompt-textarea">System Instruction (Generator)</label>
        <textarea id="prompt-textarea" class="fw-input" style="font-family:var(--font-mono);font-size:.78rem;height:140px;">${esc(promptText)}</textarea>
        <div class="d-flex gap-2 mt-3">
          <button type="button" class="btn btn-grape flex-grow-1 py-2 fw-semibold" id="ai-update-prompt-btn">Update System Prompt</button>
          <button type="button" class="btn btn-ghost px-3 py-2" id="ai-prompt-history-btn" title="Audit trail">
            <i class="bi bi-clock-history" aria-hidden="true"></i>
          </button>
        </div>
      </div>
    </div>
  </div>`;

  const freeRange = container.querySelector('#free-quota-range');
  const premRange = container.querySelector('#prem-quota-range');
  const saveQuotasBtn = container.querySelector('#ai-save-quotas-btn');
  const updatePromptBtn = container.querySelector('#ai-update-prompt-btn');
  const historyBtn = container.querySelector('#ai-prompt-history-btn');

  freeRange.addEventListener('input', () => syncFreeQuotaUi(freeRange.value));
  premRange.addEventListener('input', () => syncPremiumQuotaUi(premRange.value));

  saveQuotasBtn.addEventListener('click', async () => {
    const fq = Number(freeRange.value);
    const pq = Number(premRange.value);
    if (pq < fq) {
      showToast('Premium limit should be at least the free tier limit.', 'warning');
      return;
    }
    const orig = saveQuotasBtn.textContent;
    saveQuotasBtn.disabled = true;
    saveQuotasBtn.textContent = 'Saving…';
    try {
      await persistAiSettings({ aiFreeQuotaPerDay: fq, aiPremiumQuotaPerDay: pq });
      await logAudit('AI_QUOTA_UPDATED', { freeQuota: fq, premiumQuota: pq });
      showToast(`Quotas saved: Free ${fq}, Premium ${pq} gens/day.`);
    } catch (err) {
      console.error(err);
      showToast(err?.message || 'Could not save quotas.', 'danger');
    } finally {
      saveQuotasBtn.disabled = false;
      saveQuotasBtn.textContent = orig;
    }
  });

  updatePromptBtn.addEventListener('click', async () => {
    const txt = container.querySelector('#prompt-textarea')?.value.trim();
    if (!txt) {
      showToast('Prompt cannot be empty.', 'warning');
      return;
    }
    const orig = updatePromptBtn.textContent;
    updatePromptBtn.disabled = true;
    updatePromptBtn.textContent = 'Saving…';
    try {
      await persistAiSettings({ aiGeneratorSystemPrompt: txt });
      await logAudit('AI_PROMPT_UPDATED', { preview: txt.substring(0, 80) });
      showToast('System prompt saved to Firestore.');
    } catch (err) {
      console.error(err);
      showToast(err?.message || 'Could not save prompt.', 'danger');
    } finally {
      updatePromptBtn.disabled = false;
      updatePromptBtn.textContent = orig;
    }
  });

  historyBtn.addEventListener('click', () => {
    showToast('Prompt edits appear in System Logs as AI_PROMPT_UPDATED.', 'info');
  });

  db.collection('config')
    .doc(AI_SETTINGS_DOC)
    .get()
    .then(snap => {
      const d = snap.data();
      if (!d) return;
      if (typeof d.aiFreeQuotaPerDay === 'number') {
        const v = Math.min(20, Math.max(0, Math.round(d.aiFreeQuotaPerDay)));
        freeRange.value = String(v);
        syncFreeQuotaUi(v);
      }
      if (typeof d.aiPremiumQuotaPerDay === 'number') {
        const v = Math.min(200, Math.max(20, Math.round(d.aiPremiumQuotaPerDay)));
        premRange.value = String(v);
        syncPremiumQuotaUi(v);
      }
      if (typeof d.aiGeneratorSystemPrompt === 'string' && d.aiGeneratorSystemPrompt.trim()) {
        container.querySelector('#prompt-textarea').value = d.aiGeneratorSystemPrompt;
      }
    })
    .catch(err => {
      console.error(err);
      showToast('Could not load AI settings from Firestore.', 'warning');
    });
}
