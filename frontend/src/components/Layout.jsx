import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar.jsx'

export default function Layout() {
  return (
    <div className="flex h-screen overflow-hidden bg-slate-900">
      <Sidebar />
      <main className="flex-1 overflow-y-auto">
        <div className="p-6 md:p-8 max-w-7xl">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
