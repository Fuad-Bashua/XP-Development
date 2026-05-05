import { useState } from 'react'
import { GitCompare, TrendingDown, Minus, ChevronDown } from 'lucide-react'
import PageHeader from '../components/PageHeader.jsx'
import SectionCard from '../components/SectionCard.jsx'
import StatusBadge from '../components/StatusBadge.jsx'
import { COMPARISON } from '../data/mockData.js'

const METRICS = [
  { key: 'tdi',        label: 'TDI Score',       before: 'tdiBefore',        after: 'tdiAfter',        delta: 'tdiDelta'        },
  { key: 'complexity', label: 'Max Complexity',   before: 'complexityBefore', after: 'complexityAfter', delta: 'complexityDelta' },
  { key: 'vuln',       label: 'Vuln Density',     before: 'vulnBefore',       after: 'vulnAfter',       delta: 'vulnDelta'       },
]

function shortRisk(v) {
  if (v.includes('High Risk')) return 'High Risk'
  if (v.includes('Moderate'))  return 'Moderate'
  if (v.includes('Low Risk'))  return 'Low Risk'
  return 'Minimal'
}

function fmt(v, isInt = false) {
  return isInt ? v : v.toFixed(2)
}

export default function Compare() {
  const [activeMetric, setActiveMetric] = useState('tdi')
  const m = METRICS.find(x => x.key === activeMetric)
  const isInt = activeMetric === 'complexity'
  const { summary } = COMPARISON

  const maxBefore = Math.max(...COMPARISON.modules.map(r => r[m.before]))

  return (
    <div>
      <PageHeader
        title="Before / After Comparison"
        subtitle={COMPARISON.label}
        icon={GitCompare}
      />

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        <div className="bg-slate-800 rounded-xl border border-red-900/50 p-5">
          <p className="text-red-400 text-xs font-semibold uppercase tracking-wider mb-1">Before</p>
          <p className="text-slate-50 font-bold text-lg">{COMPARISON.scanBefore.projectName}</p>
          <p className="text-slate-400 text-sm">{COMPARISON.scanBefore.date.split('T')[0]}</p>
          <p className="mt-3 text-slate-300 text-sm">Avg TDI: <span className="text-red-400 font-bold">{summary.averageTdiBefore.toFixed(2)}</span></p>
        </div>
        <div className="bg-slate-800 rounded-xl border border-green-900/50 p-5">
          <p className="text-green-400 text-xs font-semibold uppercase tracking-wider mb-1">After</p>
          <p className="text-slate-50 font-bold text-lg">{COMPARISON.scanAfter.projectName}</p>
          <p className="text-slate-400 text-sm">{COMPARISON.scanAfter.date.split('T')[0]}</p>
          <p className="mt-3 text-slate-300 text-sm">Avg TDI: <span className="text-green-400 font-bold">{summary.averageTdiAfter.toFixed(2)}</span></p>
        </div>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-slate-800 border border-slate-700 rounded-xl p-4 text-center">
          <p className="text-green-400 text-2xl font-bold">{summary.modulesImproved}</p>
          <p className="text-slate-400 text-xs mt-1">Modules Improved</p>
        </div>
        <div className="bg-slate-800 border border-slate-700 rounded-xl p-4 text-center">
          <p className="text-slate-300 text-2xl font-bold">{summary.modulesUnchanged}</p>
          <p className="text-slate-400 text-xs mt-1">Unchanged</p>
        </div>
        <div className="bg-slate-800 border border-slate-700 rounded-xl p-4 text-center">
          <p className="text-green-400 text-2xl font-bold">
            {(summary.averageTdiBefore - summary.averageTdiAfter).toFixed(2)}
          </p>
          <p className="text-slate-400 text-xs mt-1">Avg TDI Reduction</p>
        </div>
        <div className="bg-slate-800 border border-slate-700 rounded-xl p-4 text-center">
          <p className="text-green-400 text-2xl font-bold">{summary.trend}</p>
          <p className="text-slate-400 text-xs mt-1">Overall Trend</p>
        </div>
      </div>

      <div className="flex gap-2 mb-6">
        {METRICS.map(metric => (
          <button
            key={metric.key}
            onClick={() => setActiveMetric(metric.key)}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              activeMetric === metric.key
                ? 'bg-cyan-600 text-white'
                : 'bg-slate-800 text-slate-400 border border-slate-700 hover:text-slate-200 hover:border-slate-500'
            }`}
          >
            {metric.label}
          </button>
        ))}
      </div>

      <SectionCard title="Module-by-Module Delta" subtitle={`Showing: ${m.label}`} className="mb-6">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-700 text-left">
                <th className="pb-3 px-4 text-slate-400 font-medium">Module</th>
                <th className="pb-3 px-4 text-slate-400 font-medium">Before</th>
                <th className="pb-3 px-4 text-slate-400 font-medium">After</th>
                <th className="pb-3 px-4 text-slate-400 font-medium">Delta</th>
                <th className="pb-3 px-4 text-slate-400 font-medium">Trend</th>
                <th className="pb-3 px-4 text-slate-400 font-medium hidden md:table-cell">Risk Change</th>
              </tr>
            </thead>
            <tbody>
              {COMPARISON.modules.map(row => {
                const delta = row[m.delta]
                const deltaColor = delta < 0 ? 'text-green-400' : delta > 0 ? 'text-red-400' : 'text-slate-400'
                const prefix = delta > 0 ? '+' : ''
                return (
                  <tr key={row.filename} className="border-b border-slate-700/50 hover:bg-slate-700/20 transition-colors">
                    <td className="py-3 px-4 font-mono text-cyan-400">{row.filename}</td>
                    <td className="py-3 px-4 text-slate-300">{fmt(row[m.before], isInt)}</td>
                    <td className="py-3 px-4 text-slate-300">{fmt(row[m.after], isInt)}</td>
                    <td className={`py-3 px-4 font-bold ${deltaColor}`}>{prefix}{fmt(delta, isInt)}</td>
                    <td className="py-3 px-4">
                      {row.improved
                        ? <ChevronDown size={16} className="text-green-400" />
                        : <Minus size={16} className="text-slate-500" />
                      }
                    </td>
                    <td className="py-3 px-4 hidden md:table-cell">
                      {row.riskBefore !== row.riskAfter ? (
                        <span className="text-xs text-slate-300">
                          <span className="text-red-400">{shortRisk(row.riskBefore)}</span>
                          {' → '}
                          <span className="text-green-400">{shortRisk(row.riskAfter)}</span>
                        </span>
                      ) : (
                        <span className="text-slate-500 text-xs">Unchanged</span>
                      )}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      </SectionCard>

      <SectionCard title="Visual Delta — TDI Before vs After" subtitle="Bar length proportional to TDI score">
        <div className="space-y-4">
          {COMPARISON.modules.map(row => (
            <div key={row.filename}>
              <div className="flex items-center justify-between mb-1">
                <span className="font-mono text-slate-300 text-xs">{row.filename}</span>
                <span className="text-xs text-green-400">{row.tdiDelta < 0 ? `▼ ${Math.abs(row.tdiDelta).toFixed(2)}` : '—'}</span>
              </div>
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="text-xs text-slate-500 w-12">Before</span>
                  <div className="flex-1 h-2 bg-slate-700 rounded-full overflow-hidden">
                    <div className="h-full bg-red-500/70 rounded-full" style={{ width: `${Math.min(row.tdiBefore, 100)}%` }} />
                  </div>
                  <span className="text-xs text-red-400 w-12 text-right">{row.tdiBefore.toFixed(1)}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs text-slate-500 w-12">After</span>
                  <div className="flex-1 h-2 bg-slate-700 rounded-full overflow-hidden">
                    <div className="h-full bg-green-500/70 rounded-full" style={{ width: `${Math.min(row.tdiAfter, 100)}%` }} />
                  </div>
                  <span className="text-xs text-green-400 w-12 text-right">{row.tdiAfter.toFixed(1)}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </SectionCard>
    </div>
  )
}
