import { useEffect, useRef, useState } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'

function getAuth() {
  return { headers: { Authorization: `Basic ${localStorage.getItem('auth')}` } }
}

export default function Dashboard() {
  const [entradas, setEntradas] = useState([])
  const [compras, setCompras] = useState([])
  const [transferencias, setTransferencias] = useState([])
  const [tab, setTab] = useState('entradas')
  const [esAdmin, setEsAdmin] = useState(false)
  const [qrTokens, setQrTokens] = useState({})
  const qrIntervalRef = useRef(null)
  const navigate = useNavigate()
  const email = localStorage.getItem('email')
  const [esFuncionario, setEsFuncionario] = useState(false)

  useEffect(() => {
    axios.get('/api/auth/perfil', getAuth()).then(r => {
      setEsAdmin(r.data.rol === 'ADMIN')
      setEsFuncionario(r.data.rol === 'FUNCIONARIO')
    }).catch(() => {})
  }, [])

  useEffect(() => {
    if (!localStorage.getItem('auth')) { navigate('/'); return }
    axios.get('/api/entradas', getAuth()).then(r => setEntradas(r.data))
    axios.get('/api/compras', getAuth()).then(r => setCompras(r.data))
    axios.get('/api/transferencias', getAuth()).then(r => setTransferencias(r.data))
  }, [])

  const logout = () => { localStorage.clear(); navigate('/') }

  const responderTransferencia = async (id, aceptar) => {
    try {
      await axios.put(`/api/transferencias/${id}/responder`, { aceptar }, getAuth())
      const [e, c, t] = await Promise.all([
        axios.get('/api/entradas', getAuth()),
        axios.get('/api/compras', getAuth()),
        axios.get('/api/transferencias', getAuth())
      ])
      setEntradas(e.data)
      setCompras(c.data)
      setTransferencias(t.data)
    } catch {
      alert('Error al responder la transferencia')
    }
  }

  const generarQr = async (idEntrada) => {
    const res = await axios.post(`/api/entradas/${idEntrada}/generar-qr`, {}, getAuth())
    setQrTokens(prev => ({ ...prev, [idEntrada]: res.data.token }))
  }

  // Auto-regenerar QR cada 30s para las entradas activas mientras el dashboard está en primer plano
  useEffect(() => {
    const entradasActivas = entradas.filter(e => e.estado?.toLowerCase() === 'activa')
    if (tab !== 'entradas' || entradasActivas.length === 0) return

    // Generar QR inmediatamente al montar/cambiar tab
    entradasActivas.forEach(e => generarQr(e.idEntrada))

    qrIntervalRef.current = setInterval(() => {
      entradasActivas.forEach(e => generarQr(e.idEntrada))
    }, 30000)

    return () => clearInterval(qrIntervalRef.current)
  }, [tab, entradas])

  const tabs = ['entradas', 'compras', 'transferencias']

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-green-900 text-white px-6 py-4 flex justify-between items-center shadow">
        <div className="flex items-center gap-2">
          <span className="text-2xl">⚽</span>
          <div>
            <p className="font-bold text-lg">Mundial 2026</p>
            <p className="text-green-300 text-xs">{email}</p>
          </div>
        </div>
        <div className="flex gap-2 flex-wrap justify-end">
          <button onClick={() => navigate('/comprar')}
            className="bg-green-600 hover:bg-green-500 text-white text-sm px-3 py-2 rounded-lg transition-colors">
            + Comprar
          </button>
          <button onClick={() => navigate('/transferir')}
            className="bg-green-700 hover:bg-green-600 text-white text-sm px-3 py-2 rounded-lg transition-colors">
            → Transferir
          </button>
          {esFuncionario && (
              <button onClick={() => navigate('/validacion')}
                className="bg-green-700 hover:bg-green-600 text-white text-sm px-3 py-2 rounded-lg transition-colors">
                ✅ Validar
              </button>
          )}
          {esAdmin && (
            <>
              <button onClick={() => navigate('/admin')}
                className="bg-yellow-600 hover:bg-yellow-500 text-white text-sm px-3 py-2 rounded-lg transition-colors">
                ⚙️ Admin
              </button>
              <button onClick={() => navigate('/rankings')}
                className="bg-yellow-600 hover:bg-yellow-500 text-white text-sm px-3 py-2 rounded-lg transition-colors">
                🏆 Rankings
              </button>
            </>
          )}
          <button onClick={logout}
            className="bg-red-600 hover:bg-red-500 text-white text-sm px-3 py-2 rounded-lg transition-colors">
            Salir
          </button>
        </div>
      </div>

      <div className="max-w-3xl mx-auto px-4 py-6">
        {/* Tabs */}
        <div className="flex gap-2 mb-6 border-b border-gray-200">
          {tabs.map(t => (
            <button key={t} onClick={() => setTab(t)}
              className={`pb-2 px-4 text-sm font-medium transition-colors border-b-2 ${
                tab === t
                  ? 'border-green-700 text-green-700'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}>
              {t.charAt(0).toUpperCase() + t.slice(1)}
            </button>
          ))}
        </div>

        {/* Entradas */}
        {tab === 'entradas' && (
          <div className="space-y-3">
            {entradas.length === 0
              ? <p className="text-gray-500">No tenés entradas.</p>
              : entradas.map(e => (
                <div key={e.idEntrada} className="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
                  <div className="flex justify-between items-start">
                    <div>
                      <p className="font-bold text-green-900">
                        ⚽ {e.encuentro?.equipoLocal?.pais} vs {e.encuentro?.equipoVisitante?.pais}
                      </p>
                      <p className="text-xs text-gray-400">Entrada #{e.idEntrada}</p>
                      <p className="text-sm text-gray-500">📅 {e.encuentro?.fecha} {e.encuentro?.hora}</p>
                      <p className="text-sm text-gray-500">🏟️ {e.encuentro?.estadio?.nombre || e.encuentro?.estadio?.direccion}</p>
                      <p className="text-sm text-gray-600 mt-1">Sector <strong>{e.letraSector}</strong> — <strong>${e.montoSector}</strong></p>
                    </div>
                    <span className={`text-xs font-semibold px-2 py-1 rounded-full ${
                      e.estado === 'activa' ? 'bg-green-100 text-green-700'
                      : e.estado === 'consumida' ? 'bg-gray-100 text-gray-500'
                      : 'bg-yellow-100 text-yellow-700'
                    }`}>
                      {e.estado}
                    </span>
                  </div>
                  {e.estado?.toLowerCase() === 'activa' && qrTokens[e.idEntrada] && (
                    <div className="mt-3 bg-gray-100 rounded-lg p-3">
                      <p className="text-xs text-gray-500 mb-1">🔄 QR (se renueva cada 30s)</p>
                      <p className="font-mono text-xs break-all text-green-900 font-semibold">
                        {qrTokens[e.idEntrada]}
                      </p>
                    </div>
                  )}
                </div>
              ))}
          </div>
        )}

        {/* Compras */}
        {tab === 'compras' && (
          <div className="space-y-3">
            {compras.length === 0
              ? <p className="text-gray-500">No tenés compras.</p>
              : compras.map(c => (
                <div key={c.idCompra} className="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
                  <div className="flex justify-between items-center">
                    <div>
                      <p className="font-semibold text-gray-800">Compra #{c.idCompra}</p>
                      <p className="text-sm text-gray-500">{c.fecha}</p>
                    </div>
                    <div className="text-right">
                      <p className="font-bold text-green-800">${c.montoTotal}</p>
                      <span className={`text-xs px-2 py-1 rounded-full ${
                        c.estado === 'paga' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'
                      }`}>{c.estado}</span>
                    </div>
                  </div>
                </div>
              ))}
          </div>
        )}

        {/* Transferencias */}
        {tab === 'transferencias' && (
          <div className="space-y-3">
            {transferencias.length === 0
              ? <p className="text-gray-500">No tenés transferencias.</p>
              : transferencias.map(t => (
                <div key={t.idTransferencia} className="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
                  <div className="flex justify-between items-start">
                    <div>
                      <p className="text-sm text-gray-600">
                        <span className="font-medium">{t.emisor?.email}</span>
                        <span className="mx-2">→</span>
                        <span className="font-medium">{t.receptor?.email}</span>
                      </p>
                      <p className="text-xs text-gray-400 mt-1">Entrada #{t.entrada?.idEntrada}</p>
                    </div>
                    <span className={`text-xs font-semibold px-2 py-1 rounded-full ${
                      t.estado === 'aceptada' ? 'bg-green-100 text-green-700'
                      : t.estado === 'rechazada' ? 'bg-red-100 text-red-600'
                      : 'bg-yellow-100 text-yellow-700'
                    }`}>{t.estado}</span>
                  </div>
                  {t.estado === 'pendiente' && t.receptor?.email === email && (
                    <div className="flex gap-2 mt-3">
                      <button onClick={() => responderTransferencia(t.idTransferencia, true)}
                        className="bg-green-600 hover:bg-green-700 text-white text-xs px-3 py-1.5 rounded-lg transition-colors">
                        Aceptar
                      </button>
                      <button onClick={() => responderTransferencia(t.idTransferencia, false)}
                        className="bg-red-500 hover:bg-red-600 text-white text-xs px-3 py-1.5 rounded-lg transition-colors">
                        Rechazar
                      </button>
                    </div>
                  )}
                </div>
              ))}
          </div>
        )}
      </div>
    </div>
  )
}