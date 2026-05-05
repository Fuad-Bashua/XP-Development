import { LayoutDashboard, FileCode, Layers, AlertOctagon, Activity, AlertTriangle, Clock, CheckCircle } from 'lucide-react'
import PageHeader from '../components/PageHeader.jsx'
import StatCard from '../components/StatCard.jsx'
import SectionCard from '../components/SectionCard.jsx'
import ModuleCard from '../components/ModuleCard.jsx'
import EmptyState from '../components/EmptyState.jsx'
import { MODULES, SUMMARY, RECENT_ACTIVITY } from '../data/mockData.js'

const ACTIVITY_DOT = {
  scan:   'bg-cyan-400',
  fix:    'bg-green-400',
  alert:  'bg-red-400',
  export: 'bg-blue-400',
}

function tdiBarColor(tdi) {
  if (tdi >= 50) return 'bg-red-500'
  if (tdi >= 30) return 'bg-amber-500'
  if (tdi >= 10) return 'bg-blue-500'
  return 'bg-green-500'
}

function tdiTextColor(tdi) {
  if (tdi >= 50) return 'text-red-400'
  if (tdi >= 30) return 'text-amber-400'
  if (tdi >= 10) return 'text-blue-400'
  return 'text-green-400'
}

export default function Dashboard() {
  const highRisk = MODULES.filter(m => m.isHighRisk)
  const sorted   = [...MODULES].sort((a, b) => b.tdi - a.tdi)

  return (
    <div>
      <PageHeader
        title="Dashboard"
        subtitle="PaymentService v2.4.1 — Last scan 2 hours ago"
        icon={LayoutDashboard}
      />

      {SUMMARY.highRiskModules > 0 && (
        <div className="mb-6 flex items-start gap-3 px-4 py-3 rounded-xl bg-red-900/20 border border-red-700 text-red-300">
          <AlertTriangle size={18} className="text-red-400 mt-0.5 shrink-0" />
          <p className="text-sm">
            <span className="font-bold">{SUMMARY.highRiskModules} module</span> classified as{' '}
            <span className="font-bold">HIGH RISK</span>. Immediate refactoring recommended.
          </p>
        </div>
      )}

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <StatCard
          label="Total Lines of Code"
          value={SUMMARY.totalLoc.toLocaleString()}
          icon={FileCode}
          iconColor="text-cyan-400"
        />
        <StatCard
          label="Modules Scanned"
          value={String(SUMMARY.analysedModules)}
          icon={Layers}
          iconColor="text-blue-400"
        />
        <StatCard
          label="Total Red Flags"
          value={String(SUMMARY.totalRedFlags)}
          icon={AlertOctagon}
          iconColor="text-red-400"
          accent={SUMMARY.totalRedFlags > 0}
        />
        <StatCard
          label="Average TDI"
          value={SUMMARY.averageTdi.toFixed(2)}
          icon={Activity}
          iconColor="text-amber-400"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
        <div className="lg:col-span-2">
          <SectionCard title="High Risk Modules" icon={AlertTriangle}>
            {highRisk.length === 0 ? (
              <EmptyState icon={CheckCircle} title="No high-risk modules" description="All modules are within acceptable thresholds." />
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {highRisk.map(m => <ModuleCard key={m.filename} module={m} />)}
              </div>
            )}
          </SectionCard>
        </div>

        <div>
          <SectionCard title="Recent Activity" icon={Clock}>
            <div className="space-y-4">
              {RECENT_ACTIVITY.map(item => (
                <div key={item.id} className="flex items-start gap-3">
                  <div className={`mt-1.5 w-2 h-2 rounded-full shrink-0 ${ACTIVITY_DOT[item.type] ?? 'bg-slate-400'}`} />
                  <div className="min-w-0">
                    <p className="text-slate-200 text-sm font-medium">{item.event}</p>
                    <p className="text-slate-400 text-xs truncate">{item.detail}</p>
                    <p className="text-slate-600 text-xs mt-0.5">{item.time}</p>
                  </div>
                </div>
              ))}
            </div>
          </SectionCard>
        </div>
      </div>

      <SectionCard
        title="All Modules — TDI Overview"
        subtitle="Technical Debt Index sorted highest to lowest"
        icon={Activity}
      >
        <div className="space-y-3">
          {sorted.map(m => (
            <div key={m.filename} className="flex items-center gap-4">
              <span className="font-mono text-slate-300 text-sm w-44 shrink-0 truncate">{m.filename}</span>
              <div className="flex-1 h-2.5 bg-slate-700 rounded-full overflow-hidden">
                <div
                  className={`h-full rounded-full ${tdiBarColor(m.tdi)}`}
                  style={{ width: `${Math.min(m.tdi, 100)}%` }}
                />
              </div>
              <span className={`text-sm font-bold w-14 text-right ${tdiTextColor(m.tdi)}`}>
                {m.tdi.toFixed(2)}
              </span>
            </div>
          ))}
        </div>
      </SectionCard>
    </div>
  )
}
