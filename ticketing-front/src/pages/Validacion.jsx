import { useState } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'

function getAuth() {
  return { headers: { Authorization: `Basic ${localStorage.getItem('auth')}` } }
}

export default function Validacion() {
  const [idEntrada, setIdEntrada] = useState('')
  const [idDispositivo, setIdDispositivo] = useState('')
  const [codigoQr, setCodigoQr] = useState('')
  const [mensaje, setMensaje] = useState('')
  const [error, setError] = useState('')
  const navigate = useNavigate()

  const escanear = async (e) => {
    e.preventDefault()
    setMensaje('')
    setError('')
    try {
      const res = await axios.post('/api/validacion/escanear', {
        idEntrada: parseInt(idEntrada),
        idDispositivo,
        codigoQr
      }, getAuth())
      setMensaje(res.data.mensaje)
      setCodigoQr('')
    } catch (err) {
      setError(err.response?.data?.error || 'Error al validar')
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-green-900 text-white px-6 py-4 flex items-center gap-3 shadow">
        <button onClick={() => navigate('/dashboard')} className="text-green-300 hover:text-white text-sm">← Volver</button>
        <span className="text-lg font-bold">Validación de acceso</span>
      </div>

      <div className="max-w-md mx-auto px-4 py-8">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <form onSubmit={escanear} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">ID de entrada</label>
              <input placeholder="Ej: 1" value={idEntrada}
                onChange={e => setIdEntrada(e.target.value)}
                className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-green-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">ID de dispositivo</label>
              <input placeholder="Ej: DISP-001" value={idDispositivo}
                onChange={e => setIdDispositivo(e.target.value)}
                className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-green-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Código QR</label>
              <input placeholder="Token visible en la entrada del usuario" value={codigoQr}
                onChange={e => setCodigoQr(e.target.value)}
                className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-green-500" />
            </div>

            {mensaje && <p className="text-green-600 text-sm font-medium">{mensaje}</p>}
            {error && <p className="text-red-500 text-sm font-medium">{error}</p>}

            <button type="submit"
              className="w-full bg-green-700 hover:bg-green-800 text-white font-semibold py-3 rounded-lg transition-colors">
              Validar entrada
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}