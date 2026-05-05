import { Inbox } from 'lucide-react'

export default function EmptyState({ icon: Icon = Inbox, title, description, action }) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      <div className="p-4 rounded-full bg-slate-700/50 mb-4">
        <Icon size={32} className="text-slate-500" />
      </div>
      <h3 className="text-slate-300 font-semibold text-lg">{title}</h3>
      {description && <p className="text-slate-500 text-sm mt-1 max-w-xs">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  )
}
