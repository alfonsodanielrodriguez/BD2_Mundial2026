import { useEffect, useState } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'

function getAuth() {
  return { headers: { Authorization: `Basic ${localStorage.getItem('auth')}` } }
}

export default function Comprar() {
  const [encuentros, setEncuentros] = useState([])
  const [form, setForm] = useState({ idEncuentro: '', letraSector: '', cantidad: 1 })
  const [sectores, setSectores] = useState([])
  const [mensaje, setMensaje] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    axios.get('/api/encuentros', getAuth()).then(r => setEncuentros(r.data))
  }, [])

  useEffect(() => {
    if (!form.idEncuentro) { setSectores([]); return }
    axios.get(`/api/admin/encuentros/${form.idEncuentro}/sectores`, getAuth())
      .then(r => setSectores(r.data))
  }, [form.idEncuentro])

  const handleComprar = async (e) => {
    e.preventDefault()
    try {
      await axios.post('/api/compras', {
        idEncuentro: parseInt(form.idEncuentro),
        letraSector: form.letraSector,
        cantidad: parseInt(form.cantidad)
      }, getAuth())
      setMensaje('¡Compra realizada con éxito!')
      setTimeout(() => navigate('/dashboard'), 2000)
    } catch (err) {
      setMensaje(err.response?.data?.message || 'Error al realizar la compra')
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-green-900 text-white px-6 py-4 flex items-center gap-3 shadow">
        <button onClick={() => navigate('/dashboard')} className="text-green-300 hover:text-white text-sm">← Volver</button>
        <span className="text-lg font-bold">Comprar entradas</span>
      </div>

      <div className="max-w-md mx-auto px-4 py-8">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <form onSubmit={handleComprar} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Partido</label>
              <select value={form.idEncuentro}
                onChange={e => setForm({ ...form, idEncuentro: e.target.value })}
                className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-green-500">
                <option value="">Seleccioná un partido</option>
                {encuentros.map(enc => (
                  <option key={enc.idEncuentro} value={enc.idEncuentro}>
                    {enc.equipoLocal?.pais} vs {enc.equipoVisitante?.pais} — {enc.fecha}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Sector</label>
              <select value={form.letraSector}
                onChange={e => setForm({ ...form, letraSector: e.target.value })}
                className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
                disabled={!form.idEncuentro}>
                <option value="">Seleccioná un sector</option>
                {sectores.map(s => (
                  <option key={s.letra} value={s.letra}>Sector {s.letra}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Cantidad (máx. 5)</label>
              <input type="number" min={1} max={5} value={form.cantidad}
                onChange={e => setForm({ ...form, cantidad: e.target.value })}
                className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-green-500" />
            </div>

            {mensaje && (
              <p className={`text-sm font-medium ${mensaje.includes('éxito') ? 'text-green-600' : 'text-red-500'}`}>
                {mensaje}
              </p>
            )}

            <button type="submit"
              className="w-full bg-green-700 hover:bg-green-800 text-white font-semibold py-3 rounded-lg transition-colors">
              Comprar
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}