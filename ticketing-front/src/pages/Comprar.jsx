import { useEffect, useState } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'

function getAuth() {
  return { headers: { Authorization: `Basic ${localStorage.getItem('auth')}` } }
}

export default function Comprar() {
  const [encuentros, setEncuentros] = useState([])
  const [form, setForm] = useState({ idEncuentro: '', letraSector: '', cantidad: 1 })
  const [disponibilidad, setDisponibilidad] = useState([])
  const [error, setError] = useState('')
  const [paso, setPaso] = useState(1)
  const [compraReservada, setCompraReservada] = useState(null)
  const [redirigiendo, setRedirigiendo] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    axios.get('/api/encuentros', getAuth()).then(r => setEncuentros(r.data))
  }, [])

  useEffect(() => {
    if (!form.idEncuentro) { setDisponibilidad([]); return }
    axios.get(`/api/encuentros/${form.idEncuentro}/disponibilidad`, getAuth())
      .then(r => setDisponibilidad(r.data))
  }, [form.idEncuentro])

  const sectorSeleccionado = disponibilidad.find(s => s.letra === form.letraSector)
  const precioTotal = sectorSeleccionado
    ? (sectorSeleccionado.precio * parseInt(form.cantidad || 1) * 1.05).toFixed(2)
    : null

  const encuentroSeleccionado = encuentros.find(e => String(e.idEncuentro) === String(form.idEncuentro))

  const handleReservar = async (e) => {
    e.preventDefault()
    setError('')
    try {
      const res = await axios.post('/api/compras/iniciar', {
        idEncuentro: parseInt(form.idEncuentro),
        letraSector: form.letraSector,
        cantidad: parseInt(form.cantidad)
      }, getAuth())
      setCompraReservada(res.data)
      setPaso(2)
    } catch (err) {
      setError(err.response?.data?.error || 'Error al reservar')
    }
  }

  const handleConfirmar = async () => {
    setError('')
    try {
      await axios.post(`/api/compras/${compraReservada.idCompra}/confirmar`, {}, getAuth())
      setRedirigiendo(true)
      setTimeout(() => navigate('/dashboard'), 3000)
    } catch (err) {
      setError(err.response?.data?.error || 'Error al confirmar')
    }
  }

  if (redirigiendo) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center gap-4">
        <svg className="animate-spin h-10 w-10 text-green-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/>
        </svg>
        <p className="text-green-800 font-semibold">Compra confirmada</p>
        <p className="text-gray-400 text-sm">Redirigiendo a tu dashboard en unos segundos...</p>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-green-900 text-white px-6 py-4 flex items-center gap-3 shadow">
        <button onClick={() => navigate('/dashboard')} className="text-green-300 hover:text-white text-sm">← Volver</button>
        <span className="text-lg font-bold">Comprar entradas</span>
        <div className="ml-auto flex gap-2 text-xs">
          <span className={`px-2 py-1 rounded-full ${paso >= 1 ? 'bg-green-600 text-white' : 'bg-green-800 text-green-400'}`}>1 Selección</span>
          <span className={`px-2 py-1 rounded-full ${paso >= 2 ? 'bg-green-600 text-white' : 'bg-green-800 text-green-400'}`}>2 Confirmar</span>
        </div>
      </div>

      <div className="max-w-md mx-auto px-4 py-8">

        {/* PASO 1: Selección */}
        {paso === 1 && (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <form onSubmit={handleReservar} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Partido</label>
                <select value={form.idEncuentro}
                  onChange={e => setForm({ ...form, idEncuentro: e.target.value, letraSector: '' })}
                  className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-green-500">
                  <option value="">Seleccioná un partido</option>
                  {encuentros.map(enc => (
                    <option key={enc.idEncuentro} value={enc.idEncuentro}>
                      {enc.equipoLocal?.pais} vs {enc.equipoVisitante?.pais} — {enc.fecha}
                    </option>
                  ))}
                </select>
              </div>

              {disponibilidad.length > 0 && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Sector</label>
                  <div className="space-y-2">
                    {disponibilidad.map(s => {
                      const agotado = s.disponibles <= 0
                      const seleccionado = form.letraSector === s.letra
                      return (
                        <button key={s.letra} type="button" disabled={agotado}
                          onClick={() => !agotado && setForm({ ...form, letraSector: s.letra })}
                          className={`w-full text-left px-4 py-3 rounded-lg border text-sm transition-colors ${
                            agotado ? 'bg-gray-50 border-gray-200 text-gray-400 cursor-not-allowed'
                            : seleccionado ? 'bg-green-50 border-green-500 text-green-800'
                            : 'bg-white border-gray-300 hover:border-green-400'
                          }`}>
                          <div className="flex justify-between items-center">
                            <span className="font-medium">Sector {s.letra}</span>
                            <span className="font-bold">${s.precio}</span>
                          </div>
                          <div className="flex justify-between mt-1">
                            <span className={agotado ? 'text-red-400' : 'text-green-600'}>
                              {agotado ? 'Sin disponibilidad' : `${s.disponibles} disponibles`}
                            </span>
                            <span className="text-gray-400">Aforo: {s.aforo}</span>
                          </div>
                        </button>
                      )
                    })}
                  </div>
                </div>
              )}

              {form.letraSector && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Cantidad (máx. 5)</label>
                  <input type="number" min={1} max={Math.min(5, sectorSeleccionado?.disponibles || 5)}
                    value={form.cantidad}
                    onChange={e => setForm({ ...form, cantidad: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-green-500" />
                </div>
              )}

              {precioTotal && (
                <div className="bg-green-50 rounded-lg p-3 text-sm">
                  <div className="flex justify-between text-gray-600">
                    <span>Subtotal ({form.cantidad} × ${sectorSeleccionado?.precio})</span>
                    <span>${(sectorSeleccionado?.precio * parseInt(form.cantidad)).toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between text-gray-500 text-xs">
                    <span>Comisión (5%)</span>
                    <span>${(sectorSeleccionado?.precio * parseInt(form.cantidad) * 0.05).toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between font-bold text-green-800 mt-1 pt-1 border-t border-green-200">
                    <span>Total</span>
                    <span>${precioTotal}</span>
                  </div>
                </div>
              )}

              {error && <p className="text-red-500 text-sm font-medium">{error}</p>}

              <button type="submit" disabled={!form.letraSector}
                className="w-full bg-green-700 hover:bg-green-800 disabled:bg-gray-300 disabled:cursor-not-allowed text-white font-semibold py-3 rounded-lg transition-colors">
                Reservar entrada(s)
              </button>
            </form>
          </div>
        )}

        {/* PASO 2: Confirmación */}
        {paso === 2 && compraReservada && (
          <div className="space-y-4">
            <div className="bg-amber-50 border border-amber-200 rounded-2xl p-5">
              <div className="flex items-center gap-2 mb-3">
                <span className="text-amber-600 font-semibold text-sm">Reserva registrada</span>
                <span className="text-xs bg-amber-100 text-amber-700 px-2 py-0.5 rounded-full font-medium">pendiente</span>
              </div>
              <p className="text-sm text-gray-600 mb-1">
                <strong>{parseInt(form.cantidad)} entrada(s)</strong> para{' '}
                <strong>{encuentroSeleccionado?.equipoLocal?.pais} vs {encuentroSeleccionado?.equipoVisitante?.pais}</strong>
              </p>
              <p className="text-sm text-gray-600 mb-1">
                Sector <strong>{form.letraSector}</strong> — {encuentroSeleccionado?.fecha}
              </p>
              <p className="text-xs text-gray-400 mt-2">
                Las entradas están reservadas a tu nombre. Confirmá para activarlas.
              </p>
            </div>

            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
              <h3 className="text-sm font-semibold text-gray-700 mb-3">Resumen de pago</h3>
              <div className="space-y-1 text-sm">
                <div className="flex justify-between text-gray-600">
                  <span>Subtotal ({form.cantidad} × ${sectorSeleccionado?.precio})</span>
                  <span>${(sectorSeleccionado?.precio * parseInt(form.cantidad)).toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-gray-500 text-xs">
                  <span>Comisión (5%)</span>
                  <span>${(sectorSeleccionado?.precio * parseInt(form.cantidad) * 0.05).toFixed(2)}</span>
                </div>
                <div className="flex justify-between font-bold text-green-800 mt-2 pt-2 border-t border-gray-100">
                  <span>Total</span>
                  <span>${precioTotal}</span>
                </div>
              </div>
            </div>

            {error && <p className="text-red-500 text-sm font-medium">{error}</p>}

            <button onClick={handleConfirmar}
              className="w-full bg-green-700 hover:bg-green-800 text-white font-semibold py-3 rounded-lg transition-colors">
              Confirmar compra
            </button>
            <button onClick={() => { setPaso(1); setCompraReservada(null) }}
              className="w-full text-gray-500 hover:text-gray-700 text-sm py-2">
              Cancelar reserva
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
