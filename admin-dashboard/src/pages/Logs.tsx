import React from 'react';
import { ShieldAlert, User, Database, Globe, Clock, ArrowRight } from 'lucide-react';

const mockLogs = [
  { id: 'l1', type: 'AUTH', action: 'User Login', user: 'alex_learns', details: 'IP: 192.168.1.45 • Device: Android 14', time: '2 mins ago', severity: 'low' },
  { id: 'l2', type: 'ADMIN', action: 'Account Banned', user: 'admin_master', details: 'Banned sarah_chen for spamming global decks.', time: '15 mins ago', severity: 'high' },
  { id: 'l3', type: 'DATABASE', action: 'Sync Completed', user: 'system', details: 'Full sync of 45,000 flashcards to RTDB.', time: '45 mins ago', severity: 'low' },
  { id: 'l4', type: 'SECURITY', action: 'TOTP Reset', user: 'admin_master', details: 'Reset secret for user alex_learns.', time: '1 hour ago', severity: 'medium' },
  { id: 'l5', type: 'ADMIN', action: 'Global Challenge Created', user: 'admin_master', details: 'Launched "Spring Study Marathon".', time: '3 hours ago', severity: 'low' },
];

const AuditLogs = () => {
  const getSeverityStyle = (sev: string) => {
    switch(sev) {
      case 'high': return { bg: '#FEE2E2', color: '#EF4444' };
      case 'medium': return { bg: '#FFEDD5', color: '#F97316' };
      default: return { bg: '#F1F5F9', color: '#64748B' };
    }
  };

  const getIcon = (type: string) => {
    switch(type) {
      case 'AUTH': return <User size={20} />;
      case 'SECURITY': return <ShieldAlert size={20} />;
      case 'DATABASE': return <Database size={20} />;
      default: return <Globe size={20} />;
    }
  };

  return (
    <div className="content-area">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '40px' }}>
        <div>
          <h1 className="h1">Audit & Activity Logs</h1>
          <p className="text-label">System-wide event tracking and administrative accountability.</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
             <button style={{ padding: '12px 24px', background: '#FFFFFF', border: '1px solid #E2E8F0', fontWeight: 600, borderRadius: '16px' }}>Filter by Type</button>
             <button style={{ padding: '12px 24px', background: '#1E1B4B', color: 'white', fontWeight: 700, borderRadius: '16px' }}>Clear Logs</button>
        </div>
      </div>

      <div className="glass-card" style={{ padding: '16px' }}>
        {mockLogs.map((log) => {
          const style = getSeverityStyle(log.severity);
          return (
            <div key={log.id} style={{ 
              display: 'flex', 
              alignItems: 'center', 
              padding: '20px', 
              borderBottom: '1px solid #F1F5F9',
              gap: '24px'
            }}>
              <div style={{ 
                width: '48px', 
                height: '48px', 
                borderRadius: '14px', 
                backgroundColor: style.bg, 
                color: style.color,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}>
                {getIcon(log.type)}
              </div>

              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '4px' }}>
                  <h4 style={{ fontWeight: 700, color: '#1E1B4B' }}>{log.action}</h4>
                  <span style={{ fontSize: '0.75rem', fontWeight: 700, color: style.color }}>[{log.type}]</span>
                </div>
                <p style={{ fontSize: '0.875rem', color: '#64748B' }}>
                  <strong>{log.user}</strong> <ArrowRight size={12} style={{ margin: '0 4px' }} /> {log.details}
                </p>
              </div>

              <div style={{ textAlign: 'right' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#94A3B8', fontSize: '0.875rem' }}>
                  <Clock size={14} /> {log.time}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default AuditLogs;
