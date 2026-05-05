import { useRef, useState } from 'react'
import {
  Upload as UploadIcon,
  UploadCloud,
  FileCode,
  X,
  CheckCircle,
  AlertCircle,
  Loader2,
} from 'lucide-react'
import PageHeader from '../components/PageHeader.jsx'
import SectionCard from '../components/SectionCard.jsx'

const MAX_BYTES = 1 * 1024 * 1024 // 1 MB upload limit

// Validates the selected file before any reading happens.
// Returns null if the file is acceptable, or a human-readable error otherwise.
function validateFile(file) {
  if (!file) {
    return 'Please choose a Python file before submitting.'
  }
  if (!file.name.toLowerCase().endsWith('.py')) {
    return 'Only Python (.py) files are accepted. Please choose a different file.'
  }
  if (file.size === 0) {
    return 'The selected file is empty. Please choose a file containing Python source code.'
  }
  if (file.size > MAX_BYTES) {
    return `File is too large. The maximum upload size is ${MAX_BYTES / 1024 / 1024} MB.`
  }
  return null
}

function formatBytes(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
}

export default function Upload() {
  const inputRef = useRef(null)
  const [file, setFile] = useState(null)
  const [error, setError] = useState('')
  const [isAnalysing, setIsAnalysing] = useState(false)
  const [result, setResult] = useState(null)
  const [isDragging, setIsDragging] = useState(false)

  const pickFile = (candidate) => {
    setError('')
    setResult(null)
    const validationError = validateFile(candidate)
    if (validationError) {
      setFile(null)
      setError(validationError)
      return
    }
    setFile(candidate)
  }

  const handleInputChange = (event) => {
    const candidate = event.target.files?.[0] ?? null
    pickFile(candidate)
    // Reset the input so re-selecting the same file still fires onChange
    event.target.value = ''
  }

  const handleDrop = (event) => {
    event.preventDefault()
    setIsDragging(false)
    const candidate = event.dataTransfer.files?.[0] ?? null
    pickFile(candidate)
  }

  const clearSelection = () => {
    setFile(null)
    setError('')
    setResult(null)
  }

  const handleSubmit = async () => {
    setError('')
    setResult(null)

    const validationError = validateFile(file)
    if (validationError) {
      setError(validationError)
      return
    }

    setIsAnalysing(true)
    try {
      // Read the .py file as plain text in the browser. The contents are
      // NEVER executed — we only count lines for the placeholder summary.
      const text = await file.text()
      const lineCount = text.split(/\r\n|\r|\n/).length

      // TODO: replace this client-side placeholder with a call to a real
      // analysis endpoint (e.g. POST /api/analyse) once the Java backend
      // exposes an HTTP API. The endpoint should accept multipart/form-data,
      // run the existing ComplexityAnalyser + SecurityScanner pipeline, and
      // return the standard ModuleResult JSON shape used elsewhere in the UI.
      setResult({
        filename: file.name,
        size: file.size,
        lines: lineCount,
        message: 'File received successfully and is ready for analysis.',
      })
    } catch (readError) {
      setError('Could not read the selected file. Please try again.')
    } finally {
      setIsAnalysing(false)
    }
  }

  return (
    <div>
      <PageHeader
        title="Upload &amp; Analyse"
        subtitle="Submit a Python source file for static analysis"
        icon={UploadIcon}
      />

      <SectionCard title="Upload Python File" subtitle="Only .py files are accepted (max 1 MB)" icon={UploadCloud} className="mb-6">
        <div
          onDragOver={(e) => { e.preventDefault(); setIsDragging(true) }}
          onDragLeave={() => setIsDragging(false)}
          onDrop={handleDrop}
          onClick={() => inputRef.current?.click()}
          className={`flex flex-col items-center justify-center text-center px-6 py-10 rounded-xl border-2 border-dashed cursor-pointer transition-colors ${
            isDragging
              ? 'border-cyan-400 bg-cyan-500/5'
              : 'border-slate-600 hover:border-slate-500 bg-slate-700/20'
          }`}
        >
          <div className="p-3 rounded-full bg-cyan-500/10 border border-cyan-500/30 mb-3">
            <UploadCloud size={28} className="text-cyan-400" />
          </div>
          <p className="text-slate-200 font-medium">
            Drop your <span className="font-mono text-cyan-400">.py</span> file here, or click to browse
          </p>
          <p className="text-slate-500 text-xs mt-1">Python source files only · Maximum 1 MB</p>
          <input
            ref={inputRef}
            type="file"
            accept=".py,text/x-python"
            onChange={handleInputChange}
            className="hidden"
          />
        </div>

        {file && (
          <div className="mt-4 flex items-center justify-between gap-3 p-3 rounded-lg bg-slate-700/40 border border-slate-700">
            <div className="flex items-center gap-3 min-w-0">
              <div className="p-2 rounded-lg bg-slate-700 shrink-0">
                <FileCode size={18} className="text-cyan-400" />
              </div>
              <div className="min-w-0">
                <p className="font-mono text-sm text-slate-100 truncate">{file.name}</p>
                <p className="text-xs text-slate-400">{formatBytes(file.size)}</p>
              </div>
            </div>
            <button
              onClick={clearSelection}
              type="button"
              aria-label="Remove selected file"
              className="p-1.5 rounded-md text-slate-400 hover:text-slate-100 hover:bg-slate-700 transition-colors shrink-0"
            >
              <X size={16} />
            </button>
          </div>
        )}

        {error && (
          <div className="mt-4 flex items-start gap-3 px-4 py-3 rounded-lg bg-red-900/20 border border-red-700 text-red-300">
            <AlertCircle size={18} className="text-red-400 mt-0.5 shrink-0" />
            <p className="text-sm">{error}</p>
          </div>
        )}

        <div className="mt-5 flex items-center justify-end">
          <button
            onClick={handleSubmit}
            disabled={!file || isAnalysing}
            type="button"
            className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-cyan-600 hover:bg-cyan-500 disabled:bg-slate-700 disabled:text-slate-500 disabled:cursor-not-allowed text-white text-sm font-medium transition-colors"
          >
            {isAnalysing ? (
              <>
                <Loader2 size={16} className="animate-spin" />
                Analysing…
              </>
            ) : (
              <>
                <UploadIcon size={16} />
                Analyse Python File
              </>
            )}
          </button>
        </div>
      </SectionCard>

      {result && (
        <SectionCard
          title="Analysis Result"
          subtitle="Placeholder summary — full analysis pipeline coming soon"
          icon={CheckCircle}
          className="border-green-900/50"
        >
          <div className="flex items-start gap-3 px-4 py-3 rounded-lg bg-green-900/20 border border-green-700 text-green-300 mb-5">
            <CheckCircle size={18} className="text-green-400 mt-0.5 shrink-0" />
            <p className="text-sm">{result.message}</p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="bg-slate-700/30 border border-slate-700 rounded-lg p-4">
              <p className="text-slate-400 text-xs uppercase tracking-wider">Filename</p>
              <p className="font-mono text-cyan-400 text-sm mt-1 break-all">{result.filename}</p>
            </div>
            <div className="bg-slate-700/30 border border-slate-700 rounded-lg p-4">
              <p className="text-slate-400 text-xs uppercase tracking-wider">File Size</p>
              <p className="text-slate-100 text-lg font-semibold mt-1">{formatBytes(result.size)}</p>
            </div>
            <div className="bg-slate-700/30 border border-slate-700 rounded-lg p-4">
              <p className="text-slate-400 text-xs uppercase tracking-wider">Total Lines</p>
              <p className="text-slate-100 text-lg font-semibold mt-1">{result.lines.toLocaleString()}</p>
            </div>
          </div>

          <p className="text-slate-500 text-xs mt-5">
            Note: full TDI, complexity, and security scanning will appear here once the analysis
            backend is connected. Your uploaded file is read in the browser only and is not sent
            to any server or executed.
          </p>
        </SectionCard>
      )}
    </div>
  )
}
