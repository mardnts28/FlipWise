import React from 'react';
import { Send, Bell, Users, Clock, History } from 'lucide-react';

const mockHistory = [
  { id: '1', title: 'Server Maintenance', sentTo: 'All Users', time: '2 days ago', status: 'Delivered' },
  { id: '2', title: 'New Challenge: Biology Blitz', sentTo: 'Interested in Science', time: '1 week ago', status: 'Delivered' },
];

const Notifications = () => {
  return (
    <div className="content-area">
      <div style={{ marginBottom: '40px' }}>
        <h1 className="h1">System Notifications</h1>
        <p className="text-label">Broadcast messages and reminders across the platform.</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '32px' }}>
        {/* Composer */}
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
                type="text" 
                placeholder="e.g. Scheduled Maintenance" 
                style={{ width: '100%', padding: '16px', borderRadius: '14px', border: '1px solid #E2E8F0', outline: 'none' }}
              />
            </div>

            <div>
              <label className="text-label" style={{ display: 'block', marginBottom: '8px' }}>Target Audience</label>
              <select style={{ width: '100%', padding: '16px', borderRadius: '14px', border: '1px solid #E2E8F0', outline: 'none' }}>
                <option>All Registered Users</option>
                <option>Active in last 7 days</option>
                <option>New Users (last 24h)</option>
              </select>
            </div>

            <div>
              <label className="text-label" style={{ display: 'block', marginBottom: '8px' }}>Message Body</label>
              <textarea 
                rows={5}
                placeholder="Describe the update or reminder here..." 
                style={{ width: '100%', padding: '16px', borderRadius: '14px', border: '1px solid #E2E8F0', outline: 'none', resize: 'none' }}
              />
            </div>

            <button style={{ 
              padding: '18px', 
              borderRadius: '16px', 
              background: '#7C3AED', 
              color: 'white', 
              fontWeight: 700,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '12px',
              marginTop: '12px'
            }}>
              <Send size={20} /> Send Broadcast Now
            </button>
          </div>
        </div>

        {/* Recent History */}
        <div className="glass-card" style={{ padding: '32px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
             <History size={20} color="#64748B" />
             <h3 className="h2" style={{ fontSize: '1.25rem' }}>Send History</h3>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {mockHistory.map(item => (
              <div key={item.id} style={{ padding: '16px', borderRadius: '16px', border: '1px solid #F1F5F9' }}>
                <h4 style={{ fontWeight: 700, color: '#1E1B4B', fontSize: '0.9rem', marginBottom: '4px' }}>{item.title}</h4>
                <p style={{ fontSize: '0.75rem', color: '#94A3B8' }}>Sent to {item.sentTo} • {item.time}</p>
                <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.7rem', fontWeight: 700, color: '#10B981', marginTop: '8px' }}>
                  <div style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#10B981' }} /> {item.status}
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
