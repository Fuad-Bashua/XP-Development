import { useState } from 'react'
import { ShieldAlert } from 'lucide-react'
import PageHeader from '../components/PageHeader.jsx'
import SectionCard from '../components/SectionCard.jsx'
import RuleRow from '../components/RuleRow.jsx'
import StatusBadge from '../components/StatusBadge.jsx'
import EmptyState from '../components/EmptyState.jsx'
import { SECURITY_RULES } from '../data/mockData.js'

const CATEGORIES = [...new Set(SECURITY_RULES.map(r => r.category))]

const SEVERITIES = ['Critical', 'High', 'Medium', 'Low']

const SEVERITY_COUNT_CLASSES = {
  Critical: 'bg-red-900/40 text-red-400 border-red-700 hover:bg-red-900/60',
  High:     'bg-orange-900/40 text-orange-400 border-orange-700 hover:bg-orange-900/60',
  Medium:   'bg-amber-900/40 text-amber-400 border-amber-700 hover:bg-amber-900/60',
  Low:      'bg-blue-900/40 text-blue-400 border-blue-700 hover:bg-blue-900/60',
}

const selectClass = 'bg-slate-800 border border-slate-600 text-slate-200 text-sm rounded-lg px-3 py-2 focus:outline-none focus:border-cyan-500'

export default function SecurityRules() {
  const [search,   setSearch]   = useState('')
  const [severity, setSeverity] = useState('all')
  const [category, setCategory] = useState('all')

  const filtered = SECURITY_RULES
    .filter(r => {
      const q = search.toLowerCase()
      return r.ruleId.toLowerCase().includes(q) || r.name.toLowerCase().includes(q) || r.description.toLowerCase().includes(q)
    })
    .filter(r => severity === 'all' || r.severity === severity)
    .filter(r => category === 'all' || r.category === category)

  return (
    <div>
      <PageHeader
        title="Security Rules"
        subtitle="16 detection rules mapped to CWE references"
        icon={ShieldAlert}
        badge="16 rules"
      />

      <div className="flex flex-wrap gap-3 mb-6">
        {SEVERITIES.map(s => {
          const count = SECURITY_RULES.filter(r => r.severity === s).length
          return (
            <button
              key={s}
              onClick={() => setSeverity(prev => prev === s ? 'all' : s)}
              className={`flex items-center gap-2 px-3 py-1.5 rounded-full border text-sm font-medium transition-colors cursor-pointer ${SEVERITY_COUNT_CLASSES[s]} ${severity === s ? 'ring-2 ring-offset-1 ring-offset-slate-900 ring-current' : ''}`}
            >
              <span>{s}</span>
              <span className="text-xs opacity-70">({count})</span>
            </button>
          )
        })}
      </div>

      <div className="flex flex-wrap gap-3 mb-6">
        <div className="flex items-center gap-2 bg-slate-800 border border-slate-600 rounded-lg px-3 py-2 flex-1 min-w-48">
          <ShieldAlert size={15} className="text-slate-500 shrink-0" />
          <input
            type="text"
            placeholder="Search rules..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="bg-transparent text-slate-200 text-sm placeholder-slate-500 outline-none w-full"
          />
        </div>
        <select value={category} onChange={e => setCategory(e.target.value)} className={selectClass}>
          <option value="all">All Categories</option>
          {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
        </select>
      </div>

      <SectionCard
        title="Detection Rules"
        subtitle={`${filtered.length} of ${SECURITY_RULES.length} rules shown`}
        icon={ShieldAlert}
        className="mb-6"
      >
        {filtered.length === 0 ? (
          <EmptyState icon={ShieldAlert} title="No rules match your search" />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-slate-700 text-left">
                  <th className="pb-3 px-4 text-slate-400 font-medium text-sm">Rule ID</th>
                  <th className="pb-3 px-4 text-slate-400 font-medium text-sm">Name</th>
                  <th className="pb-3 px-4 text-slate-400 font-medium text-sm">Severity</th>
                  <th className="pb-3 px-4 text-slate-400 font-medium text-sm">CWE</th>
                  <th className="pb-3 px-4 text-slate-400 font-medium text-sm hidden md:table-cell">Category</th>
                  <th className="pb-3 px-4 text-slate-400 font-medium text-sm hidden lg:table-cell">Description</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((rule, i) => <RuleRow key={rule.ruleId} rule={rule} index={i} />)}
              </tbody>
            </table>
          </div>
        )}
      </SectionCard>

      <SectionCard title="Rules by Category" subtitle="5 detection categories">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {CATEGORIES.map(cat => {
            const rules = SECURITY_RULES.filter(r => r.category === cat)
            return (
              <div key={cat} className="bg-slate-700/30 rounded-lg p-4 border border-slate-700">
                <div className="flex items-center justify-between mb-3">
                  <h3 className="text-slate-200 font-medium text-sm">{cat}</h3>
                  <span className="text-xs bg-slate-700 text-slate-300 px-2 py-0.5 rounded-full">{rules.length}</span>
                </div>
                <div className="space-y-1.5">
                  {rules.map(r => (
                    <div key={r.ruleId} className="flex items-center gap-2">
                      <span className="font-mono text-cyan-500 text-xs w-16 shrink-0">{r.ruleId}</span>
                      <span className="text-slate-400 text-xs truncate">{r.name}</span>
                    </div>
                  ))}
                </div>
              </div>
            )
          })}
        </div>
      </SectionCard>
    </div>
  )
}
