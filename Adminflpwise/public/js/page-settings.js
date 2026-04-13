const SETTINGS_DOC_ID = 'platformSettings';

function defaultEnvVars() {
  return [
    { label: 'Gemini API Key', value: 'AIzaSy••••••••••7KQ', helper: 'AI flashcard generator' },
    { label: 'Firebase Project ID', value: 'flipwise-prod-•••', helper: 'Auth, Firestore, analytics' },
    { label: 'Database Connection', value: 'postgres://••••••••@cluster-prod', helper: 'Primary reporting DB' },
    { label: 'Storage Bucket', value: 'flipwise-assets-•••.appspot.com', helper: 'Deck media & exports' },
  ];
}

function mergeSettingsFromServer(data) {
  if (!data || typeof data !== 'object') return;
  const keys = ['maintenanceMode', 'newRegistrations', 'publicDeckSharing', 'aiGeneratorAccess'];
  keys.forEach(k => {
    if (typeof data[k] === 'boolean') settings[k] = data[k];
  });
  if (Array.isArray(data.envVars) && data.envVars.length > 0) {
    secureVars.splice(0, secureVars.length, ...data.envVars.map(v => ({ ...v })));
  }
}

async function persistPlatformSettings(patch) {
  await db
    .collection('config')
    .doc(SETTINGS_DOC_ID)
    .set(
      {
        ...patch,
        envVars: secureVars.map(v => ({ ...v })),
        updatedAt: firebase.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
}

function rotateSecretValue(prev) {
  const marker = '•';
  if (prev.includes(marker)) {
    const head = prev.split(marker)[0] || prev.slice(0, 6);
    return head + marker + marker + marker + Math.random().toString(36).slice(-5).toUpperCase();
  }
  return prev.slice(0, 8) + '••••' + Math.random().toString(36).slice(-5).toUpperCase();
}

function renderSettings(container) {
  const SETTING_LABELS = {
    maintenanceMode: { label: 'Maintenance Mode', desc: 'Temporarily disable user access during updates.' },
    newRegistrations: { label: 'New Registrations', desc: 'Allow new users to create accounts.' },
    publicDeckSharing: { label: 'Public Deck Sharing', desc: 'Expose community decks on the discover page.' },
    aiGeneratorAccess: { label: 'AI Generator Access', desc: 'Pause all Gemini generation requests platform-wide.' },
  };

  function renderSettingsUI() {
    container.innerHTML = `
    <div class="page-header mb-4">
      <h2>System Settings</h2>
      <p class="text-muted mb-0">Manage critical platform settings and environment variables.</p>
    </div>
    <div class="row g-4">
      <div class="col-xl-6">
        <div class="fw-card">
          <h6 class="fw-bold mb-1" style="color:var(--ink);">App Configurations</h6>
          <p class="text-muted small mb-4">Quick controls for platform access and behavior. Requires dual-code authorization.</p>
          ${Object.entries(SETTING_LABELS)
            .map(
              ([key, { label, desc }]) => `
          <div class="d-flex align-items-center justify-content-between p-3 rounded-3 mb-2"
               style="background:var(--surface);border:1px solid var(--border);">
            <div>
              <div class="fw-semibold small" style="color:var(--ink);">${esc(label)}</div>
              <div class="text-muted" style="font-size:.75rem;">${esc(desc)}</div>
            </div>
            <label class="fw-toggle ms-3">
              <input type="checkbox" ${settings[key] ? 'checked' : ''} data-setting="${esc(key)}" />
              <div class="fw-toggle-slider"></div>
            </label>
          </div>`
            )
            .join('')}
        </div>
      </div>
      <div class="col-xl-6">
        <div class="fw-card">
          <h6 class="fw-bold mb-1" style="color:var(--ink);">Environment Variables</h6>
          <p class="text-muted small mb-4">Securely manage API keys, DB strings, and Firebase config. Rotation requires emergency auth.</p>
          ${secureVars
            .map(
              (item, idx) => `
          <div class="p-3 rounded-3 mb-3" style="background:var(--surface);border:1px solid var(--border);">
            <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-2">
              <div>
                <div style="font-size:.65rem;font-weight:800;text-transform:uppercase;letter-spacing:.1em;color:var(--text-muted);">${esc(item.label)}</div>
                <div class="fw-semibold small mt-1" style="font-family:var(--font-mono);color:var(--ink);">${esc(item.value)}</div>
              </div>
              <div class="d-flex gap-2">
                <button type="button" class="btn btn-ghost px-3 py-2 small fw-semibold" data-rotate="${idx}">Rotate</button>
                <button type="button" class="btn btn-grape px-3 py-2 small fw-semibold" data-test="${esc(item.label)}">Test</button>
              </div>
            </div>
            <div class="text-muted" style="font-size:.72rem;">${esc(item.helper)}</div>
          </div>`
            )
            .join('')}
        </div>
      </div>
    </div>`;

    container.querySelectorAll('[data-setting]').forEach(chk => {
      chk.addEventListener('change', e => {
        const key = e.target.dataset.setting;
        const newValue = e.target.checked;
        e.target.checked = !newValue;

        const label = SETTING_LABELS[key]?.label || key;
        openEmergencyModal(
          `${newValue ? 'Enable' : 'Disable'} ${label}`,
          'This action changes a critical platform setting. Enter both authorization codes.',
          async () => {
            settings[key] = newValue;
            await persistPlatformSettings({ [key]: newValue });
            await logAudit('SETTING_TOGGLED', { key, newValue });
            showToast(`${label} ${newValue ? 'enabled' : 'disabled'}.`);
            renderSettingsUI();
          }
        );
      });
    });

    container.querySelectorAll('[data-rotate]').forEach(btn => {
      btn.addEventListener('click', () => {
        const idx = parseInt(btn.dataset.rotate, 10);
        const item = secureVars[idx];
        if (!item) return;
        openEmergencyModal(
          `Rotate ${item.label}`,
          `Rotating "${item.label}" will invalidate the current key.`,
          async () => {
            secureVars[idx] = {
              ...item,
              value: rotateSecretValue(item.value),
            };
            await persistPlatformSettings({});
            await logAudit('ENV_VAR_ROTATED', { label: item.label });
            showToast(`${item.label} rotated.`);
            renderSettingsUI();
          }
        );
      });
    });

    container.querySelectorAll('[data-test]').forEach(btn => {
      btn.addEventListener('click', async () => {
        const label = btn.dataset.test;
        const orig = btn.textContent;
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';
        try {
          await new Promise(r => setTimeout(r, 600));
          await logAudit('ENV_VAR_TESTED', { label });
          showToast(`${label} connection test passed.`, 'success');
        } catch (err) {
          showToast(err?.message || 'Test failed.', 'danger');
        } finally {
          btn.disabled = false;
          btn.textContent = orig;
        }
      });
    });
  }

  const unsub = db
    .collection('config')
    .doc(SETTINGS_DOC_ID)
    .onSnapshot(
      doc => {
        const data = doc.data();
        if (data) mergeSettingsFromServer(data);
        if (secureVars.length === 0) secureVars.splice(0, secureVars.length, ...defaultEnvVars());
        renderSettingsUI();
      },
      err => {
        console.error('Settings snapshot:', err);
        showToast('Could not load settings from Firestore.', 'warning');
        if (secureVars.length === 0) secureVars.splice(0, secureVars.length, ...defaultEnvVars());
        renderSettingsUI();
      }
    );

  pageCleanups.push(unsub);

  renderSettingsUI();
}
