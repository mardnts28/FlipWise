import React from 'react';
import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, Users, Library, Trophy, 
  Bell, BarChart3, ShieldAlert, LifeBuoy, Zap 
} from 'lucide-react';

const Sidebar = () => {
  const menuItems = [
    { name: 'Dashboard', path: '/', icon: LayoutDashboard },
    { name: 'User Management', path: '/users', icon: Users },
    { name: 'Content Moderation', path: '/content', icon: Library },
    { name: 'Global Challenges', path: '/challenges', icon: Trophy },
    { name: 'Notifications', path: '/notifications', icon: Bell },
    { name: 'Analytics', path: '/analytics', icon: BarChart3 },
    { name: 'Audit Logs', path: '/logs', icon: ShieldAlert },
    { name: 'Support Tickets', path: '/support', icon: LifeBuoy },
  ];

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
        <div style={{ padding: '8px', background: '#7C3AED', borderRadius: '12px' }}>
          <Zap size={24} color="white" />
        </div>
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

      <div style={{ 
        padding: '20px', 
        background: '#1E1B4B', 
        borderRadius: '24px',
        color: 'white',
        textAlign: 'center'
      }}>
        <p style={{ fontSize: '0.75rem', opacity: 0.7, marginBottom: '4px' }}>Logged in as</p>
        <p style={{ fontWeight: 700 }}>Admin Master</p>
      </div>
    </aside>
  );
};

export default Sidebar;
