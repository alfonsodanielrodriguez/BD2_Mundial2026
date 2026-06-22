import { useState } from 'react'
import axios from 'axios'
import { useNavigate, Link } from 'react-router-dom'

export default function Registro() {
  const [form, setForm] = useState({
    email: '', password: '', paisDocumentoIdentidad: '', tipoDocumento: '',
    numeroDocumento: '', paisDireccion: '', localidad: '', calle: '',
    numeroDireccion: '', codigoPostal: ''
  })
  const [telefonos, setTelefonos] = useState([''])
  const [error, setError] = useState('')
  const navigate = useNavigate()

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value })

  const handleTelefono = (i, val) => {
    const t = [...telefonos]
    t[i] = val
    setTelefonos(t)
  }

  const agregarTelefono = () => setTelefonos([...telefonos, ''])
  const quitarTelefono = (i) => setTelefonos(telefonos.filter((_, idx) => idx !== i))

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      const tel = telefonos.filter(t => t.trim() !== '')
      await axios.post('/api/auth/registro', { ...form, telefonos: tel })
      navigate('/')
    } catch {
      setError('Error al registrarse. El email puede ya estar en uso.')
    }
  }

  const campos = [
    ['email', 'Email'],
    ['password', 'Contraseña'],
    ['paisDocumentoIdentidad', 'País del documento'],
    ['tipoDocumento', 'Tipo de documento'],
    ['numeroDocumento', 'Número de documento'],
    ['paisDireccion', 'País de residencia'],
    ['localidad', 'Localidad'],
    ['calle', 'Calle'],
    ['numeroDireccion', 'Número'],
    ['codigoPostal', 'Código postal'],
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-900 via-green-800 to-emerald-900 flex items-center justify-center px-4 py-8">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-8">
        <div className="text-center mb-6">
          <div className="text-4xl mb-2">⚽</div>
          <h1 className="text-2xl font-bold text-green-900">Crear cuenta</h1>
          <p className="text-gray-500 text-sm mt-1">Mundial 2026 — Ticketing</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3">
          {campos.map(([name, label]) => (
            <input
              key={name}
              name={name}
              placeholder={label}
              type={name === 'password' ? 'password' : 'text'}
              value={form[name]}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
            />
          ))}
          {/* Teléfonos */}
          <div className="space-y-2">
            <p className="text-xs text-gray-500 font-medium">Teléfonos de contacto</p>
            {telefonos.map((tel, i) => (
              <div key={i} className="flex gap-2">
                <input
                  type="tel"
                  placeholder={`Teléfono ${i + 1}`}
                  value={tel}
                  onChange={e => handleTelefono(i, e.target.value)}
                  className="flex-1 border border-gray-300 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
                />
                {telefonos.length > 1 && (
                  <button type="button" onClick={() => quitarTelefono(i)}
                    className="text-red-400 hover:text-red-600 text-sm px-2">✕</button>
                )}
              </div>
            ))}
            <button type="button" onClick={agregarTelefono}
              className="text-green-700 hover:text-green-900 text-sm font-medium">
              + Agregar teléfono
            </button>
          </div>

          {error && <p className="text-red-500 text-sm">{error}</p>}
          <button
            type="submit"
            className="w-full bg-green-700 hover:bg-green-800 text-white font-semibold py-3 rounded-lg transition-colors mt-2"
          >
            Registrarse
          </button>
        </form>

        <p className="text-center text-sm text-gray-500 mt-6">
          ¿Ya tenés cuenta?{' '}
          <Link to="/" className="text-green-700 font-semibold hover:underline">
            Iniciá sesión
          </Link>
        </p>
      </div>
    </div>
  )
}