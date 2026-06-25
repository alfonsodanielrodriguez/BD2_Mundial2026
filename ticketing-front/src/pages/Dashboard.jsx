import { useEffect, useRef, useState } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'

function getAuth() {
  return { headers: { Authorization: `Basic ${localStorage.getItem('auth')}` } }
}

function DashboardFuncionario({ perfil, navigate, logout }) {
  const [historial, setHistorial] = useState([])

  useEffect(() => {
    axios.get('/api/validacion/historial', getAuth()).then(r => setHistorial(r.data)).catch(() => {})
  }, [])

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-green-900 text-white px-6 py-4 flex justify-between items-center shadow">
        <div className="flex items-center gap-2">
          <span className="text-2xl">⚽</span>
          <div>
            <p className="font-bold text-lg">Mundial 2026</p>
            <p className="text-green-300 text-xs">{perfil.email}</p>
          </div>
        </div>
        <button onClick={logout}
          className="bg-red-600 hover:bg-red-500 text-white text-sm px-3 py-2 rounded-lg transition-colors">
          Salir
        </button>
      </div>

      <div className="max-w-md mx-auto px-4 py-6 space-y-4">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
          <h2 className="font-semibold text-gray-700 mb-3">Mi información</h2>
          <div className="space-y-2 text-sm text-gray-600">
            <div className="flex justify-between">
              <span className="text-gray-400">Numero de Legajo</span>
              <span className="font-semibold text-green-800">{perfil.numeroLegajo || '—'}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-400">Dispositivo asignado</span>
              <span className="font-mono font-semibold text-green-800">{perfil.idDispositivo || '—'}</span>
            </div>
          </div>
        </div>

        <button onClick={() => navigate('/validacion')}
          className="w-full bg-green-700 hover:bg-green-800 text-white font-semibold py-4 rounded-2xl text-lg transition-colors shadow-sm">
          Validar entrada
        </button>

        {historial.length > 0 && (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
            <h2 className="font-semibold text-gray-700 mb-3">Mis validaciones</h2>
            <div className="space-y-2">
              {historial.map((v, i) => (
                <div key={i} className="border-b border-gray-50 last:border-0 pb-2 last:pb-0">
                  <p className="text-sm font-medium text-green-900">Entrada #{v.idEntrada}</p>
                  <p className="text-xs text-gray-500">
                    {v.encuentro?.equipoLocal?.pais} vs {v.encuentro?.equipoVisitante?.pais} — {v.encuentro?.fecha}
                  </p>
                  <p className="text-xs text-gray-400">{v.hora}</p>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default function Dashboard() {
  const [entradas, setEntradas] = useState([])
  const [compras, setCompras] = useState([])
  const [transferencias, setTransferencias] = useState([])
  const [tab, setTab] = useState('entradas')
  const [perfil, setPerfil] = useState(null)
  const [qrTokens, setQrTokens] = useState({})
  const qrIntervalRef = useRef(null)
  const navigate = useNavigate()
  const email = localStorage.getItem('email')

  useEffect(() => {
    if (!localStorage.getItem('auth')) { navigate('/'); return }
    axios.get('/api/auth/perfil', getAuth()).then(r => setPerfil(r.data)).catch(() => {})
  }, [])

  useEffect(() => {
    if (!perfil || perfil.rol === 'FUNCIONARIO') return
    axios.get('/api/entradas', getAuth()).then(r => setEntradas(r.data))
    axios.get('/api/compras', getAuth()).then(r => setCompras(r.data))
    axios.get('/api/transferencias', getAuth()).then(r => setTransferencias(r.data))
  }, [perfil])

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

  const confirmarCompra = async (idCompra) => {
    try {
      await axios.post(`/api/compras/${idCompra}/confirmar`, {}, getAuth())
      const [e, c] = await Promise.all([
        axios.get('/api/entradas', getAuth()),
        axios.get('/api/compras', getAuth())
      ])
      setEntradas(e.data)
      setCompras(c.data)
    } catch (err) {
      alert(err.response?.data?.error || 'Error al confirmar')
    }
  }

  useEffect(() => {
    const entradasActivas = entradas.filter(e => e.estado?.toLowerCase() === 'activa')
    if (tab !== 'entradas' || entradasActivas.length === 0) return

    entradasActivas.forEach(e => generarQr(e.idEntrada))
    qrIntervalRef.current = setInterval(() => {
      entradasActivas.forEach(e => generarQr(e.idEntrada))
    }, 30000)

    return () => clearInterval(qrIntervalRef.current)
  }, [tab, entradas])

  if (!perfil) return null

  const esAdmin = perfil.rol === 'ADMIN'
  const esFuncionario = perfil.rol === 'FUNCIONARIO'

  if (esFuncionario) {
    return <DashboardFuncionario perfil={perfil} navigate={navigate} logout={logout} />
  }

  const tabs = ['entradas', 'compras', 'transferencias']

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-green-900 text-white px-6 py-4 flex justify-between items-center shadow">
        <div className="flex items-center gap-2">
          <span className="text-2xl">⚽</span>
          <div>
            <p className="font-bold text-lg">Mundial 2026</p>
            <p className="text-green-300 text-xs">{email}</p>
          </div>
        </div>
        <div className="flex gap-2 flex-wrap justify-end">
          {!esAdmin && (
            <>
              <button onClick={() => navigate('/comprar')}
                className="bg-green-600 hover:bg-green-500 text-white text-sm px-3 py-2 rounded-lg transition-colors">
                + Comprar
              </button>
              <button onClick={() => navigate('/transferir')}
                className="bg-green-700 hover:bg-green-600 text-white text-sm px-3 py-2 rounded-lg transition-colors">
                → Transferir
              </button>
            </>
          )}
          {esAdmin && (
            <>
              <button onClick={() => navigate('/admin')}
                className="bg-yellow-600 hover:bg-yellow-500 text-white text-sm px-3 py-2 rounded-lg transition-colors">
                Panel Admin
              </button>
              <button onClick={() => navigate('/rankings')}
                className="bg-yellow-600 hover:bg-yellow-500 text-white text-sm px-3 py-2 rounded-lg transition-colors">
                Rankings
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

        {tab === 'entradas' && (
          <div className="space-y-3">
            {entradas.length === 0
              ? <p className="text-gray-500">No tenés entradas.</p>
              : entradas.map(e => (
                <div key={e.idEntrada} className="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
                  <div className="flex justify-between items-start">
                    <div>
                      <p className="font-bold text-green-900">
                        {e.encuentro?.equipoLocal?.pais} vs {e.encuentro?.equipoVisitante?.pais}
                      </p>
                      <p className="text-xs text-gray-400">Entrada #{e.idEntrada}</p>
                      <p className="text-sm text-gray-500">{e.encuentro?.fecha} {e.encuentro?.hora}</p>
                      <p className="text-sm text-gray-500">{e.encuentro?.estadio?.nombre || e.encuentro?.estadio?.direccion}</p>
                      <p className="text-sm text-gray-600 mt-1">Sector <strong>{e.letraSector}</strong> — <strong>${e.montoSector}</strong></p>
                    </div>
                    <span className={`text-xs font-semibold px-2 py-1 rounded-full ${
                      e.estado === 'activa' ? 'bg-green-100 text-green-700'
                      : e.estado === 'consumida' ? 'bg-gray-100 text-gray-500'
                      : e.estado === 'reservada' ? 'bg-amber-100 text-amber-700'
                      : 'bg-yellow-100 text-yellow-700'
                    }`}>
                      {e.estado}
                    </span>
                  </div>
                  {e.estado === 'reservada' && e.idCompra && (
                    <div className="mt-3 bg-amber-50 rounded-lg p-3">
                      <p className="text-xs text-amber-700 mb-2">Esta entrada está reservada. Confirmá la compra para activarla.</p>
                      <button onClick={() => confirmarCompra(e.idCompra)}
                        className="bg-amber-500 hover:bg-amber-600 text-white text-xs px-4 py-2 rounded-lg transition-colors font-medium">
                        Confirmar compra
                      </button>
                    </div>
                  )}
                  {e.estado?.toLowerCase() === 'activa' && qrTokens[e.idEntrada] && (
                    <div className="mt-3 bg-gray-100 rounded-lg p-3">
                      <p className="text-xs text-gray-500 mb-1">QR (se renueva cada 30s)</p>
                      <p className="font-mono text-xs break-all text-green-900 font-semibold">
                        {qrTokens[e.idEntrada]}
                      </p>
                    </div>
                  )}
                </div>
              ))}
          </div>
        )}

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
