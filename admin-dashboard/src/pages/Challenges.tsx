import React from 'react';
import { TrendingUp, Users, Target, Calendar, Plus } from 'lucide-react';

const mockChallenges = [
  { id: 'c1', title: 'Spring Study Marathon', goal: '1M Cards Studied', progress: 65, ended: 'In 4 days', category: 'Community' },
  { id: 'c2', title: 'Language Legend', goal: '10k New Cards (Language)', progress: 24, ended: 'In 12 days', category: 'Category' },
];

const Challenges = () => {
  return (
    <div className="content-area">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '40px' }}>
        <div>
          <h1 className="h1">Global Challenges</h1>
          <p className="text-label">Manage platform-wide gamification and collective goals.</p>
        </div>
        <button style={{ 
          display: 'flex', alignItems: 'center', gap: '8px', padding: '12px 24px', 
          background: '#7C3AED', color: 'white', fontWeight: 700, borderRadius: '16px' 
        }}>
          <Plus size={20} /> New Challenge
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '24px' }}>
        {mockChallenges.map((item) => (
          <div key={item.id} className="glass-card premium-shadow" style={{ padding: '32px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '24px' }}>
              <span style={{ fontSize: '0.75rem', fontWeight: 700, padding: '4px 10px', background: '#F5F3FF', color: '#7C3AED', borderRadius: '8px' }}>
                {item.category} Event
              </span>
              <span style={{ fontSize: '0.875rem', color: '#F97316', fontWeight: 600 }}>{item.ended}</span>
            </div>

            <h3 className="h2" style={{ marginBottom: '8px' }}>{item.title}</h3>
            <p className="text-label" style={{ marginBottom: '24px' }}>Target: {item.goal}</p>

            <div style={{ marginBottom: '8px', display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ fontSize: '0.875rem', fontWeight: 700, color: '#1E1B4B' }}>{item.progress}% Completed</span>
              <span style={{ fontSize: '0.875rem', color: '#94A3B8' }}>{item.progress * 10}k / 1M</span>
            </div>
            <div style={{ width: '100%', height: '12px', background: '#F1F5F9', borderRadius: '6px', overflow: 'hidden', marginBottom: '32px' }}>
              <div style={{ width: `${item.progress}%`, height: '100%', background: 'linear-gradient(90deg, #7C3AED, #F97316)', borderRadius: '6px' }} />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
               <button style={{ padding: '12px', borderRadius: '12px', background: '#F1F5F9', color: '#1E1B4B', fontWeight: 600 }}>Edit Rules</button>
               <button style={{ padding: '12px', borderRadius: '12px', background: '#FFFFFF', border: '1px solid #E2E8F0', color: '#1E1B4B', fontWeight: 600 }}>View Participants</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Challenges;
