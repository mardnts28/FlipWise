import { useState, useEffect } from 'react';
import { ShieldAlert, User, Database, Globe, Clock, ArrowRight, Loader2 } from 'lucide-react';
import { rtdb } from '../firebase';
import { ref, onValue } from 'firebase/database';

interface Log {
  id: string;
  type: string;
  action: string;
  user: string;
  details: string;
  timestamp: number;
  severity: string;
}

const AuditLogs = () => {
  const [logs, setLogs] = useState<Log[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const logsRef = ref(rtdb, 'audit_logs');
    const unsubscribe = onValue(logsRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        setLogs(Object.entries(data).map(([id, val]: [string, any]) => ({
          id, ...val
        })).sort((a, b) => b.timestamp - a.timestamp));
      }
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  const getSeverityStyle = (sev: string) => {
    switch(sev?.toLowerCase()) {
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

  if (loading) {
    return (
      <div className="content-area" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <Loader2 className="animate-spin" size={48} color="#7C3AED" />
      </div>
    );
  }

  return (
    <div className="content-area">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '40px' }}>
        <div>
          <h1 className="h1">Audit & Activity Logs</h1>
          <p className="text-label">System-wide event tracking and administrative accountability.</p>
        </div>
      </div>

      <div className="glass-card" style={{ padding: '16px' }}>
        {logs.length === 0 ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#94A3B8' }}>No logs recorded.</div>
        ) : (
          logs.map((log) => {
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
                    <Clock size={14} /> {new Date(log.timestamp).toLocaleString()}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

export default AuditLogs;
