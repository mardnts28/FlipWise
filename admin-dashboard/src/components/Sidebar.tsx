import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { 
  LayoutDashboard, Users, Library, Trophy, 
  Bell, ShieldAlert, Zap, LogOut 
} from 'lucide-react';
import { auth } from '../firebase';

const Sidebar = () => {
  const navigate = useNavigate();
  const menuItems = [
    { name: 'Dashboard', path: '/', icon: LayoutDashboard },
    { name: 'User Management', path: '/users', icon: Users },
    { name: 'Content Moderation', path: '/content', icon: Library },
    { name: 'Global Challenges', path: '/challenges', icon: Trophy },
    { name: 'Notifications', path: '/notifications', icon: Bell },
    { name: 'Audit Logs', path: '/logs', icon: ShieldAlert },
  ];

  const handleLogout = async () => {
    if (window.confirm("Sign out of the Admin Portal?")) {
      await auth.signOut();
      navigate('/');
    }
  };

  return (
    <aside style={{
      width: '280px',
      height: '100vh',
      backgroundColor: 'white',
      borderRight: '1px solid #E5E7EB',
      padding: '32px 20px',
      display: 'flex',
      flexDirection: 'column'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '40px', padding: '0 12px' }}>
        <img src="/logo.png" alt="FlipWise" style={{ width: '40px', height: '40px', borderRadius: '10px', objectFit: 'cover' }} />
        <span style={{ fontSize: '1.5rem', fontWeight: 800, color: '#1E1B4B' }}>FlipWise</span>
      </div>

      <nav style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '8px' }}>
        {menuItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            style={({ isActive }) => ({
              display: 'flex',
              alignItems: 'center',
              gap: '12px',
              padding: '14px 16px',
              borderRadius: '16px',
              color: isActive ? '#7C3AED' : '#64748B',
              backgroundColor: isActive ? '#F5F3FF' : 'transparent',
              fontWeight: isActive ? 700 : 500,
              transition: 'all 0.2s ease',
            })}
          >
            <item.icon size={20} />
            <span>{item.name}</span>
          </NavLink>
        ))}
      </nav>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <button 
            onClick={handleLogout}
            style={{ 
              display: 'flex', alignItems: 'center', gap: '8px', padding: '12px 16px', 
              borderRadius: '12px', background: '#FEE2E2', color: '#EF4444', 
              fontWeight: 700, width: '100%', cursor: 'pointer'
            }}
          >
            <LogOut size={18} />
            <span>Log out</span>
          </button>

          <div style={{ 
            padding: '20px', 
            background: '#1E1B4B', 
            borderRadius: '24px',
            color: 'white',
            textAlign: 'center'
          }}>
            <p style={{ fontSize: '0.75rem', opacity: 0.7, marginBottom: '4px' }}>Logged in as</p>
            <p style={{ fontWeight: 700, fontSize: '0.8rem', overflow: 'hidden', textOverflow: 'ellipsis' }}>{auth.currentUser?.email}</p>
          </div>
      </div>
    </aside>
  );
};

export default Sidebar;
