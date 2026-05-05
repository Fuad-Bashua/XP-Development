export default function StatCard({ label, value, icon: Icon, iconColor = 'text-cyan-400', trend, trendUp, accent = false }) {
  const trendColor = trendUp === true ? 'text-green-400' : trendUp === false ? 'text-red-400' : 'text-slate-400'
  const border = accent ? 'border-cyan-700' : 'border-slate-700'

  return (
    <div className={`bg-slate-800 rounded-xl p-5 border ${border}`}>
      <div className="flex justify-between items-start">
        <div>
          <p className="text-slate-400 text-sm">{label}</p>
          <p className="text-3xl font-bold text-slate-50 mt-1">{value}</p>
          {trend && <p className={`text-xs mt-1 ${trendColor}`}>{trend}</p>}
        </div>
        <div className="p-2 rounded-lg bg-slate-700">
          <Icon size={20} className={iconColor} />
        </div>
      </div>
    </div>
  )
}
