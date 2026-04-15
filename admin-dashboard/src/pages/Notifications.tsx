import React, { useState, useEffect } from 'react';
import { Send, Bell, History, Loader2, CheckCircle2 } from 'lucide-react';
import { rtdb } from '../firebase';
import { ref, onValue, push, set, get } from 'firebase/database';

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
  const [history, setHistory] = useState<Broadcast[]>([]);
  const [sending, setSending] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const historyRef = ref(rtdb, 'system_broadcasts');
    const unsubscribe = onValue(historyRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        setHistory(Object.entries(data).map(([id, val]: [string, any]) => ({
          id, ...val
        })).sort((a, b) => b.timestamp - a.timestamp));
      }
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  const handleSend = async () => {
    if (!title || !message) return;
    setSending(true);
    try {
      const broadcastRef = ref(rtdb, 'system_broadcasts');
      const newBroadcast = {
        title,
        message,
        target,
        timestamp: Date.now()
      };
      await push(broadcastRef, newBroadcast);

      // Also push to global notifications seen by Android
      const globalNoteRef = ref(rtdb, 'public_announcements');
      await set(globalNoteRef, {
         title,
         message,
         timestamp: Date.now()
      });

      setTitle('');
      setMessage('');
      alert("Broadcast sent successfully!");
    } catch (err) {
      alert("Failed to send broadcast");
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

      <div style={{ display: 'grid', gridTemplateColumns: '1.5fr 1fr', gap: '32px' }}>
        <div className="glass-card" style={{ padding: '32px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '32px' }}>
            <div style={{ width: '48px', height: '48px', borderRadius: '14px', background: '#F5F3FF', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Bell size={24} color="#7C3AED" />
            </div>
            <h3 className="h2">Compose Broadcast</h3>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <div>
              <label className="text-label" style={{ display: 'block', marginBottom: '8px' }}>Notification Title</label>
              <input 
                value={title} onChange={e => setTitle(e.target.value)}
                type="text" placeholder="e.g. Scheduled Maintenance" 
                style={{ width: '100%', padding: '16px', borderRadius: '14px', border: '1px solid #E2E8F0', outline: 'none' }}
              />
            </div>

            <div>
              <label className="text-label" style={{ display: 'block', marginBottom: '8px' }}>Target Audience</label>
              <select value={target} onChange={e => setTarget(e.target.value)} style={{ width: '100%', padding: '16px', borderRadius: '14px', border: '1px solid #E2E8F0', outline: 'none' }}>
                <option>All Registered Users</option>
              </select>
            </div>

            <div>
              <label className="text-label" style={{ display: 'block', marginBottom: '8px' }}>Message Body</label>
              <textarea 
                value={message} onChange={e => setMessage(e.target.value)}
                rows={5} placeholder="Describe the update or reminder here..." 
                style={{ width: '100%', padding: '16px', borderRadius: '14px', border: '1px solid #E2E8F0', outline: 'none', resize: 'none' }}
              />
            </div>

            <button 
              onClick={handleSend} disabled={sending}
              style={{ 
                padding: '18px', borderRadius: '16px', background: '#7C3AED', color: 'white', fontWeight: 700,
                display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '12px', marginTop: '12px',
                opacity: sending ? 0.7 : 1
              }}>
              {sending ? <Loader2 className="animate-spin" size={20} /> : <Send size={20} />} Send Broadcast Now
            </button>
          </div>
        </div>

        <div className="glass-card" style={{ padding: '32px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
             <History size={20} color="#64748B" />
             <h3 className="h2" style={{ fontSize: '1.25rem' }}>Send History</h3>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {history.map(item => (
              <div key={item.id} style={{ padding: '16px', borderRadius: '16px', border: '1px solid #F1F5F9' }}>
                <h4 style={{ fontWeight: 700, color: '#1E1B4B', fontSize: '0.9rem', marginBottom: '4px' }}>{item.title}</h4>
                <p style={{ fontSize: '0.75rem', color: '#94A3B8' }}>{new Date(item.timestamp).toLocaleString()}</p>
                <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.7rem', fontWeight: 700, color: '#10B981', marginTop: '8px' }}>
                   <CheckCircle2 size={14} /> Delivered
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Notifications;
