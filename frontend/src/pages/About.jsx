import { Shield, FileCode, Activity, AlertTriangle, BarChart2, GitCompare, Info, Users, Code2 } from 'lucide-react'
import PageHeader from '../components/PageHeader.jsx'
import SectionCard from '../components/SectionCard.jsx'

const FEATURES = [
  { icon: FileCode,      text: 'Scans Python source files for security vulnerabilities'           },
  { icon: Activity,      text: 'Calculates Cyclomatic Complexity per function using CFG analysis' },
  { icon: AlertTriangle, text: 'Detects 16 security patterns mapped to CWE references'            },
  { icon: BarChart2,     text: 'Computes Vulnerability Density and Technical Debt Index (TDI)'    },
  { icon: GitCompare,    text: 'Produces before/after refactoring comparisons via JSON reports'   },
]

const BACKEND_STACK = [
  { label: 'Language',  value: 'Java 17+'                },
  { label: 'Build',     value: 'Apache Maven'            },
  { label: 'Testing',   value: 'JUnit 5.10.2'            },
  { label: 'Deps',      value: 'Zero external runtime deps' },
]

const FRONTEND_STACK = [
  { label: 'Framework', value: 'React 18'         },
  { label: 'Bundler',   value: 'Vite 5'           },
  { label: 'Styling',   value: 'Tailwind CSS v3'  },
  { label: 'Routing',   value: 'React Router v6'  },
  { label: 'Icons',     value: 'Lucide React'     },
]

const METRICS = [
  {
    name: 'Cyclomatic Complexity (CC)',
    formula: 'M = D + 1',
    detail: 'D = number of decision points (if, for, while, except…)',
    thresholds: [
      { range: '1 – 10',  label: 'Low',       color: 'text-green-400'  },
      { range: '11 – 20', label: 'Moderate',   color: 'text-blue-400'   },
      { range: '21 – 50', label: 'High',       color: 'text-amber-400'  },
      { range: '51 +',    label: 'Very High',  color: 'text-red-400'    },
    ],
  },
  {
    name: 'Vulnerability Density (VD)',
    formula: 'VD = (flags / LOC) × 1000',
    detail: 'Normalises security findings per 1,000 lines of code',
    thresholds: [
      { range: '0',      label: 'Clean',    color: 'text-green-400' },
      { range: '1 – 20', label: 'Low',      color: 'text-blue-400'  },
      { range: '20 – 50',label: 'Moderate', color: 'text-amber-400' },
      { range: '50 +',   label: 'High',     color: 'text-red-400'   },
    ],
  },
  {
    name: 'Technical Debt Index (TDI)',
    formula: 'TDI = (CC × 0.5) + (VD × 0.5)',
    detail: 'Combines complexity and security risk into a single score',
    thresholds: [
      { range: '< 10',  label: 'Minimal Risk',   color: 'text-green-400' },
      { range: '10–30', label: 'Low Risk',        color: 'text-blue-400'  },
      { range: '30–50', label: 'Moderate Risk',   color: 'text-amber-400' },
      { range: '50 +',  label: 'High Risk',       color: 'text-red-400'   },
    ],
  },
]

export default function About() {
  return (
    <div>
      <PageHeader title="About CodeShield" subtitle="Java-powered static analysis for Python security" icon={Info} />

      <div className="bg-slate-800 rounded-xl border border-slate-700 p-8 mb-6 text-center">
        <div className="inline-flex p-4 rounded-2xl bg-cyan-500/10 border border-cyan-500/30 mb-4">
          <Shield size={40} className="text-cyan-400" />
        </div>
        <h2 className="text-gradient-cyan text-3xl font-bold mb-1">CodeShield</h2>
        <p className="text-slate-400 text-sm font-mono mb-3">v1.0-ITERATION4 — Technical Debt &amp; Security Scanner</p>
        <p className="text-slate-300 max-w-lg mx-auto leading-relaxed">
          CodeShield is a static analysis tool for Python source code. It identifies security vulnerabilities,
          measures code complexity, and produces actionable reports to guide refactoring decisions.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
        <SectionCard title="What CodeShield Does" icon={Shield}>
          <div className="space-y-3">
            {FEATURES.map(({ icon: Icon, text }) => (
              <div key={text} className="flex items-start gap-3">
                <Icon size={16} className="text-cyan-400 mt-0.5 shrink-0" />
                <p className="text-slate-300 text-sm">{text}</p>
              </div>
            ))}
          </div>
        </SectionCard>

        <SectionCard title="Technology Stack" icon={Code2}>
          <div className="mb-4">
            <p className="text-slate-400 text-xs font-semibold uppercase tracking-wider mb-2">Backend</p>
            <div className="space-y-2">
              {BACKEND_STACK.map(({ label, value }) => (
                <div key={label} className="flex justify-between text-sm">
                  <span className="text-slate-500">{label}</span>
                  <span className="text-slate-200 font-medium">{value}</span>
                </div>
              ))}
            </div>
          </div>
          <div className="border-t border-slate-700 pt-4">
            <p className="text-slate-400 text-xs font-semibold uppercase tracking-wider mb-2">Frontend</p>
            <div className="space-y-2">
              {FRONTEND_STACK.map(({ label, value }) => (
                <div key={label} className="flex justify-between text-sm">
                  <span className="text-slate-500">{label}</span>
                  <span className="text-slate-200 font-medium">{value}</span>
                </div>
              ))}
            </div>
          </div>
        </SectionCard>
      </div>

      <SectionCard title="How the Metrics Work" subtitle="Formulas and risk thresholds" className="mb-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {METRICS.map(metric => (
            <div key={metric.name} className="bg-slate-700/30 rounded-lg p-4 border border-slate-700">
              <h3 className="text-slate-200 font-semibold text-sm mb-1">{metric.name}</h3>
              <code className="text-cyan-400 text-sm font-mono block mb-1">{metric.formula}</code>
              <p className="text-slate-500 text-xs mb-3">{metric.detail}</p>
              <div className="space-y-1">
                {metric.thresholds.map(t => (
                  <div key={t.range} className="flex justify-between text-xs">
                    <span className="text-slate-400 font-mono">{t.range}</span>
                    <span className={`font-medium ${t.color}`}>{t.label}</span>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      </SectionCard>

      <SectionCard title="Project Team" icon={Users}>
        <div className="text-center py-4">
          <p className="text-slate-300 mb-2">
            Built as part of an <span className="text-cyan-400 font-medium">Extreme Programming (XP)</span> coursework project.
          </p>
          <p className="text-slate-500 text-sm">
            Developed following XP practices: iterative development, continuous testing, pair programming, and refactoring.
          </p>
        </div>
      </SectionCard>
    </div>
  )
}
