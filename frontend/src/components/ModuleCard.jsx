import StatusBadge from './StatusBadge.jsx'

function tdiColor(tdi) {
  if (tdi >= 50) return 'text-red-400'
  if (tdi >= 30) return 'text-amber-400'
  if (tdi >= 10) return 'text-blue-400'
  return 'text-green-400'
}

function tdiBarColor(tdi) {
  if (tdi >= 50) return 'bg-red-500'
  if (tdi >= 30) return 'bg-amber-500'
  if (tdi >= 10) return 'bg-blue-500'
  return 'bg-green-500'
}

export default function ModuleCard({ module }) {
  const criticalCount = module.redFlags.filter(f => f.severity === 'Critical').length
  const highCount     = module.redFlags.filter(f => f.severity === 'High').length
  const mediumCount   = module.redFlags.filter(f => f.severity === 'Medium').length

  const topFunctions = [...module.functions]
    .sort((a, b) => b.cyclomaticComplexity - a.cyclomaticComplexity)
    .slice(0, 3)

  const barWidth = Math.min(module.tdi, 100)

  return (
    <div className="bg-slate-800 rounded-xl border border-slate-700 p-5 hover:border-slate-600 transition-colors">
      <div className="flex items-start justify-between mb-4">
        <span className="font-mono text-cyan-400 text-sm font-medium truncate max-w-[180px]">
          {module.filename}
        </span>
        {module.isHighRisk && (
          <span className="text-xs font-bold text-red-400 bg-red-900/30 border border-red-700 px-2 py-0.5 rounded-full ml-2 shrink-0">
            HIGH RISK
          </span>
        )}
      </div>

      <div className="grid grid-cols-3 gap-2 mb-4 text-center">
        <div>
          <p className="text-slate-400 text-xs">LOC</p>
          <p className="text-slate-50 font-semibold">{module.loc}</p>
        </div>
        <div>
          <p className="text-slate-400 text-xs">Functions</p>
          <p className="text-slate-50 font-semibold">{module.numFunctions}</p>
        </div>
        <div>
          <p className="text-slate-400 text-xs">Max CC</p>
          <p className="text-slate-50 font-semibold">{module.complexityScore}</p>
        </div>
      </div>

      <div className="mb-4">
        <div className="flex justify-between items-center mb-1">
          <span className="text-slate-400 text-xs">TDI Score</span>
          <span className={`font-bold text-lg ${tdiColor(module.tdi)}`}>{module.tdi.toFixed(2)}</span>
        </div>
        <div className="h-2 bg-slate-700 rounded-full overflow-hidden">
          <div
            className={`h-full rounded-full transition-all ${tdiBarColor(module.tdi)}`}
            style={{ width: `${barWidth}%` }}
          />
        </div>
      </div>

      <div className="mb-4">
        <StatusBadge type="risk" value={module.riskClassification} />
      </div>

      {module.redFlags.length > 0 && (
        <div className="mb-4 flex items-center gap-2 text-xs">
          <span className="text-slate-400">{module.redFlags.length} flags:</span>
          {criticalCount > 0 && <span className="text-red-400 font-medium">{criticalCount}C</span>}
          {highCount     > 0 && <span className="text-orange-400 font-medium">{highCount}H</span>}
          {mediumCount   > 0 && <span className="text-amber-400 font-medium">{mediumCount}M</span>}
        </div>
      )}

      <div className="space-y-1">
        <p className="text-slate-500 text-xs mb-1">Top functions by complexity</p>
        {topFunctions.map(fn => (
          <div key={fn.name} className="flex justify-between items-center">
            <span className="font-mono text-slate-300 text-xs truncate max-w-[150px]">{fn.name}</span>
            <span className="text-xs font-medium text-slate-400 ml-2">CC {fn.cyclomaticComplexity}</span>
          </div>
        ))}
      </div>
    </div>
  )
}
