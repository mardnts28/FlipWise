import { useEffect, useState } from 'react';
import { Plus, Loader2, Trash2 } from 'lucide-react';
import { rtdb } from '../firebase';
import { ref, onValue, push, set, remove } from 'firebase/database';

interface Challenge {
  id: string;
  name: string;
  goal: number;
  goalType: string;
  status: string;
  endDate: number;
  description: string;
}

const Challenges = () => {
  const [challenges, setChallenges] = useState<Challenge[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [newChallenge, setNewChallenge] = useState({
    name: '',
    goal: 1000,
    goalType: 'Cards',
    description: '',
    duration: 7
  });

  useEffect(() => {
    const challengesRef = ref(rtdb, 'challenges');
    const unsubscribe = onValue(challengesRef, (snapshot) => {
      try {
        const data = snapshot.val();
        if (data) {
          setChallenges(Object.entries(data).map(([id, val]: [string, any]) => ({
            id,
            ...val
          })));
        } else {
          setChallenges([]);
        }
        setLoading(false);
      } catch (err) {
        console.error("Data processing error:", err);
        setError("Error parsing challenge data.");
        setLoading(false);
      }
    }, (err) => {
      console.error("Firebase error:", err);
      setError("Permission Denied or Connection Error.");
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  const handleCreate = async () => {
    if (!newChallenge.name) return;
    try {
      const challengesRef = ref(rtdb, 'challenges');
      const newRef = push(challengesRef);
      await set(newRef, {
        name: newChallenge.name,
        description: newChallenge.description,
        goal: newChallenge.goal || 0,
        goalType: newChallenge.goalType || 'Cards',
        status: 'active',
        startDate: Date.now(),
        endDate: Date.now() + (newChallenge.duration * 86400000),
        createdAt: Date.now()
      });
      setShowForm(false);
    } catch (err) {
      alert("Failed to create challenge. Check your permissions.");
    }
  };

  const handleDelete = async (id: string) => {
    if (window.confirm("Delete this challenge?")) {
      await remove(ref(rtdb, `challenges/${id}`));
    }
  };

  if (error) {
    return (
      <div className="content-area" style={{ textAlign: 'center', paddingTop: '100px' }}>
        <h2 className="h2">Unable to Load Challenges</h2>
        <p className="text-label">{error}</p>
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
          <h1 className="h1">Global Challenges</h1>
          <p className="text-label">Manage platform-wide gamification and collective goals.</p>
        </div>
        <button 
          onClick={() => setShowForm(!showForm)}
          style={{ 
            display: 'flex', alignItems: 'center', gap: '8px', padding: '12px 24px', 
            background: '#7C3AED', color: 'white', fontWeight: 700, borderRadius: '16px' 
          }}
        >
          <Plus size={20} /> {showForm ? 'Cancel' : 'New Challenge'}
        </button>
      </div>

      {showForm && (
        <div className="glass-card" style={{ padding: '32px', marginBottom: '32px' }}>
            <h3 className="h2" style={{ marginBottom: '24px' }}>Create New Challenge</h3>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                <input 
                  type="text" placeholder="Challenge Name" 
                  value={newChallenge.name} onChange={e => setNewChallenge({...newChallenge, name: e.target.value})}
                  style={{ padding: '12px', borderRadius: '12px', border: '1px solid #E2E8F0' }}
                />
                <input 
                   type="number" placeholder="Target Goal" 
                   value={newChallenge.goal} onChange={e => setNewChallenge({...newChallenge, goal: parseInt(e.target.value)})}
                   style={{ padding: '12px', borderRadius: '12px', border: '1px solid #E2E8F0' }}
                />
                <textarea 
                   placeholder="Description" 
                   value={newChallenge.description} onChange={e => setNewChallenge({...newChallenge, description: e.target.value})}
                   style={{ padding: '12px', borderRadius: '12px', border: '1px solid #E2E8F0', gridColumn: 'span 2' }}
                />
                <button onClick={handleCreate} style={{ padding: '12px', background: '#7C3AED', color: 'white', borderRadius: '12px', fontWeight: 700 }}>Launch Challenge</button>
            </div>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '24px' }}>
        {challenges.map((item) => (
          <div key={item.id} className="glass-card premium-shadow" style={{ padding: '32px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '24px' }}>
              <span style={{ fontSize: '0.75rem', fontWeight: 700, padding: '4px 10px', background: '#F5F3FF', color: '#7C3AED', borderRadius: '8px' }}>
                {item.goalType || 'Challenge'} Event
              </span>
              <span style={{ fontSize: '0.875rem', color: '#F97316', fontWeight: 600 }}>
                {item.endDate && new Date(item.endDate) > new Date() ? 'Active' : 'Ended'}
              </span>
            </div>

            <h3 className="h2" style={{ marginBottom: '8px' }}>{item.name || 'Untitled Challenge'}</h3>
            <p className="text-label" style={{ marginBottom: '24px' }}>Goal: {(item.goal || 0).toLocaleString()} {item.goalType || ''}</p>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
               <button style={{ padding: '12px', borderRadius: '12px', background: '#F1F5F9', color: '#1E1B4B', fontWeight: 600 }}>Edit Rules</button>
               <button 
                onClick={() => handleDelete(item.id)}
                style={{ padding: '12px', borderRadius: '12px', background: '#FEE2E2', color: '#EF4444', fontWeight: 600, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
               >
                 <Trash2 size={18} /> Delete
               </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Challenges;
