import { useState, useEffect } from 'react';
import { Send, Bell, History, Loader2, CheckCircle2 } from 'lucide-react';
import { rtdb } from '../firebase';
import { ref, onValue, push, set } from 'firebase/database';

interface Broadcast {
  id: string;
  title: string;
  message: string;
  target: string;
  timestamp: number;
}

const Notifications = () => {
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [target, setTarget] = useState('All Registered Users');
  const [users, setUsers] = useState<{id: string, name: string}[]>([]);
  const [history, setHistory] = useState<Broadcast[]>([]);
  const [sending, setSending] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fetch History
    const historyRef = ref(rtdb, 'system_broadcasts');
    onValue(historyRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        setHistory(Object.entries(data).map(([id, val]: [string, any]) => ({
          id, ...val
        })).sort((a, b) => b.timestamp - a.timestamp));
      }
    });

    // Fetch Users for Targeting
    const usersRef = ref(rtdb, 'leaderboard');
    onValue(usersRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        setUsers(Object.entries(data).map(([id, val]: [string, any]) => ({
          id, name: val.displayName || val.username || id
        })));
      }
      setLoading(false);
    });
  }, []);

  const handleSend = async () => {
    if (!title || !message) return;
    setSending(true);
    try {
      const timestamp = Date.now();
      const broadcastRef = ref(rtdb, 'system_broadcasts');
      
      const targetName = target === 'All Registered Users' 
        ? 'All Registered Users' 
        : users.find(u => u.id === target)?.name || 'Specific User';

      const newBroadcast = {
        title,
        message,
        target: targetName,
        timestamp
      };
      await push(broadcastRef, newBroadcast);

      if (target === 'All Registered Users') {
        // Global Broadcast
        const globalNoteRef = ref(rtdb, 'public_announcements');
        await set(globalNoteRef, { title, message, timestamp });
      } else {
        // Targeted Notification
        const userNoteRef = ref(rtdb, `users/${target}/notifications`);
        await push(userNoteRef, { title, message, timestamp, read: false });
      }

      setTitle('');
      setMessage('');
      alert("Notification sent successfully!");
    } catch (err) {
      alert("Failed to send notification");
    } finally {
      setSending(false);
    }
  };

  if (loading) {
    return (
      <div className="content-area" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <Loader2 className="animate-spin" size={48} color="#7C3AED" />
      </div>
    );
  }

  return (
    <div className="content-area">
      <div style={{ marginBottom: '40px' }}>
        <h1 className="h1">System Notifications</h1>
        <p className="text-label">Broadcast messages and reminders across the platform.</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '32px' }}>
        <div className="glass-card" style={{ padding: '32px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '32px' }}>
            <div style={{ width: '48px', height: '48px', borderRadius: '14px', background: 'linear-gradient(135deg, #F5F3FF 0%, #EDE9FE 100%)', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 4px 12px rgba(124, 58, 237, 0.15)' }}>
              <Bell size={24} color="#7C3AED" />
            </div>
            <h3 className="h2">Compose Notification</h3>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <div>
              <label className="text-label" style={{ display: 'block', marginBottom: '8px', fontWeight: 600 }}>NOTIFICATION TITLE</label>
              <input 
                value={title} onChange={e => setTitle(e.target.value)}
                type="text" placeholder="e.g. Server Maintenance" 
                style={{ width: '100%', padding: '16px', borderRadius: '14px', border: '1px solid #E2E8F0', outline: 'none', background: '#F8FAFC' }}
              />
            </div>

            <div>
              <label className="text-label" style={{ display: 'block', marginBottom: '8px', fontWeight: 600 }}>TARGET RECIPIENT</label>
              <select 
                value={target} onChange={e => setTarget(e.target.value)} 
                style={{ width: '100%', padding: '16px', borderRadius: '14px', border: '1px solid #E2E8F0', outline: 'none', background: '#F8FAFC', cursor: 'pointer' }}
              >
                <option value="All Registered Users">Broadcast to All Users</option>
                <optgroup label="Specific Users (Real-time)">
                  {users.map(user => (
                    <option key={user.id} value={user.id}>{user.name}</option>
                  ))}
                </optgroup>
              </select>
            </div>

            <div>
              <label className="text-label" style={{ display: 'block', marginBottom: '8px', fontWeight: 600 }}>MESSAGE CONTENT</label>
              <textarea 
                value={message} onChange={e => setMessage(e.target.value)}
                rows={5} placeholder="Type your message here..." 
                style={{ width: '100%', padding: '16px', borderRadius: '14px', border: '1px solid #E2E8F0', outline: 'none', resize: 'none', background: '#F8FAFC' }}
              />
            </div>

            <button 
              onClick={handleSend} disabled={sending}
              style={{ 
                padding: '18px', borderRadius: '16px', background: '#7C3AED', color: 'white', fontWeight: 700,
                display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '12px', marginTop: '12px',
                boxShadow: '0 10px 15px -3px rgba(124, 58, 237, 0.3)', cursor: 'pointer', transition: 'transform 0.2s'
              }}
              onMouseDown={e => e.currentTarget.style.transform = 'scale(0.98)'}
              onMouseUp={e => e.currentTarget.style.transform = 'scale(1)'}
            >
              {sending ? <Loader2 className="animate-spin" size={20} /> : <Send size={20} />} 
              {target === 'All Registered Users' ? "Broadcast to Platform" : "Send Private Alert"}
            </button>
          </div>
        </div>

        <div className="glass-card" style={{ padding: '32px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
             <History size={20} color="#64748B" />
             <h3 className="h2" style={{ fontSize: '1.25rem' }}>Transmission Logs</h3>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', maxHeight: '550px', overflowY: 'auto', paddingRight: '8px' }}>
            {history.map(item => (
              <div key={item.id} style={{ padding: '20px', borderRadius: '20px', background: '#F8FAFC', border: '1px solid #F1F5F9' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                  <h4 style={{ fontWeight: 700, color: '#1E1B4B', fontSize: '0.95rem' }}>{item.title}</h4>
                  <span style={{ fontSize: '0.7rem', color: '#7C3AED', fontWeight: 700, background: '#F5F3FF', padding: '4px 8px', borderRadius: '6px' }}>{item.target}</span>
                </div>
                <p style={{ fontSize: '0.875rem', color: '#64748B', marginBottom: '12px', lineHeight: '1.5' }}>{item.message}</p>
                <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.75rem', color: '#94A3B8' }}>
                   <CheckCircle2 size={14} color="#10B981" /> {new Date(item.timestamp).toLocaleString()}
                </div>
              </div>
            ))}
            {history.length === 0 && <p style={{ textAlign: 'center', color: '#94A3B8', marginTop: '40px' }}>No activity recorded.</p>}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Notifications;
