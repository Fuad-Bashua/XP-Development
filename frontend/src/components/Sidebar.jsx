import { NavLink } from 'react-router-dom'
import { Shield, LayoutDashboard, Search, ShieldAlert, GitCompare, Info, Upload } from 'lucide-react'

const NAV_ITEMS = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard'      },
  { to: '/upload',    icon: Upload,          label: 'Upload'         },
  { to: '/scans',     icon: Search,          label: 'Scans'          },
  { to: '/rules',     icon: ShieldAlert,     label: 'Security Rules' },
  { to: '/compare',   icon: GitCompare,      label: 'Compare'        },
  { to: '/about',     icon: Info,            label: 'About'          },
]

const active   = 'flex items-center gap-3 px-3 py-2.5 rounded-lg text-cyan-400 bg-cyan-950/50 border-l-2 border-cyan-400 font-medium text-sm'
const inactive = 'flex items-center gap-3 px-3 py-2.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-700/50 transition-colors text-sm border-l-2 border-transparent'

export default function Sidebar() {
  return (
    <aside className="w-56 shrink-0 h-screen bg-slate-800 border-r border-slate-700 flex flex-col">
      <div className="px-5 py-5 border-b border-slate-700">
        <div className="flex items-center gap-2.5">
          <div className="p-1.5 rounded-lg bg-cyan-500/10 border border-cyan-500/30">
            <Shield size={20} className="text-cyan-400" />
          </div>
          <div>
            <span className="text-gradient-cyan font-bold text-base leading-tight block">CodeShield</span>
            <span className="text-slate-500 text-xs">Static Analysis</span>
          </div>
        </div>
      </div>

      <nav className="flex-1 px-3 py-4 space-y-1">
        {NAV_ITEMS.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) => isActive ? active : inactive}
          >
            <Icon size={16} />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="px-5 py-4 border-t border-slate-700">
        <p className="text-slate-500 text-xs font-mono">v1.0-ITERATION4</p>
        <p className="text-slate-600 text-xs mt-0.5">Python Static Analysis</p>
      </div>
    </aside>
  )
}
