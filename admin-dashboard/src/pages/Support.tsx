import React from 'react';
import { MessageSquare, Clock, Filter, CheckCircle2, AlertCircle } from 'lucide-react';

const mockTickets = [
  { id: 't1', user: 'jake_flash', type: 'Bug', subject: 'Session not saving correctly', message: 'I finished a deck but my points didnt update...', status: 'Open', priority: 'High', date: '10 mins ago' },
  { id: 't2', user: 'linda_k', type: 'Feedback', subject: 'Dark mode colors', message: 'The dark mode is a bit too purple, maybe more blue?', status: 'Resolved', priority: 'Low', date: '2 hours ago' },
  { id: 't3', user: 'sam_tech', type: 'Support', subject: 'Forgot TOTP secret', message: 'I lost my authenticator app and need to log in.', status: 'In Progress', priority: 'Medium', date: '5 hours ago' },
  { id: 't4', user: 'maria_r', type: 'Bug', subject: 'Image upload failed', message: 'Trying to upload a custom avatar but it keeps erroring out.', status: 'Open', priority: 'High', date: '1 day ago' },
];

const Support = () => {
  return (
    <div className="content-area">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '40px' }}>
        <div>
          <h1 className="h1">Support & Feedback</h1>
          <p className="text-label">Manage user communications, bug reports, and feedback tickets.</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
             <div style={{ display: 'flex', alignItems: 'center', background: 'white', border: '1px solid #E2E8F0', padding: '0 16px', borderRadius: '16px', gap: '8px' }}>
                <Filter size={16} color="#64748B" />
                <select style={{ border: 'none', outline: 'none', padding: '12px 0', fontWeight: 600, background: 'transparent' }}>
                  <option>All Tickets</option>
                  <option>Open Only</option>
                  <option>Bug Reports</option>
                </select>
             </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '20px' }}>
        {mockTickets.map((ticket) => (
          <div key={ticket.id} className="glass-card premium-shadow" style={{ padding: '24px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <div style={{ 
                  width: '40px', height: '40px', borderRadius: '12px', background: '#F5F3FF', display: 'flex', alignItems: 'center', justifyContent: 'center' 
                }}>
                  <MessageSquare size={20} color="#7C3AED" />
                </div>
                <div>
                  <h4 style={{ fontWeight: 700, color: '#1E1B4B' }}>{ticket.subject}</h4>
                  <p style={{ fontSize: '0.75rem', color: '#94A3B8' }}>From @{ticket.user} • {ticket.type}</p>
                </div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <span style={{ 
                  padding: '6px 12px', borderRadius: '8px', fontSize: '0.75rem', fontWeight: 700,
                  background: ticket.status === 'Open' ? '#FEE2E2' : ticket.status === 'Resolved' ? '#D1FAE5' : '#FEF3C7',
                  color: ticket.status === 'Open' ? '#EF4444' : ticket.status === 'Resolved' ? '#065F46' : '#B45309',
                }}>
                  {ticket.status}
                </span>
                <p style={{ fontSize: '0.75rem', color: '#94A3B8', marginTop: '6px' }}>{ticket.date}</p>
              </div>
            </div>

            <p style={{ color: '#64748B', fontSize: '0.9rem', lineHeight: '1.6', marginBottom: '24px' }}>{ticket.message}</p>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                {ticket.priority === 'High' && <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#EF4444', fontSize: '0.75rem', fontWeight: 700 }}><AlertCircle size={14} /> High Priority</span>}
              </div>
              <div style={{ display: 'flex', gap: '12px' }}>
                <button style={{ padding: '8px 16px', borderRadius: '10px', background: '#F1F5F9', color: '#64748B', fontWeight: 600 }}>Message User</button>
                <button style={{ padding: '8px 16px', borderRadius: '10px', background: '#7C3AED', color: 'white', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <CheckCircle2 size={16} /> Mark Resolved
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Support;
