import { Search, Calendar as CalIcon } from 'lucide-react';

const Header = () => {
  const today = new Date().toLocaleDateString('en-US', { 
    weekday: 'long', 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  });

  return (
    <header style={{
      padding: '24px 32px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      backgroundColor: 'transparent'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '32px' }}>
        <div style={{ 
          display: 'flex', 
          alignItems: 'center', 
          background: 'white',
          padding: '12px 20px',
          borderRadius: '16px',
          gap: '12px',
          width: '400px',
          border: '1px solid #E5E7EB'
        }}>
          <Search size={18} color="#94A3B8" />
          <input 
            type="text" 
            placeholder="Search users, decks, or logs..." 
            style={{ border: 'none', outline: 'none', width: '100%', fontSize: '0.9rem' }} 
          />
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#64748B' }}>
          <CalIcon size={16} />
          <span style={{ fontSize: '0.875rem', fontWeight: 600 }}>{today}</span>
        </div>
        <div style={{ 
          width: '44px', 
          height: '44px', 
          borderRadius: '14px', 
          background: '#DDD6FE',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontWeight: 700,
          color: '#7C3AED'
        }}>
          AM
        </div>
      </div>
    </header>
  );
};

export default Header;
