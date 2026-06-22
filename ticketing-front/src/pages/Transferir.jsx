import { useEffect, useState } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'

function getAuth() {
  return { headers: { Authorization: `Basic ${localStorage.getItem('auth')}` } }
}

export default function Transferir() {
  const [entradas, setEntradas] = useState([])
  const [form, setForm] = useState({ idEntrada: '', emailReceptor: '' })
  const [mensaje, setMensaje] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    axios.get('/api/entradas', getAuth()).then(r =>
      setEntradas(r.data.filter(e => e.estado === 'activa'))
    )
  }, [])

  const handleTransferir = async (e) => {
    e.preventDefault()
    try {
      await axios.post('/api/transferencias', {
        idEntrada: parseInt(form.idEntrada),
        emailReceptor: form.emailReceptor
      }, getAuth())
      setMensaje('¡Transferencia iniciada! El receptor debe aceptarla.')
      setTimeout(() => navigate('/dashboard'), 2000)
    } catch (err) {
      setMensaje(err.response?.data?.message || 'Error al transferir')
    }
  }

  const exito = mensaje.includes('éxito') || mensaje.includes('iniciada')

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-green-900 text-white px-6 py-4 flex items-center gap-3 shadow">
        <button onClick={() => navigate('/dashboard')} className="text-green-300 hover:text-white text-sm">← Volver</button>
        <span className="text-lg font-bold">Transferir entrada</span>
      </div>

      <div className="max-w-md mx-auto px-4 py-8">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <form onSubmit={handleTransferir} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Entrada a transferir</label>
              <select value={form.idEntrada}
                onChange={e => setForm({ ...form, idEntrada: e.target.value })}
                className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-green-500">
                <option value="">Seleccioná una entrada</option>
                {entradas.map(e => (
                  <option key={e.idEntrada} value={e.idEntrada}>
                    #{e.idEntrada} — Sector {e.letraSector} — ${e.montoSector}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email del receptor</label>
              <input type="email" value={form.emailReceptor}
                onChange={e => setForm({ ...form, emailReceptor: e.target.value })}
                placeholder="email@ejemplo.com"
                className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-green-500" />
            </div>

            {mensaje && (
              <p className={`text-sm font-medium ${exito ? 'text-green-600' : 'text-red-500'}`}>{mensaje}</p>
            )}

            <button type="submit"
              className="w-full bg-green-700 hover:bg-green-800 text-white font-semibold py-3 rounded-lg transition-colors">
              Transferir
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}