export default function SectionCard({ title, subtitle, icon: Icon, children, className = '', action }) {
  return (
    <div className={`bg-slate-800 rounded-xl border border-slate-700 ${className}`}>
      <div className="px-6 py-4 border-b border-slate-700 flex justify-between items-center">
        <div className="flex items-center gap-2">
          {Icon && <Icon size={18} className="text-cyan-400" />}
          <div>
            <h2 className="text-slate-50 font-semibold">{title}</h2>
            {subtitle && <p className="text-slate-400 text-xs mt-0.5">{subtitle}</p>}
          </div>
        </div>
        {action && <div>{action}</div>}
      </div>
      <div className="p-6">{children}</div>
    </div>
  )
}
