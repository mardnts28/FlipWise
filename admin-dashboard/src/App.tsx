import React, { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import { auth } from './firebase';
import { onAuthStateChanged, signInWithEmailAndPassword } from 'firebase/auth';
import { Loader2, Lock, ShieldCheck, QrCode, CheckCircle2 } from 'lucide-react';
import QRCode from 'qrcode';
import { verifyTotp, generateTotpSecret, buildOtpAuthUri } from './utils/totp';

import Dashboard from './pages/Dashboard';
import Users from './pages/Users';
import Decks from './pages/Content';
import Challenges from './pages/Challenges';
import Notifications from './pages/Notifications';
import Logs from './pages/Logs';

const TOTP_SECRET_KEY = 'flipwise_admin_totp_secret';
type AuthStep = 'login' | 'qr-setup' | 'otp-verify';

function App() {
  const [user, setUser] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [step, setStep] = useState<AuthStep>('login');
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [loginLoading, setLoginLoading] = useState(false);
  const [otpError, setOtpError] = useState('');
  const [totpSecret, setTotpSecret] = useState('');
  const [qrDataUrl, setQrDataUrl] = useState('');
  const [setupConfirmed, setSetupConfirmed] = useState(false);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (u) => {
      if (step === 'login') setUser(u);
      setLoading(false);
    });
    return () => unsubscribe();
  }, [step]);

  // Render QR code whenever secret or email changes
  useEffect(() => {
    if (!totpSecret || !email) return;
    const uri = buildOtpAuthUri(email, totpSecret);
    QRCode.toDataURL(uri, { width: 220, margin: 2 }).then(setQrDataUrl).catch(console.error);
  }, [totpSecret, email]);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoginLoading(true);
    try {
      await signInWithEmailAndPassword(auth, email, password);
      const existing = localStorage.getItem(TOTP_SECRET_KEY);
      if (existing) {
        setTotpSecret(existing);
        setStep('otp-verify');
      } else {
        setTotpSecret(generateTotpSecret());
        setStep('qr-setup');
      }
    } catch (err: any) {
      alert('Login Failed: ' + err.message);
    } finally {
      setLoginLoading(false);
    }
  };

  const handleConfirmSetup = () => {
    localStorage.setItem(TOTP_SECRET_KEY, totpSecret);
    setSetupConfirmed(false);
    setOtp(['', '', '', '', '', '']);
    setStep('otp-verify');
  };

  const handleOtpChange = (index: number, value: string) => {
    if (!/^\d*$/.test(value) || value.length > 1) return;
    const next = [...otp];
    next[index] = value;
    setOtp(next);
    setOtpError('');
    if (value && index < 5) document.getElementById(`otp-${index + 1}`)?.focus();
  };

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoginLoading(true);
    try {
      const code = otp.join('');
      const valid = await verifyTotp(code, totpSecret);
      if (valid) {
        setUser(auth.currentUser);
        setStep('login');
      } else {
        setOtpError('Invalid code. Check your authenticator app and try again.');
        setOtp(['', '', '', '', '', '']);
        setTimeout(() => document.getElementById('otp-0')?.focus(), 50);
      }
    } finally {
      setLoginLoading(false);
    }
  };

  const handleResetDevice = () => {
    if (window.confirm('Remove this device registration? You will need to scan a new QR code.')) {
      localStorage.removeItem(TOTP_SECRET_KEY);
      auth.signOut();
      setStep('login');
      setOtp(['', '', '', '', '', '']);
    }
  };

  if (loading) {
    return (
      <div style={{ height: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', background: '#F8FAFC' }}>
        <Loader2 className="animate-spin" size={48} color="#7C3AED" />
      </div>
    );
  }

  if (!user || step !== 'login') {
    return (
      <div style={{ height: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', background: 'linear-gradient(135deg, #F8FAFC 0%, #F0F4FF 100%)' }}>
        <div className="glass-card premium-shadow" style={{ padding: '48px', width: step === 'qr-setup' ? '480px' : '440px', textAlign: 'center' }}>

          {/* ── STEP 1: LOGIN ── */}
          {step === 'login' && (
            <form onSubmit={handleLogin}>
              <div style={{ width: '64px', height: '64px', borderRadius: '22px', background: 'linear-gradient(135deg, #F5F3FF, #EDE9FE)', color: '#7C3AED', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 24px', boxShadow: '0 8px 16px -4px rgba(124,58,237,0.15)' }}>
                <Lock size={32} />
              </div>
              <h2 className="h2" style={{ marginBottom: '8px', fontSize: '28px' }}>Admin Portal</h2>
              <p className="text-label" style={{ marginBottom: '32px' }}>Secure access to FlipWise infrastructure</p>

              <div style={{ textAlign: 'left', marginBottom: '16px' }}>
                <label style={{ fontSize: '12px', fontWeight: 700, color: '#64748B', marginBottom: '8px', display: 'block' }}>EMAIL ADDRESS</label>
                <input type="email" placeholder="admin@flipwise.app" value={email} onChange={e => setEmail(e.target.value)}
                  style={{ width: '100%', padding: '16px', borderRadius: '14px', border: '1px solid #E2E8F0', background: '#fff', outline: 'none' }} required />
              </div>

              <div style={{ textAlign: 'left', marginBottom: '32px' }}>
                <label style={{ fontSize: '12px', fontWeight: 700, color: '#64748B', marginBottom: '8px', display: 'block' }}>PASSWORD</label>
                <input type="password" placeholder="••••••••" value={password} onChange={e => setPassword(e.target.value)}
                  style={{ width: '100%', padding: '16px', borderRadius: '14px', border: '1px solid #E2E8F0', background: '#fff', outline: 'none' }} required />
              </div>

              <button type="submit" disabled={loginLoading}
                style={{ width: '100%', padding: '18px', background: '#7C3AED', color: 'white', fontWeight: 700, borderRadius: '16px', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '12px', boxShadow: '0 10px 15px -3px rgba(124,58,237,0.3)' }}>
                {loginLoading ? <Loader2 className="animate-spin" size={20} /> : 'Continue →'}
              </button>
            </form>
          )}

          {/* ── STEP 2: QR SETUP (first-time device registration) ── */}
          {step === 'qr-setup' && (
            <div>
              <div style={{ width: '64px', height: '64px', borderRadius: '22px', background: 'linear-gradient(135deg, #FFF7ED, #FED7AA)', color: '#C2410C', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 24px', boxShadow: '0 8px 16px -4px rgba(194,65,12,0.15)' }}>
                <QrCode size={32} />
              </div>
              <h2 className="h2" style={{ marginBottom: '8px', fontSize: '26px' }}>Register This Device</h2>
              <p className="text-label" style={{ marginBottom: '24px', fontSize: '14px', lineHeight: '1.6' }}>
                Open <strong>Google Authenticator</strong> or any TOTP app<br />and scan this QR code to link your device.
              </p>

              {qrDataUrl ? (
                <div style={{ background: 'white', padding: '16px', borderRadius: '20px', display: 'inline-block', marginBottom: '24px', boxShadow: '0 4px 12px rgba(0,0,0,0.08)', border: '1px solid #F1F5F9' }}>
                  <img src={qrDataUrl} alt="TOTP QR Code" style={{ display: 'block', borderRadius: '8px' }} />
                </div>
              ) : (
                <div style={{ height: '252px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <Loader2 className="animate-spin" size={32} color="#C2410C" />
                </div>
              )}

              <div style={{ background: '#FFF7ED', borderRadius: '14px', padding: '14px 20px', marginBottom: '24px', textAlign: 'left' }}>
                <p style={{ fontSize: '12px', color: '#C2410C', fontWeight: 700, marginBottom: '4px' }}>⚠️ ONE-TIME SETUP</p>
                <p style={{ fontSize: '13px', color: '#92400E' }}>Scan now — this QR code will not appear again on this device.</p>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px', cursor: 'pointer', textAlign: 'left' }}
                onClick={() => setSetupConfirmed(v => !v)}>
                <div style={{ width: '22px', height: '22px', borderRadius: '6px', border: `2px solid ${setupConfirmed ? '#7C3AED' : '#CBD5E1'}`, background: setupConfirmed ? '#7C3AED' : 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, transition: 'all 0.2s' }}>
                  {setupConfirmed && <CheckCircle2 size={14} color="white" />}
                </div>
                <span style={{ fontSize: '14px', color: '#475569' }}>I have scanned and saved this code in my authenticator app</span>
              </div>

              <button onClick={handleConfirmSetup} disabled={!setupConfirmed}
                style={{ width: '100%', padding: '18px', background: setupConfirmed ? '#7C3AED' : '#CBD5E1', color: 'white', fontWeight: 700, borderRadius: '16px', border: 'none', cursor: setupConfirmed ? 'pointer' : 'not-allowed', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '12px', transition: 'all 0.3s', boxShadow: setupConfirmed ? '0 10px 15px -3px rgba(124,58,237,0.3)' : 'none' }}>
                Proceed to Verification →
              </button>

              <button type="button" onClick={() => { auth.signOut(); setStep('login'); }}
                style={{ marginTop: '16px', background: 'none', border: 'none', color: '#94A3B8', fontWeight: 600, fontSize: '13px', cursor: 'pointer' }}>
                ← Back to Login
              </button>
            </div>
          )}

          {/* ── STEP 3: OTP VERIFY ── */}
          {step === 'otp-verify' && (
            <form onSubmit={handleVerifyOtp}>
              <div style={{ width: '64px', height: '64px', borderRadius: '22px', background: 'linear-gradient(135deg, #F0FDFA, #CCFBF1)', color: '#0D9488', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 24px', boxShadow: '0 8px 16px -4px rgba(13,148,136,0.15)' }}>
                <ShieldCheck size={32} />
              </div>
              <h2 className="h2" style={{ marginBottom: '8px', fontSize: '28px' }}>Identity Verification</h2>
              <p className="text-label" style={{ marginBottom: '32px' }}>Enter the 6-digit code from your authenticator app</p>

              <div style={{ display: 'flex', gap: '8px', justifyContent: 'center', marginBottom: otpError ? '12px' : '32px' }}>
                {otp.map((digit, idx) => (
                  <input key={idx} id={`otp-${idx}`} type="text" inputMode="numeric" maxLength={1} value={digit}
                    onChange={(e) => handleOtpChange(idx, e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === 'Backspace' && !otp[idx] && idx > 0) {
                        document.getElementById(`otp-${idx - 1}`)?.focus();
                      }
                    }}
                    autoFocus={idx === 0}
                    style={{ width: '48px', height: '58px', textAlign: 'center', fontSize: '24px', fontWeight: 700, borderRadius: '14px', border: `2px solid ${otpError ? '#EF4444' : '#7C3AED'}`, background: 'white', color: '#1E1B4B', outline: 'none' }}
                  />
                ))}
              </div>

              {otpError && (
                <p style={{ color: '#EF4444', fontSize: '13px', marginBottom: '20px', background: '#FEF2F2', padding: '10px 16px', borderRadius: '10px' }}>
                  {otpError}
                </p>
              )}

              <button type="submit" disabled={otp.join('').length < 6 || loginLoading}
                style={{ width: '100%', padding: '18px', background: '#0D9488', color: 'white', fontWeight: 700, borderRadius: '16px', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '12px', opacity: otp.join('').length < 6 ? 0.6 : 1, boxShadow: '0 10px 15px -3px rgba(13,148,136,0.3)' }}>
                {loginLoading ? <Loader2 className="animate-spin" size={20} /> : 'Verify & Access Dashboard'}
              </button>

              <div style={{ marginTop: '20px', display: 'flex', justifyContent: 'space-between' }}>
                <button type="button" onClick={() => { auth.signOut(); setStep('login'); setOtp(['','','','','','']); }}
                  style={{ background: 'none', border: 'none', color: '#94A3B8', fontWeight: 600, fontSize: '13px', cursor: 'pointer' }}>
                  ← Back to Login
                </button>
                <button type="button" onClick={handleResetDevice}
                  style={{ background: 'none', border: 'none', color: '#EF4444', fontWeight: 600, fontSize: '13px', cursor: 'pointer' }}>
                  Reset Device
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    );
  }

  return (
    <BrowserRouter>
      <div className="admin-layout">
        <Sidebar />
        <main style={{ flex: 1, height: '100vh', overflowY: 'auto' }}>
          <Header />
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/users" element={<Users />} />
            <Route path="/content" element={<Decks />} />
            <Route path="/challenges" element={<Challenges />} />
            <Route path="/notifications" element={<Notifications />} />
            <Route path="/logs" element={<Logs />} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
