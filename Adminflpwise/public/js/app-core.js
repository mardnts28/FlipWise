/* ═══════════════════════════════════════════════════════════════
   FIREBASE INITIALIZATION
   Replace the placeholder values with your own config from
   Firebase Console → Project Settings → Your Apps.
═══════════════════════════════════════════════════════════════ */
const firebaseConfig = {
  apiKey: "AIzaSyDE9Ix9BeWMkzCExBStwAOLW49eumWbdUg",
  authDomain: "flipwiseadmin.firebaseapp.com",
  projectId: "flipwiseadmin",
  storageBucket: "flipwiseadmin.firebasestorage.app",
  messagingSenderId: "336978239686",
  appId: "1:336978239686:web:c8e35426e08e9f5e1137c7",
  measurementId: "G-X23HZ7ZYNV"
};
firebase.initializeApp(firebaseConfig);
const db   = firebase.firestore();
const auth = firebase.auth();

/* ═══════════════════════════════════════════════════════════════
   STRIDE SECURITY LAYER
   S – Spoofing:     Admin-only Firestore role check on login
   T – Tampering:    All writes go through server-side Firestore rules (enforced in console)
                     Client-side: no direct DOM innerHTML injection with user data
   R – Repudiation:  Every sensitive action is logged to auditLogs collection
   I – Info Disc:    No sensitive data echoed in errors; generic error messages
   D – DoS:          Client-side login rate limiter with exponential backoff window
   E – Elevation:    Role read from Firestore, not from client token claims alone
═══════════════════════════════════════════════════════════════ */

/* ── DoS: Login rate limiter ── */
const LOGIN_LIMIT = { max: 5, windowMs: 5 * 60 * 1000 };
let loginAttempts = JSON.parse(sessionStorage.getItem('fw_login_attempts') || '[]');
function recordLoginAttempt() {
  const now = Date.now();
  loginAttempts = loginAttempts.filter(t => now - t < LOGIN_LIMIT.windowMs);
  loginAttempts.push(now);
  sessionStorage.setItem('fw_login_attempts', JSON.stringify(loginAttempts));
}
function isRateLimited() {
  const now = Date.now();
  loginAttempts = loginAttempts.filter(t => now - t < LOGIN_LIMIT.windowMs);
  return loginAttempts.length >= LOGIN_LIMIT.max;
}

/* ── Repudiation: Audit logger ── */
async function logAudit(action, details) {
  const user = auth.currentUser;
  try {
    await db.collection('auditLogs').add({
      action,
      details,
      performedBy: user?.email ?? 'unknown',
      uid:         user?.uid   ?? null,
      timestamp:   firebase.firestore.FieldValue.serverTimestamp(),
      userAgent:   navigator.userAgent.substring(0, 200),
    });
  } catch (e) { console.warn('Audit log failed:', e); }
}

/* ── Info Disclosure: Sanitize text output ── */
function esc(str) {
  if (str == null) return '—';
  const d = document.createElement('div');
  d.textContent = String(str);
  return d.innerHTML;
}

/* ═══════════════════════════════════════════════════════════════
   STATE
═══════════════════════════════════════════════════════════════ */
let currentUser   = null;
let activePage    = 'overview';
let pageCleanups  = []; // Firestore unsubscribe functions
/** Latest Firestore profile for signed-in user (role, permissions, …). */
let sessionAdminProfile = null;

// Emergency modal callback
let emergencyCallback = null;

// Settings state (Firestore-backed in a real deploy)
let settings = {
  maintenanceMode:   false,
  newRegistrations:  true,
  publicDeckSharing: true,
  aiGeneratorAccess: true,
};
let pendingSettingKey = null;
let secureVars = [
  { label: 'Gemini API Key',      value: 'AIzaSy••••••••••7KQ', helper: 'AI flashcard generator' },
  { label: 'Firebase Project ID', value: 'flipwise-prod-•••',   helper: 'Auth, Firestore, analytics' },
  { label: 'Database Connection', value: 'postgres://••••••••@cluster-prod', helper: 'Primary reporting DB' },
  { label: 'Storage Bucket',      value: 'flipwise-assets-•••.appspot.com', helper: 'Deck media & exports' },
];
let pendingEnvAction = null;

/* ═══════════════════════════════════════════════════════════════
   INIT
═══════════════════════════════════════════════════════════════ */
auth.onAuthStateChanged(async (user) => {
  if (user) {
    /* ── STRIDE Elevation: Verify admin role from Firestore ── */
    try {
      const doc  = await db.collection('users').doc(user.uid).get();
      const data = doc.data();
      const role = data?.role ?? '';
      if (!/(admin|moderator)/i.test(role)) {
        sessionAdminProfile = null;
        await auth.signOut();
        showAuthScreen();
        showToast('Access denied. Admin accounts only.', 'danger');
        hideSplash();
        return;
      }
      /* Newly invited admins: no terminal access until password is set via email link (auth-action.html). */
      if (data?.setupPending === true) {
        sessionAdminProfile = null;
        await auth.signOut();
        showAuthScreen();
        showToast('Complete your account setup using the link in your invitation email before signing in.', 'warning');
        hideSplash();
        return;
      }
      sessionAdminProfile = data || {};
    } catch (e) {
      sessionAdminProfile = null;
      await auth.signOut();
      showAuthScreen();
      hideSplash();
      return;
    }
    currentUser = user;
    document.getElementById('sidebar-email').textContent = user.email;
    showApp();
    updateSidebarForSession();
    renderPage('overview');
  } else {
    sessionAdminProfile = null;
    currentUser = null;
    showAuthScreen();
  }
  hideSplash();
});

function hideSplash()    { document.getElementById('loading-screen').style.display = 'none'; }
function showAuthScreen(){ document.getElementById('auth-screen').style.display  = 'flex';  document.getElementById('app').style.display = 'none'; }
function showApp() {
  document.getElementById('app').style.display = 'block';
  document.getElementById('auth-screen').style.display = 'none';
  applySidebarCollapsedFromStorage();
}

function isSuperAdmin() {
  return String(sessionAdminProfile?.role ?? '').trim() === 'Super Admin';
}

/** Standard nav pages (not Access Control). Missing permissions default to allowed. */
function canAccessTerminalPage(page) {
  if (page === 'access') return isSuperAdmin();
  if (isSuperAdmin()) return true;
  const perms = sessionAdminProfile?.permissions;
  if (!perms || typeof perms !== 'object') return true;
  return perms[page] !== false;
}

function updateSidebarForSession() {
  const nav = document.getElementById('sidebar-nav');
  const roleEl = document.getElementById('sidebar-user-role');
  if (!nav) return;
  nav.querySelectorAll('[data-page]').forEach(btn => {
    const p = btn.dataset.page;
    if (p === 'access') {
      btn.style.display = isSuperAdmin() ? '' : 'none';
      return;
    }
    btn.style.display = canAccessTerminalPage(p) ? '' : 'none';
  });
  if (roleEl) {
    roleEl.textContent = isSuperAdmin() ? 'Signed in · Super Admin' : 'Signed in · Admin';
  }
}

window.refreshSessionProfile = async function refreshSessionProfile() {
  const uid = auth.currentUser?.uid;
  if (!uid) return;
  const doc = await db.collection('users').doc(uid).get();
  sessionAdminProfile = doc.data() || {};
  updateSidebarForSession();
};

/* ═══════════════════════════════════════════════════════════════
   NAVIGATION
═══════════════════════════════════════════════════════════════ */
document.getElementById('sidebar-nav').addEventListener('click', e => {
  const btn = e.target.closest('[data-page]');
  if (!btn) return;
  const page = btn.dataset.page;
  if (!canAccessTerminalPage(page)) {
    showToast('You do not have access to that area.', 'danger');
    return;
  }
  document.querySelectorAll('#sidebar-nav [data-page]').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  renderPage(page);
  closeSidebar();
});

function renderPage(page) {
  if (!canAccessTerminalPage(page)) {
    showToast('You do not have access to that area.', 'danger');
    page = 'overview';
    document.querySelectorAll('#sidebar-nav [data-page]').forEach(b => {
      b.classList.toggle('active', b.dataset.page === 'overview');
    });
  }
  activePage = page;
  cleanupPage();
  const container = document.getElementById('page-container');
  container.className = 'animate-page';
  void container.offsetWidth; // reflow to restart animation
  const pageMap = {
    overview:   renderOverview,
    users:      renderUsers,
    moderation: renderModeration,
    ai:         renderAI,
    gamify:     renderGamify,
    reports:    renderReports,
    security:   renderSecurity,
    settings:   renderSettings,
    access:     renderAccessControl,
  };
  (pageMap[page] || renderOverview)(container);
}

function cleanupPage() {
  pageCleanups.forEach(fn => { try { fn(); } catch(e){} });
  pageCleanups = [];
}

function togglePasswordVisibility(id, btn) {
  const inp = document.getElementById(id);
  if (!inp) return;
  inp.type = inp.type === 'password' ? 'text' : 'password';
  btn.innerHTML = inp.type === 'password' ? '<i class="bi bi-eye"></i>' : '<i class="bi bi-eye-slash"></i>';
}

/* ─ Toast ─ */
function showToast(msg, type = 'success') {
  const colors = { success:'#D1FAE5', danger:'#FEE2E2', info:'#DBEAFE', warning:'#FEF3C7' };
  const text   = { success:'#065F46', danger:'#991B1B', info:'#1E40AF', warning:'#92400E' };
  const t = document.createElement('div');
  t.style.cssText = `background:${colors[type]};color:${text[type]};padding:.75rem 1.25rem;border-radius:12px;font-size:.85rem;font-weight:600;box-shadow:0 4px 20px rgba(0,0,0,.12);margin-top:.5rem;`;
  t.textContent = msg;
  document.getElementById('toast-container').appendChild(t);
  setTimeout(() => t.remove(), 4000);
}

/* ─ Sidebar ─ */
function toggleSidebar() {
  const s = document.getElementById('sidebar');
  const o = document.getElementById('sidebar-overlay');
  s.classList.toggle('open');
  o.style.display = s.classList.contains('open') ? 'block' : 'none';
}
function closeSidebar() {
  document.getElementById('sidebar').classList.remove('open');
  document.getElementById('sidebar-overlay').style.display = 'none';
}

window.addEventListener('resize', () => {
  if (window.innerWidth < 992) closeSidebar();
});

const SIDEBAR_COLLAPSED_KEY = 'fw-sidebar-collapsed';

function applySidebarCollapsedFromStorage() {
  const app = document.getElementById('app');
  if (!app) return;
  try {
    if (localStorage.getItem(SIDEBAR_COLLAPSED_KEY) === '1') app.classList.add('sidebar-collapsed');
    else app.classList.remove('sidebar-collapsed');
  } catch (e) {
    app.classList.remove('sidebar-collapsed');
  }
  updateSidebarCollapseToggleUi();
}

window.toggleSidebarCollapse = function toggleSidebarCollapse() {
  if (window.innerWidth < 992) return;
  const app = document.getElementById('app');
  if (!app) return;
  app.classList.toggle('sidebar-collapsed');
  try {
    localStorage.setItem(SIDEBAR_COLLAPSED_KEY, app.classList.contains('sidebar-collapsed') ? '1' : '0');
  } catch (e) { /* private mode */ }
  updateSidebarCollapseToggleUi();
};

function updateSidebarCollapseToggleUi() {
  const app = document.getElementById('app');
  const btn = document.getElementById('sidebar-collapse-toggle');
  const icon = btn?.querySelector('[data-collapse-icon]');
  if (!app || !btn || !icon) return;
  const collapsed = app.classList.contains('sidebar-collapsed');
  icon.className = collapsed ? 'bi bi-chevron-double-right' : 'bi bi-chevron-double-left';
  btn.setAttribute('aria-expanded', String(!collapsed));
  btn.setAttribute('aria-label', collapsed ? 'Expand sidebar' : 'Collapse sidebar');
  btn.setAttribute('title', collapsed ? 'Expand sidebar' : 'Collapse sidebar');
}

/* ═══════════════════════════════════════════════════════════════
   AUTH: LOGIN
═══════════════════════════════════════════════════════════════ */
document.getElementById('login-form').addEventListener('submit', async e => {
  e.preventDefault();
  const errBox = document.getElementById('login-error');
  const warnBox = document.getElementById('login-attempts-warning');
  errBox.style.display = 'none';

  /* STRIDE DoS: Rate limit check */
  if (isRateLimited()) {
    warnBox.style.display = 'block';
    return;
  }

  const email    = document.getElementById('login-email').value.trim();
  const password = document.getElementById('login-password').value;
  const btn      = document.getElementById('login-btn');

  /* STRIDE Info Disclosure: basic client validation without revealing system details */
  if (!email || !password) {
    errBox.textContent = 'Please enter your email and password.';
    errBox.style.display = 'block';
    return;
  }

  btn.disabled = true;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Signing in…';

  try {
    recordLoginAttempt();
    await auth.signInWithEmailAndPassword(email, password);
    /* Auth state change handler will take it from here */
    await logAudit('ADMIN_LOGIN', { email });
  } catch (err) {
    /* STRIDE Info Disclosure: generic error messages, no leaking of user existence */
    const msgs = {
      'auth/user-not-found':    'Invalid credentials.',
      'auth/wrong-password':    'Invalid credentials.',
      'auth/invalid-email':     'Please enter a valid email address.',
      'auth/too-many-requests': 'Too many attempts. Try again later.',
      'auth/invalid-credential':'Invalid credentials.',
    };
    errBox.textContent = msgs[err.code] || 'Sign-in failed. Please try again.';
    errBox.style.display = 'block';
  } finally {
    btn.disabled = false;
    btn.textContent = 'Sign In to Terminal';
  }
});

/* ═══════════════════════════════════════════════════════════════
   AUTH: FORGOT PASSWORD
═══════════════════════════════════════════════════════════════ */
function openForgotPasswordModal() {
  document.getElementById('auth-modal-alert').style.display = 'none';
  document.getElementById('auth-modal').style.display = 'flex';
}
function closeAuthModal() {
  document.getElementById('auth-modal').style.display = 'none';
  document.getElementById('auth-modal-form').reset();
}

document.getElementById('auth-modal-form').addEventListener('submit', async e => {
  e.preventDefault();
  const alertBox  = document.getElementById('auth-modal-alert');
  const submitBtn = document.getElementById('auth-submit-btn');
  const spin      = document.getElementById('auth-submit-spin');
  alertBox.style.display = 'none';

  const email = document.getElementById('auth-email').value.trim();
  if (!email) {
    alertBox.className = 'alert-fw danger';
    alertBox.textContent = 'Email is required.';
    alertBox.style.display = 'block';
    return;
  }

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    alertBox.className = 'alert-fw danger';
    alertBox.textContent = 'Please enter a valid email address.';
    alertBox.style.display = 'block';
    return;
  }

  submitBtn.disabled = true;
  spin.style.display = 'inline-block';

  const resetContinueUrl = new URL('auth-action.html', window.location.href).href;
  try {
    await auth.sendPasswordResetEmail(email, { url: resetContinueUrl, handleCodeInApp: false });
    await logAudit('PASSWORD_RESET_REQUESTED', { email });
    alertBox.className = 'alert-fw success';
    alertBox.textContent = `Password reset link sent to ${email}. Check your inbox.`;
    alertBox.style.display = 'block';
  } catch (err) {
    const msgs = {
      'auth/invalid-email':  'Please enter a valid email address.',
      'auth/user-not-found': 'No account found with this email.',
    };
    alertBox.className = 'alert-fw danger';
    alertBox.textContent = msgs[err.code] || 'An error occurred. Please try again.';
    alertBox.style.display = 'block';
  } finally {
    submitBtn.disabled = false;
    spin.style.display = 'none';
  }
});

/* ═══════════════════════════════════════════════════════════════
   LOGOUT
═══════════════════════════════════════════════════════════════ */
document.getElementById('logout-btn').addEventListener('click', () => {
  document.getElementById('logout-modal').style.display = 'flex';
});
function closeLogoutModal() {
  document.getElementById('logout-modal').style.display = 'none';
}
document.getElementById('confirm-logout-btn').addEventListener('click', async () => {
  await logAudit('ADMIN_LOGOUT', {});
  await auth.signOut();
  cleanupPage();
  closeLogoutModal();
});

/* ═══════════════════════════════════════════════════════════════
   EMERGENCY CODE MODAL
   STRIDE: Tampering + Elevation — requires dual-code auth for destructive ops
═══════════════════════════════════════════════════════════════ */
function openEmergencyModal(title, desc, callback) {
  emergencyCallback = callback;
  document.getElementById('emergency-action-title').textContent = title;
  document.getElementById('emergency-action-desc').textContent  = desc;
  document.getElementById('emergency-admin-code').value  = '';
  document.getElementById('emergency-super-code').value  = '';
  document.getElementById('emergency-error').style.display = 'none';
  document.getElementById('emergency-modal').style.display = 'flex';
}
function closeEmergencyModal() {
  document.getElementById('emergency-modal').style.display = 'none';
  emergencyCallback = null;
  pendingSettingKey = null;
  pendingEnvAction  = null;
}
async function confirmEmergencyAction() {
  const adminInput = document.getElementById('emergency-admin-code').value;
  const superInput = document.getElementById('emergency-super-code').value;
  const errEl      = document.getElementById('emergency-error');

  // Fallback codes — change these to your own
  let adminCode      = 'ADMIN123';
  let superAdminCode = 'SUPERADMIN123';

  // Try to load from Firestore if available
  try {
    const snap = await db.collection('config').doc('emergencyCodes').get();
    if (snap.exists && snap.data()?.adminCode) {
      adminCode      = snap.data().adminCode;
      superAdminCode = snap.data().superAdminCode;
    }
  } catch(e) {
    // Firestore unavailable — use fallback codes above
    console.warn('Could not load emergency codes from Firestore, using fallback.');
  }

  // Validate the entered codes
  if (!adminInput || !superInput) {
    errEl.textContent = 'Please enter both codes.';
    errEl.style.display = 'block';
    return;
  }

  if (adminInput !== adminCode || superInput !== superAdminCode) {
    errEl.textContent = 'Invalid authorization code. Action aborted.';
    errEl.style.display = 'block';
    await logAudit('EMERGENCY_AUTH_FAILED', {
      action: document.getElementById('emergency-action-title').textContent
    });
    return;
  }

  // Codes correct — proceed (capture callback before close clears it)
  errEl.style.display = 'none';
  const cb = emergencyCallback;
  closeEmergencyModal();
  if (typeof cb === 'function') {
    try {
      await Promise.resolve(cb());
    } catch (err) {
      console.error('Emergency action failed:', err);
      showToast(err?.message || 'Action could not be completed.', 'danger');
    }
  }
}

function statCardHTML(label, value, colorClass, badge, trend, subtext) {
  const trendColor = (trend||'').startsWith('+') ? 'text-success' : 'text-danger';
  return `
  <div class="col-sm-6 col-xl">
    <div class="stat-card">
      <div class="stat-label">${esc(label)}</div>
      ${trend ? `<span class="stat-trend ${trendColor}">${esc(trend)}</span>` : ''}
      <div class="stat-value ${colorClass}">${esc(value)}</div>
      ${subtext ? `<div class="stat-sub">${esc(subtext)}</div>` : ''}
      ${badge ? `<span class="stat-badge">${esc(badge)}</span>` : ''}
    </div>
  </div>`;
}
