import { useState } from 'react'
import { Search, ArrowUp, ArrowDown } from 'lucide-react'
import PageHeader from '../components/PageHeader.jsx'
import SectionCard from '../components/SectionCard.jsx'
import StatusBadge from '../components/StatusBadge.jsx'
import EmptyState from '../components/EmptyState.jsx'
import { MODULES, SCAN_METADATA } from '../data/mockData.js'

const SORT_FIELDS = [
  { value: 'tdi',                label: 'TDI Score'       },
  { value: 'loc',                label: 'Lines of Code'   },
  { value: 'complexityScore',    label: 'Max Complexity'  },
  { value: 'vulnerabilityDensity', label: 'Vuln Density'  },
]

const RISK_FILTERS = [
  { value: 'all',      label: 'All Risk Levels' },
  { value: 'high',     label: 'High Risk'       },
  { value: 'moderate', label: 'Moderate'        },
  { value: 'low',      label: 'Low Risk'        },
  { value: 'minimal',  label: 'Minimal'         },
]

function tdiColor(tdi) {
  if (tdi >= 50) return 'text-red-400'
  if (tdi >= 30) return 'text-amber-400'
  if (tdi >= 10) return 'text-blue-400'
  return 'text-green-400'
}

const selectClass = 'bg-slate-800 border border-slate-600 text-slate-200 text-sm rounded-lg px-3 py-2 focus:outline-none focus:border-cyan-500'

export default function Scans() {
  const [search,    setSearch]    = useState('')
  const [sortField, setSortField] = useState('tdi')
  const [sortDir,   setSortDir]   = useState('desc')
  const [riskFilter, setRiskFilter] = useState('all')

  const filtered = MODULES
    .filter(m => m.filename.toLowerCase().includes(search.toLowerCase()))
    .filter(m => {
      if (riskFilter === 'all')      return true
      if (riskFilter === 'high')     return m.riskClassification.includes('High Risk')
      if (riskFilter === 'moderate') return m.riskClassification.includes('Moderate')
      if (riskFilter === 'low')      return m.riskClassification.includes('Low Risk')
      if (riskFilter === 'minimal')  return m.riskClassification.includes('Minimal')
      return true
    })
    .sort((a, b) => {
      const dir = sortDir === 'asc' ? 1 : -1
      return (a[sortField] - b[sortField]) * dir
    })

  return (
    <div>
      <PageHeader
        title="Module Scans"
        subtitle={`${SCAN_METADATA.projectName} — scanned by ${SCAN_METADATA.scannedBy}`}
        icon={Search}
        badge={`${MODULES.length} modules`}
      />

      <div className="flex flex-wrap gap-3 mb-6">
        <div className="flex items-center gap-2 bg-slate-800 border border-slate-600 rounded-lg px-3 py-2 flex-1 min-w-48">
          <Search size={15} className="text-slate-500 shrink-0" />
          <input
            type="text"
            placeholder="Filter by filename..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="bg-transparent text-slate-200 text-sm placeholder-slate-500 outline-none w-full"
          />
        </div>
        <select value={riskFilter} onChange={e => setRiskFilter(e.target.value)} className={selectClass}>
          {RISK_FILTERS.map(f => <option key={f.value} value={f.value}>{f.label}</option>)}
        </select>
        <select value={sortField} onChange={e => setSortField(e.target.value)} className={selectClass}>
          {SORT_FIELDS.map(f => <option key={f.value} value={f.value}>Sort by {f.label}</option>)}
        </select>
        <button
          onClick={() => setSortDir(d => d === 'asc' ? 'desc' : 'asc')}
          className="p-2 bg-slate-800 border border-slate-600 rounded-lg text-slate-400 hover:text-slate-200 hover:border-slate-500 transition-colors"
        >
          {sortDir === 'asc' ? <ArrowUp size={16} /> : <ArrowDown size={16} />}
        </button>
      </div>

      <SectionCard title="Analysis Results" subtitle={`${filtered.length} of ${MODULES.length} modules shown`}>
        {filtered.length === 0 ? (
          <EmptyState icon={Search} title="No modules match your filter" description="Try adjusting the search or risk filter." />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-700 text-left">
                  <th className="pb-3 px-4 text-slate-400 font-medium">Module</th>
                  <th className="pb-3 px-4 text-slate-400 font-medium">LOC</th>
                  <th className="pb-3 px-4 text-slate-400 font-medium">Fns</th>
                  <th className="pb-3 px-4 text-slate-400 font-medium">Max CC</th>
                  <th className="pb-3 px-4 text-slate-400 font-medium">Vuln Density</th>
                  <th className="pb-3 px-4 text-slate-400 font-medium">TDI</th>
                  <th className="pb-3 px-4 text-slate-400 font-medium">Risk</th>
                  <th className="pb-3 px-4 text-slate-400 font-medium">Flags</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map(m => (
                  <tr key={m.filename} className="border-b border-slate-700/50 hover:bg-slate-700/20 transition-colors">
                    <td className="py-3 px-4 font-mono text-cyan-400">{m.filename}</td>
                    <td className="py-3 px-4 text-slate-300">{m.loc.toLocaleString()}</td>
                    <td className="py-3 px-4 text-slate-300">{m.numFunctions}</td>
                    <td className="py-3 px-4 text-slate-300">{m.complexityScore}</td>
                    <td className="py-3 px-4 text-slate-300">{m.vulnerabilityDensity.toFixed(2)}</td>
                    <td className={`py-3 px-4 font-bold ${tdiColor(m.tdi)}`}>{m.tdi.toFixed(2)}</td>
                    <td className="py-3 px-4"><StatusBadge type="risk" value={m.riskClassification} /></td>
                    <td className="py-3 px-4 text-slate-300">{m.redFlags.length}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </SectionCard>
    </div>
  )
}
