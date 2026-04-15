import React from 'react';
import { Package, Star, Trash2, AlertTriangle, ExternalLink } from 'lucide-react';

const mockDecks = [
  { id: 'd1', name: 'Advanced Neurobiology', author: 'Dr. Smith', category: 'Science', cards: 154, status: 'Global', trending: true },
  { id: 'd2', name: 'Learn Japanese Kanji', author: 'Yuki_T', category: 'Language', cards: 800, status: 'Reported', trending: false },
  { id: 'd3', name: 'World Capitals Quiz', author: 'Global_Geo', category: 'Trivia', cards: 200, status: 'Featured', trending: true },
  { id: 'd4', name: 'React Hooks Deep Dive', author: 'DevMode', category: 'Technology', cards: 45, status: 'Global', trending: false },
];

const ContentModeration = () => {
  return (
    <div className="content-area">
      <div style={{ marginBottom: '40px' }}>
        <h1 className="h1">Content Moderation</h1>
        <p className="text-label">Review global decks, manage featured content, and handle reports.</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '24px' }}>
        {mockDecks.map((deck) => (
          <div key={deck.id} className="glass-card premium-shadow" style={{ padding: '24px', display: 'flex', flexDirection: 'column' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
              <div style={{ padding: '12px', background: '#F5F3FF', borderRadius: '16px', color: '#7C3AED' }}>
                <Package size={24} />
              </div>
              {deck.status === 'Reported' && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '4px', background: '#FEE2E2', color: '#EF4444', padding: '6px 12px', borderRadius: '8px', fontSize: '0.75rem', fontWeight: 700 }}>
                  <AlertTriangle size={14} /> Reported
                </div>
              )}
              {deck.status === 'Featured' && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '4px', background: '#FEF3C7', color: '#D97706', padding: '6px 12px', borderRadius: '8px', fontSize: '0.75rem', fontWeight: 700 }}>
                  <Star size={14} /> Featured
                </div>
              )}
            </div>

            <h3 style={{ fontSize: '1.25rem', fontWeight: 700, color: '#1E1B4B', marginBottom: '4px' }}>{deck.name}</h3>
            <p className="text-label" style={{ marginBottom: '20px' }}>By {deck.author} • {deck.cards} cards</p>
            
            <div style={{ display: 'flex', gap: '12px', marginBottom: '24px' }}>
              <span style={{ fontSize: '0.75rem', padding: '4px 10px', background: '#F1F5F9', borderRadius: '6px', color: '#64748B' }}>{deck.category}</span>
              {deck.trending && <span style={{ fontSize: '0.75rem', padding: '4px 10px', background: '#FEE2E2', borderRadius: '6px', color: '#EF4444' }}>🔥 Trending</span>}
            </div>

            <div style={{ marginTop: 'auto', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <button style={{ 
                padding: '12px', 
                borderRadius: '12px', 
                background: '#7C3AED', 
                color: 'white', 
                fontWeight: 600,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '8px'
              }}>
                <Star size={18} /> {deck.status === 'Featured' ? 'Unfeature' : 'Feature'}
              </button>
              <button style={{ 
                padding: '12px', 
                borderRadius: '12px', 
                background: '#F1F5F9', 
                color: '#EF4444', 
                fontWeight: 600,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '8px'
              }}>
                <Trash2 size={18} /> Remove
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ContentModeration;
