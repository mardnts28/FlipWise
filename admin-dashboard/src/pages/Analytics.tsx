import React from 'react';
import { BarChart3, TrendingUp, PieChart as PieIcon, Users } from 'lucide-react';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, 
  Tooltip, ResponsiveContainer, PieChart, Pie, Cell 
} from 'recharts';

const data = [
  { name: 'Language', value: 400 },
  { name: 'Science', value: 300 },
  { name: 'History', value: 200 },
  { name: 'Tech', value: 278 },
];

const COLORS = ['#7C3AED', '#F97316', '#10B981', '#FBBF24'];

const Analytics = () => {
  return (
    <div className="content-area">
      <div style={{ marginBottom: '40px' }}>
        <h1 className="h1">Detailed Analytics</h1>
        <p className="text-label">Deep dive into user cohorts, engagement heatmaps, and subject trends.</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '32px' }}>
        <div className="glass-card" style={{ padding: '32px' }}>
          <h3 className="h2" style={{ marginBottom: '32px' }}>Engagement by Category</h3>
          <div style={{ height: '350px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={data}
                  cx="50%"
                  cy="50%"
                  innerRadius={80}
                  outerRadius={120}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {data.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginTop: '24px' }}>
             {data.map((item, i) => (
               <div key={item.name} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <div style={{ width: '12px', height: '12px', borderRadius: '4px', background: COLORS[i] }} />
                  <span style={{ fontSize: '0.875rem', fontWeight: 600 }}>{item.name}: {item.value}k users</span>
               </div>
             ))}
          </div>
        </div>

        <div className="glass-card" style={{ padding: '32px' }}>
          <h3 className="h2" style={{ marginBottom: '32px' }}>Active Study Hours</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '8px', height: '300px' }}>
            {/* Heatmap Placeholder */}
            {Array.from({ length: 49 }).map((_, i) => (
              <div 
                key={i} 
                style={{ 
                  background: `rgba(124, 58, 237, ${Math.random()})`, 
                  borderRadius: '4px',
                  opacity: 0.1 + Math.random() * 0.9
                }} 
                title="Activity level"
              />
            ))}
          </div>
          <p className="text-label" style={{ marginTop: '20px', textAlign: 'center' }}>Time of day (Vertical) vs Day of week (Horizontal)</p>
        </div>
      </div>
    </div>
  );
};

export default Analytics;
