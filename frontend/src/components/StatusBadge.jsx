const SEVERITY_CLASSES = {
  Critical: 'bg-red-900/50 text-red-400 border-red-700',
  High:     'bg-orange-900/50 text-orange-400 border-orange-700',
  Medium:   'bg-amber-900/50 text-amber-400 border-amber-700',
  Low:      'bg-blue-900/50 text-blue-400 border-blue-700',
}

const RISK_CLASSES = {
  high:     'bg-red-900/50 text-red-400 border-red-700',
  moderate: 'bg-amber-900/50 text-amber-400 border-amber-700',
  low:      'bg-blue-900/50 text-blue-400 border-blue-700',
  minimal:  'bg-green-900/50 text-green-400 border-green-700',
}

function shortRisk(value) {
  if (value.includes('High Risk')) return 'High Risk'
  if (value.includes('Moderate'))  return 'Moderate'
  if (value.includes('Low Risk'))  return 'Low Risk'
  if (value.includes('Minimal'))   return 'Minimal'
  return value
}

function riskKey(value) {
  if (value.includes('High Risk')) return 'high'
  if (value.includes('Moderate'))  return 'moderate'
  if (value.includes('Low Risk'))  return 'low'
  return 'minimal'
}

export default function StatusBadge({ value, type = 'severity', size = 'sm' }) {
  const padding = size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-3 py-1 text-sm'

  let classes, label
  if (type === 'severity') {
    classes = SEVERITY_CLASSES[value] ?? 'bg-slate-700 text-slate-300 border-slate-600'
    label = value
  } else {
    classes = RISK_CLASSES[riskKey(value)] ?? 'bg-slate-700 text-slate-300 border-slate-600'
    label = shortRisk(value)
  }

  return (
    <span className={`inline-flex items-center font-medium rounded-full border ${padding} ${classes}`}>
      {label}
    </span>
  )
}
