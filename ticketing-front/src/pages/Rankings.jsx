import { useEffect, useState } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'

function getAuth() {
  return { headers: { Authorization: `Basic ${localStorage.getItem('auth')}` } }
}

export default function Rankings() {
  const [eventos, setEventos] = useState([])
  const [compradores, setCompradores] = useState([])
  const [tab, setTab] = useState('eventos')
  const navigate = useNavigate()

  useEffect(() => {
    axios.get('/api/rankings/eventos', getAuth()).then(r => setEventos(r.data))
    axios.get('/api/rankings/compradores', getAuth()).then(r => setCompradores(r.data))
  }, [])

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-green-900 text-white px-6 py-4 flex items-center justify-between shadow">
        <div className="flex items-center gap-3">
          <span className="text-lg font-bold">🏆 Rankings</span>
        </div>
        <button onClick={() => navigate('/admin')} className="text-green-300 hover:text-white text-sm">← Volver</button>
      </div>

      <div className="max-w-2xl mx-auto px-4 py-6">
        <div className="flex gap-2 mb-6 border-b border-gray-200">
          {[['eventos', 'Eventos con más ventas'], ['compradores', 'Mayores compradores']].map(([key, label]) => (
            <button key={key} onClick={() => setTab(key)}
              className={`pb-2 px-4 text-sm font-medium transition-colors border-b-2 ${
                tab === key ? 'border-green-700 text-green-700' : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}>
              {label}
            </button>
          ))}
        </div>

        {tab === 'eventos' && (
          <div className="space-y-3">
            {eventos.length === 0 ? <p className="text-gray-500">Sin datos aún.</p> :
              eventos.map((e, i) => (
                <div key={e.idEncuentro} className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 flex justify-between items-center">
                  <div>
                    <p className="font-bold text-green-900">#{i + 1} — {e.local} vs {e.visitante}</p>
                    <p className="text-sm text-gray-500">📅 {e.fecha} — 🏟️ {e.estadio}</p>
                  </div>
                  <span className="bg-green-100 text-green-700 font-bold text-sm px-3 py-1 rounded-full">
                    🎟️ {e.totalEntradas}
                  </span>
                </div>
              ))}
          </div>
        )}

        {tab === 'compradores' && (
          <div className="space-y-3">
            {compradores.length === 0 ? <p className="text-gray-500">Sin datos aún.</p> :
              compradores.map((c, i) => (
                <div key={c.email} className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 flex justify-between items-center">
                  <div>
                    <p className="font-bold text-green-900">#{i + 1}</p>
                    <p className="text-sm text-gray-600">{c.email}</p>
                  </div>
                  <span className="bg-yellow-100 text-yellow-700 font-bold text-sm px-3 py-1 rounded-full">
                    💰 ${c.totalGastado}
                  </span>
                </div>
              ))}
          </div>
        )}
      </div>
    </div>
  )
}