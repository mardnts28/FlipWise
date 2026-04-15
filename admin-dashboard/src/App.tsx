import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Header from './components/Header';

import Dashboard from './pages/Dashboard';
import Users from './pages/Users';
import Decks from './pages/Content';
import Challenges from './pages/Challenges';
import Notifications from './pages/Notifications';
import Analytics from './pages/Analytics';
import Logs from './pages/Logs';
import Support from './pages/Support';

function App() {
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
            <Route path="/analytics" element={<Analytics />} />
            <Route path="/logs" element={<Logs />} />
            <Route path="/support" element={<Support />} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
