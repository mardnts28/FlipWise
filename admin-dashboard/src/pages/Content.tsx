import { useState, useEffect } from 'react';
import { Package, Star, Trash2, Loader2 } from 'lucide-react';
import { rtdb } from '../firebase';
import { ref, onValue, remove, update } from 'firebase/database';

interface Deck {
  id: string;
  name: string;
  author: string;
  category: string;
  cardCount: number;
  status: string;
}

const ContentModeration = () => {
  const [decks, setDecks] = useState<Deck[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const decksRef = ref(rtdb, 'community_decks');
    const unsubscribe = onValue(decksRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        setDecks(Object.entries(data).map(([id, val]: [string, any]) => ({
          id, ...val
        })));
      } else {
        setDecks([]);
      }
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  const handleToggleFeatured = async (deckId: string, currentStatus: string) => {
    const newStatus = currentStatus === 'Featured' ? 'Global' : 'Featured';
    await update(ref(rtdb, `community_decks/${deckId}`), { status: newStatus });
  };

  const handleDelete = async (deckId: string) => {
    if (window.confirm("Remove this deck from community?")) {
      await remove(ref(rtdb, `community_decks/${deckId}`));
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
      <div style={{ marginBottom: '40px' }}>
        <h1 className="h1">Content Moderation</h1>
        <p className="text-label">Review global decks, manage featured content, and handle reports.</p>
      </div>

      {decks.length === 0 ? (
        <div className="glass-card" style={{ padding: '40px', textAlign: 'center' }}>
          <Package size={48} color="#94A3B8" style={{ marginBottom: '16px' }} />
          <p className="text-label">No community decks found.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '24px' }}>
          {decks.map((deck) => (
            <div key={deck.id} className="glass-card premium-shadow" style={{ padding: '24px', display: 'flex', flexDirection: 'column' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
                <div style={{ padding: '12px', background: '#F5F3FF', borderRadius: '16px', color: '#7C3AED' }}>
                  <Package size={24} />
                </div>
                {deck.status === 'Featured' && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: '4px', background: '#FEF3C7', color: '#D97706', padding: '6px 12px', borderRadius: '8px', fontSize: '0.75rem', fontWeight: 700 }}>
                    <Star size={14} /> Featured
                  </div>
                )}
              </div>

              <h3 style={{ fontSize: '1.25rem', fontWeight: 700, color: '#1E1B4B', marginBottom: '4px' }}>{deck.name}</h3>
              <p className="text-label" style={{ marginBottom: '20px' }}>By {deck.author} • {deck.cardCount} cards</p>
              
              <div style={{ marginTop: 'auto', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <button 
                  onClick={() => handleToggleFeatured(deck.id, deck.status)}
                  style={{ padding: '12px', borderRadius: '12px', background: '#7C3AED', color: 'white', fontWeight: 600, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
                >
                  <Star size={18} /> {deck.status === 'Featured' ? 'Unfeature' : 'Feature'}
                </button>
                <button 
                  onClick={() => handleDelete(deck.id)}
                  style={{ padding: '12px', borderRadius: '12px', background: '#F1F5F9', color: '#EF4444', fontWeight: 600, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
                >
                  <Trash2 size={18} /> Remove
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ContentModeration;
