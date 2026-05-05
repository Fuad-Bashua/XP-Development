export default function PageHeader({ title, subtitle, icon: Icon, badge, actions }) {
  return (
    <div className="mb-8 flex justify-between items-start">
      <div>
        <div className="flex items-center gap-3 mb-1">
          {Icon && <Icon size={26} className="text-cyan-400" />}
          <h1 className="text-2xl font-bold text-slate-50">{title}</h1>
          {badge && (
            <span className="px-2.5 py-0.5 text-xs font-medium bg-slate-700 text-slate-300 rounded-full border border-slate-600">
              {badge}
            </span>
          )}
        </div>
        {subtitle && <p className="text-slate-400 text-sm">{subtitle}</p>}
      </div>
      {actions && <div className="flex items-center gap-2">{actions}</div>}
    </div>
  )
}
