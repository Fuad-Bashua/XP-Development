import StatusBadge from './StatusBadge.jsx'

export default function RuleRow({ rule, index }) {
  return (
    <tr className={`border-b border-slate-700/50 hover:bg-slate-700/20 transition-colors ${index % 2 === 0 ? '' : 'bg-slate-800/30'}`}>
      <td className="py-3 px-4 font-mono text-cyan-400 text-sm whitespace-nowrap">{rule.ruleId}</td>
      <td className="py-3 px-4 text-slate-200 text-sm font-medium whitespace-nowrap">{rule.name}</td>
      <td className="py-3 px-4 whitespace-nowrap">
        <StatusBadge type="severity" value={rule.severity} />
      </td>
      <td className="py-3 px-4 whitespace-nowrap">
        <span className="text-slate-400 text-xs font-mono">{rule.cwe}</span>
      </td>
      <td className="py-3 px-4 hidden md:table-cell">
        <span className="text-xs font-medium bg-slate-700 text-slate-300 px-2 py-0.5 rounded">{rule.category}</span>
      </td>
      <td className="py-3 px-4 text-slate-400 text-xs hidden lg:table-cell max-w-xs">
        <span className="line-clamp-2">{rule.description}</span>
      </td>
    </tr>
  )
}
