import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Scans from './pages/Scans.jsx'
import SecurityRules from './pages/SecurityRules.jsx'
import Compare from './pages/Compare.jsx'
import About from './pages/About.jsx'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="scans"     element={<Scans />} />
        <Route path="rules"     element={<SecurityRules />} />
        <Route path="compare"   element={<Compare />} />
        <Route path="about"     element={<About />} />
        <Route path="*"         element={<Navigate to="/dashboard" replace />} />
      </Route>
    </Routes>
  )
}
