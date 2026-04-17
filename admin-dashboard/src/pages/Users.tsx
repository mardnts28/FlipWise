import { useEffect, useState } from 'react';
import { Search, UserCog, ShieldCheck, Ban, Loader2 } from 'lucide-react';
import { rtdb, auth } from '../firebase';
import { ref, onValue, update } from 'firebase/database';

interface User {
  id: string;
  username: string;
  displayName: string;
  email: string;
  joinedAt: number;
  status: string;
  totalPoints: number;
  role: string;
}

const UserManagement = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const usersRef = ref(rtdb, 'leaderboard');
    const unsubscribe = onValue(usersRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const userList = Object.entries(data).map(([id, val]: [string, any]) => ({
          id,
          username: val.username || 'unknown',
          displayName: val.displayName || 'User',
          email: val.email || 'N/A',
          joinedAt: val.joinedAt || Date.now(),
          status: val.status || 'Active',
          totalPoints: val.totalPoints || 0,
          role: val.role || 'standard'
        }));
        setUsers(userList);
      }
      setLoading(false);
    }, (err) => {
      console.error(err);
      setError("Permission Denied: You do not have administrative access.");
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const handleStatusChange = async (userId: string, newStatus: string) => {
     try {
        const updates: any = {};
        updates[`users/${userId}/profile/status`] = newStatus;
        updates[`leaderboard/${userId}/status`] = newStatus;
        await update(ref(rtdb), updates);
     } catch (err) {
        console.error("Failed to update status", err);
     }
  };

  const filteredUsers = users.filter(user => 
    user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.displayName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleExportCSV = () => {
    if (filteredUsers.length === 0) return;
    
    // Define headers
    const headers = ['User ID', 'Username', 'Display Name', 'Status', 'Points', 'Role'];
    
    // Map data to rows
    const rows = filteredUsers.map(user => [
      user.id,
      user.username,
      user.displayName,
      user.status,
      user.totalPoints,
      user.role
    ]);

    // Construct CSV content
    const csvContent = [
      headers.join(','),
      ...rows.map(row => row.join(','))
    ].join('\n');

    // Create a blob and trigger download
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `flipwise_users_${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  if (error) {
    return (
      <div className="content-area" style={{ textAlign: 'center', paddingTop: '100px' }}>
        <ShieldCheck size={64} color="#EF4444" style={{ marginBottom: '24px' }} />
        <h2 className="h2">Access Restricted</h2>
        <p className="text-label">{error}</p>
        <button 
          onClick={() => auth.signOut()}
          style={{ marginTop: '24px', padding: '12px 24px', background: '#7C3AED', color: 'white', borderRadius: '12px', fontWeight: 700 }}
        >
          Sign Out & Try Another Account
        </button>
      </div>
    );
  }

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
          <h1 className="h1">User Management</h1>
          <p className="text-label">Control access, security settings, and account integrity.</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
             <div style={{ position: 'relative' }}>
                <Search style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#94A3B8' }} size={16} />
                <input 
                  type="text" 
                  placeholder="Filter users..." 
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  style={{ padding: '12px 12px 12px 40px', borderRadius: '16px', border: '1px solid #E2E8F0', outline: 'none', width: '250px' }}
                />
             </div>
             <button 
               onClick={handleExportCSV}
               style={{ padding: '12px 24px', background: '#7C3AED', color: 'white', fontWeight: 700, borderRadius: '16px' }}
             >
               Export CSV
             </button>
        </div>
      </div>

      <div className="glass-card" style={{ overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid #E2E8F0', background: '#F8FAFC' }}>
              {['User', 'Status', 'Points', 'Role', 'Actions'].map((head) => (
                <th key={head} style={{ padding: '20px 24px', fontSize: '0.875rem', fontWeight: 700, color: '#64748B' }}>{head}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {filteredUsers.map((user) => (
              <tr key={user.id} style={{ borderBottom: '1px solid #F1F5F9', transition: 'background 0.2s' }} className="user-row">
                <td style={{ padding: '20px 24px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{ width: '40px', height: '40px', borderRadius: '12px', background: '#F5F3FF', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, color: '#7C3AED' }}>
                      {user.displayName.charAt(0)}
                    </div>
                    <div>
                      <p style={{ fontWeight: 700, color: '#1E1B4B' }}>{user.displayName}</p>
                      <p style={{ fontSize: '0.75rem', color: '#94A3B8' }}>@{user.username}</p>
                    </div>
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
                <td style={{ padding: '20px 24px', fontWeight: 700, color: '#F97316' }}>{user.totalPoints.toLocaleString()}</td>
                <td style={{ padding: '20px 24px', color: '#64748B', fontSize: '0.875rem' }}>{user.role}</td>
                <td style={{ padding: '20px 24px' }}>
                   <div style={{ display: 'flex', gap: '8px' }}>
                      <button style={{ padding: '8px', borderRadius: '10px', background: '#F1F5F9', color: '#64748B' }} title="Profile Settings"><UserCog size={18} /></button>
                      <button 
                        onClick={() => handleStatusChange(user.id, user.status === 'Active' ? 'Banned' : 'Active')}
                        style={{ padding: '8px', borderRadius: '10px', background: user.status === 'Active' ? '#FEE2E2' : '#D1FAE5', color: user.status === 'Active' ? '#EF4444' : '#059669' }} 
                        title={user.status === 'Active' ? "Ban User" : "Unban User"}
                      >
                        <Ban size={18} />
                      </button>
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
