import React, { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import { auth } from './firebase';
import { onAuthStateChanged, signInWithEmailAndPassword, GoogleAuthProvider, signInWithPopup } from 'firebase/auth';
import { Loader2, Lock } from 'lucide-react';

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

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (u) => {
      setUser(u);
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await signInWithEmailAndPassword(auth, email, password);
    } catch (err: any) {
      alert("Login Failed: " + err.message);
    }
  };

  if (loading) {
    return (
      <div style={{ height: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', background: '#F8FAFC' }}>
        <Loader2 className="animate-spin" size={48} color="#7C3AED" />
      </div>
    );
  }

  if (!user) {
    return (
      <div style={{ height: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', background: '#F8FAFC' }}>
        <form onSubmit={handleLogin} className="glass-card" style={{ padding: '40px', width: '400px', textAlign: 'center' }}>
          <div style={{ width: '64px', height: '64px', borderRadius: '20px', background: '#F5F3FF', color: '#7C3AED', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 24px' }}>
            <Lock size={32} />
          </div>
          <h2 className="h2" style={{ marginBottom: '8px' }}>Admin Portal</h2>
          <p className="text-label" style={{ marginBottom: '32px' }}>Authorized access only</p>
          
          <input 
            type="email" placeholder="Admin Email" value={email} onChange={e => setEmail(e.target.value)}
            style={{ width: '100%', padding: '16px', borderRadius: '12px', border: '1px solid #E2E8F0', marginBottom: '16px', outline: 'none' }}
          />
          <input 
            type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)}
            style={{ width: '100%', padding: '16px', borderRadius: '12px', border: '1px solid #E2E8F0', marginBottom: '24px', outline: 'none' }}
          />
          <button type="submit" style={{ width: '100%', padding: '16px', background: '#7C3AED', color: 'white', fontWeight: 700, borderRadius: '12px' }}>
            Login to Dashboard
          </button>
        </form>
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
