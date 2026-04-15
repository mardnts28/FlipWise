import React from 'react';
import { Users, BookOpen, BrainCircuit, Activity, TrendingUp } from 'lucide-react';
import { 
  AreaChart, Area, XAxis, YAxis, CartesianGrid, 
  Tooltip, ResponsiveContainer, BarChart, Bar 
} from 'recharts';

const mockData = [
  { name: 'Mon', active: 400, sessions: 240 },
  { name: 'Tue', active: 300, sessions: 139 },
  { name: 'Wed', active: 200, sessions: 980 },
  { name: 'Thu', active: 278, sessions: 390 },
  { name: 'Fri', active: 189, sessions: 480 },
  { name: 'Sat', active: 539, sessions: 380 },
  { name: 'Sun', active: 349, sessions: 430 },
];

const StatCard = ({ title, value, sub, icon: Icon, color }: any) => (
  <div className="stat-card premium-shadow">
    <div className="icon-holder" style={{ backgroundColor: `${color}15` }}>
      <Icon size={28} color={color} />
    </div>
    <div>
      <p className="text-label">{title}</p>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px' }}>
        <h3 className="h2">{value}</h3>
        <span style={{ fontSize: '0.75rem', color: '#10B981', fontWeight: 700 }}>+12.5%</span>
      </div>
      <p style={{ fontSize: '0.75rem', color: '#94A3B8' }}>{sub}</p>
    </div>
  </div>
);

const Dashboard = () => {
  return (
    <div className="content-area">
      <div style={{ marginBottom: '40px' }}>
        <h1 className="h1">Dashboard Overview</h1>
        <p className="text-label">Real-time platform metrics and system health.</p>
      </div>

      <div className="stats-grid">
        <StatCard title="Total Users" value="12,458" sub="2,450 MAU" icon={Users} color="#7C3AED" />
        <StatCard title="Decks Created" value="45,892" sub="1,200 New Today" icon={BookOpen} color="#F97316" />
        <StatCard title="Study Sessions" value="189,320" sub="8,400 Last 24h" icon={BrainCircuit} color="#10B981" />
        <StatCard title="System Health" value="99.9%" sub="All Systems Operational" icon={Activity} color="#1E1B4B" />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '24px' }}>
        <div className="glass-card" style={{ padding: '32px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '32px' }}>
            <h3 className="h2">Retention & Active Trends</h3>
            <div style={{ display: 'flex', gap: '8px' }}>
               {/* Filters Placeholder */}
            </div>
          </div>
          <div style={{ height: '350px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={mockData}>
                <defs>
                  <linearGradient id="colorActive" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#7C3AED" stopOpacity={0.1}/>
                    <stop offset="95%" stopColor="#7C3AED" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#94A3B8', fontSize: 12}} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{fill: '#94A3B8', fontSize: 12}} />
                <Tooltip />
                <Area type="monotone" dataKey="active" stroke="#7C3AED" strokeWidth={3} fillOpacity={1} fill="url(#colorActive)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="glass-card" style={{ padding: '32px' }}>
          <h3 className="h2" style={{ marginBottom: '24px' }}>Subject Engagement</h3>
          <div style={{ height: '350px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={mockData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#94A3B8', fontSize: 12}} />
                <Tooltip />
                <Bar dataKey="sessions" fill="#F97316" radius={[10, 10, 0, 0]} barSize={20} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
