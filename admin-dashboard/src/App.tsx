import React, { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import { auth } from './firebase';
import { onAuthStateChanged, signInWithEmailAndPassword } from 'firebase/auth';
import { Loader2, Lock, ShieldCheck } from 'lucide-react';

import Dashboard from './pages/Dashboard';
import Users from './pages/Users';
import Decks from './pages/Content';
import Challenges from './pages/Challenges';
import Notifications from './pages/Notifications';
import Logs from './pages/Logs';

function App() {
  const [user, setUser] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isTwoFactor, setIsTwoFactor] = useState(false);
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [loginLoading, setLoginLoading] = useState(false);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (u) => {
      // If we are in the middle of OTP, don't auto-set user yet
      if (!isTwoFactor) {
        setUser(u);
      }
      setLoading(false);
    });
    return () => unsubscribe();
  }, [isTwoFactor]);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoginLoading(true);
    try {
      await signInWithEmailAndPassword(auth, email, password);
      setIsTwoFactor(true);
    } catch (err: any) {
      alert("Login Failed: " + err.message);
    } finally {
      setLoginLoading(false);
    }
  };

  const handleOtpChange = (index: number, value: string) => {
    if (value.length > 1) return;
    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);

    // Auto-focus next input
    if (value && index < 5) {
      const nextInput = document.getElementById(`otp-${index + 1}`);
      nextInput?.focus();
    }
  };

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoginLoading(true);
    // Simulate verification delay
    setTimeout(() => {
      setUser(auth.currentUser);
      setIsTwoFactor(false);
      setLoginLoading(false);
    }, 1500);
  };

  if (loading) {
    return (
      <div style={{ height: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', background: '#F8FAFC' }}>
        <Loader2 className="animate-spin" size={48} color="#7C3AED" />
      </div>
    );
  }

  if (!user || isTwoFactor) {
    return (
      <div style={{ height: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', background: '#F8FAFC' }}>
        <div className="glass-card premium-shadow" style={{ padding: '48px', width: '440px', textAlign: 'center' }}>
          {!isTwoFactor ? (
            <form onSubmit={handleLogin}>
              <div style={{ width: '64px', height: '64px', borderRadius: '22px', background: 'linear-gradient(135deg, #F5F3FF 0%, #EDE9FE 100%)', color: '#7C3AED', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 24px', boxShadow: '0 8px 16px -4px rgba(124, 58, 237, 0.1)' }}>
                <Lock size={32} />
              </div>
              <h2 className="h2" style={{ marginBottom: '8px', fontSize: '28px' }}>Admin Portal</h2>
              <p className="text-label" style={{ marginBottom: '32px' }}>Secure access to FlipWise infrastructure</p>
              
              <div style={{ textAlign: 'left', marginBottom: '16px' }}>
                <label style={{ fontSize: '12px', fontWeight: 700, color: '#64748B', marginLeft: '4px', marginBottom: '8px', display: 'block' }}>EMAIL ADDRESS</label>
                <input 
                  type="email" placeholder="admin@flipwise.app" value={email} onChange={e => setEmail(e.target.value)}
                  style={{ width: '100%', padding: '16px', borderRadius: '14px', border: '1px solid #E2E8F0', background: '#FFFFFF', outline: 'none', transition: 'all 0.2s' }}
                  required
                />
              </div>

              <div style={{ textAlign: 'left', marginBottom: '32px' }}>
                <label style={{ fontSize: '12px', fontWeight: 700, color: '#64748B', marginLeft: '4px', marginBottom: '8px', display: 'block' }}>PASSWORD</label>
                <input 
                  type="password" placeholder="••••••••" value={password} onChange={e => setPassword(e.target.value)}
                  style={{ width: '100%', padding: '16px', borderRadius: '14px', border: '1px solid #E2E8F0', background: '#FFFFFF', outline: 'none', transition: 'all 0.2s' }}
                  required
                />
              </div>

              <button 
                type="submit" 
                disabled={loginLoading}
                style={{ width: '100%', padding: '18px', background: '#7C3AED', color: 'white', fontWeight: 700, borderRadius: '16px', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '12px', boxShadow: '0 10px 15px -3px rgba(124, 58, 237, 0.3)' }}
              >
                {loginLoading ? <Loader2 className="animate-spin" size={20} /> : "Continue to Verify"}
              </button>
            </form>
          ) : (
            <form onSubmit={handleVerifyOtp}>
              <div style={{ width: '64px', height: '64px', borderRadius: '22px', background: 'linear-gradient(135deg, #F0FDFA 0%, #CCFBF1 100%)', color: '#0D9488', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 24px' }}>
                <ShieldCheck size={32} />
              </div>
              <h2 className="h2" style={{ marginBottom: '8px', fontSize: '28px' }}>Verify Identity</h2>
              <p className="text-label" style={{ marginBottom: '32px' }}>Enter the 6-digit code sent to your admin device</p>
              
              <div style={{ display: 'flex', gap: '8px', justifyContent: 'center', marginBottom: '32px' }}>
                {otp.map((digit, idx) => (
                  <input
                    key={idx}
                    id={`otp-${idx}`}
                    type="text"
                    maxLength={1}
                    value={digit}
                    onChange={(e) => handleOtpChange(idx, e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === 'Backspace' && !otp[idx] && idx > 0) {
                        document.getElementById(`otp-${idx - 1}`)?.focus();
                      }
                    }}
                    style={{ width: '48px', height: '56px', textAlign: 'center', fontSize: '24px', fontWeight: 700, borderRadius: '12px', border: '2px solid #7C3AED', background: 'white', color: '#1E1B4B', outline: 'none' }}
                  />
                ))}
              </div>

              <button 
                type="submit" 
                disabled={loginLoading || otp.join('').length < 6}
                style={{ width: '100%', padding: '18px', background: '#0D9488', color: 'white', fontWeight: 700, borderRadius: '16px', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '12px', opacity: otp.join('').length < 6 ? 0.6 : 1 }}
              >
                {loginLoading ? <Loader2 className="animate-spin" size={20} /> : "Verify & Access"}
              </button>
              
              <button 
                type="button"
                onClick={() => setIsTwoFactor(false)}
                style={{ marginTop: '20px', background: 'none', border: 'none', color: '#64748B', fontWeight: 600, fontSize: '14px', cursor: 'pointer' }}
              >
                Back to Login
              </button>
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
