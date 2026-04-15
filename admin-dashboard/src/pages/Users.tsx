import React from 'react';
import { Search, UserCog, ShieldCheck, Mail, Calendar, Ban } from 'lucide-react';

const mockUsers = [
  { id: '1', username: 'alex_learns', displayName: 'Alex Rivera', email: 'alex@example.com', joined: 'Oct 12, 2025', status: 'Active', points: 15400 },
  { id: '2', username: 'study_pro', displayName: 'Sarah Chen', email: 'sarah.c@web.com', joined: 'Nov 05, 2025', status: 'Banned', points: 2100 },
  { id: '3', username: 'flipMaster', displayName: 'Mark Johnson', email: 'markj@service.net', joined: 'Dec 20, 2025', status: 'Active', points: 8900 },
  { id: '4', username: 'brainy_bee', displayName: 'Emma Wilson', email: 'emma.w@edu.com', joined: 'Jan 15, 2026', status: 'Active', points: 12100 },
];

const UserManagement = () => {
  return (
    <div className="content-area">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '40px' }}>
        <div>
          <h1 className="h1">User Management</h1>
          <p className="text-label">Control access, security settings, and account integrity.</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
             <button style={{ padding: '12px 24px', background: '#7C3AED', color: 'white', fontWeight: 700, borderRadius: '16px' }}>Export CSV</button>
        </div>
      </div>

      <div className="glass-card" style={{ overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid #E2E8F0', background: '#F8FAFC' }}>
              {['User', 'Email', 'Joined Date', 'Status', 'Points', 'Actions'].map((head) => (
                <th key={head} style={{ padding: '20px 24px', fontSize: '0.875rem', fontWeight: 700, color: '#64748B' }}>{head}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {mockUsers.map((user) => (
              <tr key={user.id} style={{ borderBottom: '1px solid #F1F5F9', transition: 'background 0.2s' }} className="user-row">
                <td style={{ padding: '20px 24px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{ width: '40px', height: '40px', borderRadius: '12px', background: '#F5F3FF', display: 'flex', alignItems: 'center', justifySelf: 'center', justifyContent: 'center', fontWeight: 700, color: '#7C3AED' }}>
                      {user.displayName.charAt(0)}
                    </div>
                    <div>
                      <p style={{ fontWeight: 700, color: '#1E1B4B' }}>{user.displayName}</p>
                      <p style={{ fontSize: '0.75rem', color: '#94A3B8' }}>@{user.username}</p>
                    </div>
                  </div>
                </td>
                <td style={{ padding: '20px 24px' }}>
                   <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#64748B', fontSize: '0.875rem' }}>
                      <Mail size={14} /> {user.email}
                   </div>
                </td>
                <td style={{ padding: '20px 24px' }}>
                   <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#64748B', fontSize: '0.875rem' }}>
                      <Calendar size={14} /> {user.joined}
                   </div>
                </td>
                <td style={{ padding: '20px 24px' }}>
                  <span style={{ 
                    padding: '6px 12px', 
                    borderRadius: '8px', 
                    fontSize: '0.75rem', 
                    fontWeight: 700,
                    background: user.status === 'Active' ? '#D1FAE5' : '#FEE2E2',
                    color: user.status === 'Active' ? '#065F46' : '#991B1B'
                  }}>
                    {user.status}
                  </span>
                </td>
                <td style={{ padding: '20px 24px', fontWeight: 700, color: '#F97316' }}>{user.points.toLocaleString()}</td>
                <td style={{ padding: '20px 24px' }}>
                   <div style={{ display: 'flex', gap: '8px' }}>
                      <button style={{ p: '8px', borderRadius: '10px', background: '#F1F5F9', color: '#64748B' }} title="Profile Settings"><UserCog size={18} /></button>
                      <button style={{ p: '8px', borderRadius: '10px', background: '#F1F5F9', color: '#64748B' }} title="Verify Security"><ShieldCheck size={18} /></button>
                      <button style={{ p: '8px', borderRadius: '10px', background: '#FEE2E2', color: '#EF4444' }} title="Ban User"><Ban size={18} /></button>
                   </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default UserManagement;
