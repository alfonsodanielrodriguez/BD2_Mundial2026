import { useEffect, useState } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'

function getAuth() {
  return { headers: { Authorization: `Basic ${localStorage.getItem('auth')}` } }
}

export default function Admin() {
  const [tab, setTab] = useState('equipos')
  const [equipos, setEquipos] = useState([])
  const [estadios, setEstadios] = useState([])
  const [encuentros, setEncuentros] = useState([])
  const [sectoresHabilitados, setSectoresHabilitados] = useState([])
  const [preciosSector, setPreciosSector] = useState({})
  const [encuentroSeleccionado, setEncuentroSeleccionado] = useState('')
  const [tipoMensaje, setTipoMensaje] = useState('ok')
  const [mensaje, setMensaje] = useState('')
  const navigate = useNavigate()
  const email = localStorage.getItem('email')

  const [equipoForm, setEquipoForm] = useState({ pais: '' })
  const [estadioForm, setEstadioForm] = useState({ nombre: '', direccion: '', pais: '', aforo: '' })
  const [encuentroForm, setEncuentroForm] = useState({
    fecha: '', hora: '', idEstadio: '', paisLocal: '', paisVisitante: '', emailAdmin: email
  })
  const [funcionarios, setFuncionarios] = useState([])
  const [funcionarioForm, setFuncionarioForm] = useState({
    email: '', password: '',
    paisDocumentoIdentidad: '', tipoDocumento: '', numeroDocumento: ''
  })
  const [editandoEstadio, setEditandoEstadio] = useState(null)
  const [estadioEditForm, setEstadioEditForm] = useState({ nombre: '', direccion: '', pais: '' })
  const [funcionarioExpandido, setFuncionarioExpandido] = useState(null)
  const [encuentrosFuncionario, setEncuentrosFuncionario] = useState({})
  const [asignandoEncuentro, setAsignandoEncuentro] = useState('')
  const [historialValidaciones, setHistorialValidaciones] = useState([])

  useEffect(() => {
    axios.get('/api/admin/equipos', getAuth()).then(r => setEquipos(r.data))
    axios.get('/api/admin/estadios', getAuth()).then(r => setEstadios(r.data))
    axios.get('/api/encuentros', getAuth()).then(r => setEncuentros(r.data))
    axios.get('/api/admin/funcionarios', getAuth()).then(r => setFuncionarios(r.data)).catch(() => {})
    axios.get('/api/validacion/historial/todos', getAuth()).then(r => setHistorialValidaciones(r.data)).catch(() => {})
  }, [])

  const msg = (texto, tipo = 'ok') => { 
    setMensaje(texto)
    setTipoMensaje(tipo)
    setTimeout(() => setMensaje(''), 3000) 
  }

  const crearEquipo = async (e) => {
    e.preventDefault()
    await axios.post('/api/admin/equipos', equipoForm, getAuth())
    const r = await axios.get('/api/admin/equipos', getAuth())
    setEquipos(r.data)
    msg('Equipo creado')
    setEquipoForm({ pais: '' })
  }

  const crearEstadio = async (e) => {
    e.preventDefault()
    await axios.post('/api/admin/estadios', { ...estadioForm, aforo: parseInt(estadioForm.aforo) }, getAuth())
    const r = await axios.get('/api/admin/estadios', getAuth())
    setEstadios(r.data)
    msg('Estadio creado con sectores A, B, C, D')
    setEstadioForm({ nombre: '', direccion: '', pais: '', aforo: '' })
  }

  const crearEncuentro = async (e) => {
    e.preventDefault()
    await axios.post('/api/admin/encuentros', {
      ...encuentroForm, idEstadio: parseInt(encuentroForm.idEstadio)
    }, getAuth())
    const r = await axios.get('/api/encuentros', getAuth())
    setEncuentros(r.data)
    msg('Encuentro creado')
    setEncuentroForm({ fecha: '', hora: '', idEstadio: '', paisLocal: '', paisVisitante: '', emailAdmin: email })
  }

  const eliminarEstadio = async (id) => {
    try {
      await axios.delete(`/api/admin/estadios/${id}`, getAuth())
      setEstadios(estadios.filter(e => e.idEstadio !== id))
      msg('Estadio eliminado')
    } catch (err) {
      msg(err.response?.data?.error || 'Error al eliminar')
    }
  }

  const eliminarEncuentro = async (id) => {
    try {
      await axios.delete(`/api/admin/encuentros/${id}`, getAuth())
      setEncuentros(encuentros.filter(e => e.idEncuentro !== id))
      msg('Encuentro eliminado')
    } catch (err) {
      msg(err.response?.data?.error || 'Error al eliminar')
    }
  }

  const eliminarEquipo = async (pais) => {
    try {
      await axios.delete(`/api/admin/equipos/${pais}`, getAuth())
      setEquipos(equipos.filter(e => e.pais !== pais))
      msg('Equipo eliminado')
    } catch (err) {
      msg(err.response?.data?.error || 'Error al eliminar')
    }
  }

  const cargarSectoresHabilitados = async (idEncuentro) => {
    setEncuentroSeleccionado(idEncuentro)
    if (!idEncuentro) return
    const r = await axios.get(`/api/admin/encuentros/${idEncuentro}/sectores`, getAuth())
    setSectoresHabilitados(r.data.map(s => s.letra))
  }

  const habilitarSector = async (letra) => {
    const precio = preciosSector[letra] || 100
    await axios.post(`/api/admin/encuentros/${encuentroSeleccionado}/sectores`, { letra, precio: parseFloat(precio) }, getAuth())
    setSectoresHabilitados([...sectoresHabilitados, letra])
    msg(`Sector ${letra} habilitado a $${precio}`)
  }

  const deshabilitarSector = async (letra) => {
    await axios.delete(`/api/admin/encuentros/${encuentroSeleccionado}/sectores/${letra}`, getAuth())
    setSectoresHabilitados(sectoresHabilitados.filter(s => s !== letra))
    msg(`Sector ${letra} deshabilitado`)
  }

  const crearFuncionario = async (e) => {
    e.preventDefault()
    try {
      const res = await axios.post('/api/admin/funcionarios', funcionarioForm, getAuth())
      const r = await axios.get('/api/admin/funcionarios', getAuth())
      setFuncionarios(r.data)
      msg(`Funcionario creado — Legajo: ${res.data.numeroLegajo}, Dispositivo: ${res.data.idDispositivo}`)
      setFuncionarioForm({ email: '', password: '', paisDocumentoIdentidad: '', tipoDocumento: '', numeroDocumento: '' })
    } catch (err) {
      msg(err.response?.data?.error || 'Error al crear funcionario', 'error')
    }
  }

  const expandirFuncionario = async (email) => {
    if (funcionarioExpandido === email) { setFuncionarioExpandido(null); return }
    setFuncionarioExpandido(email)
    const r = await axios.get(`/api/admin/funcionarios/${encodeURIComponent(email)}/encuentros`, getAuth())
    setEncuentrosFuncionario(prev => ({ ...prev, [email]: r.data }))
  }

  const asignarEncuentro = async (email) => {
    if (!asignandoEncuentro) return
    try {
      await axios.post(`/api/admin/funcionarios/${encodeURIComponent(email)}/encuentros/${asignandoEncuentro}`, {}, getAuth())
      const r = await axios.get(`/api/admin/funcionarios/${encodeURIComponent(email)}/encuentros`, getAuth())
      setEncuentrosFuncionario(prev => ({ ...prev, [email]: r.data }))
      setAsignandoEncuentro('')
      msg('Encuentro asignado')
    } catch (err) {
      msg(err.response?.data?.error || 'Error al asignar', 'error')
    }
  }

  const desasignarEncuentro = async (email, idEncuentro) => {
    await axios.delete(`/api/admin/funcionarios/${encodeURIComponent(email)}/encuentros/${idEncuentro}`, getAuth())
    const r = await axios.get(`/api/admin/funcionarios/${encodeURIComponent(email)}/encuentros`, getAuth())
    setEncuentrosFuncionario(prev => ({ ...prev, [email]: r.data }))
    msg('Encuentro desasignado')
  }

  const editarEstadio = async (e) => {
    e.preventDefault()
    try {
      await axios.put(`/api/admin/estadios/${editandoEstadio}`, estadioEditForm, getAuth())
      const r = await axios.get('/api/admin/estadios', getAuth())
      setEstadios(r.data)
      msg('Estadio actualizado')
      setEditandoEstadio(null)
    } catch (err) {
      msg(err.response?.data?.error || 'Error al editar', 'error')
    }
  }

  const tabs = ['equipos', 'estadios', 'encuentros', 'sectores', 'funcionarios', 'validaciones']
  const inputClass = "w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
  const btnPrimary = "w-full bg-green-700 hover:bg-green-800 text-white font-semibold py-3 rounded-lg transition-colors"

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-green-900 text-white px-6 py-4 flex items-center justify-between shadow">
        <span className="text-lg font-bold">⚙️ Panel Administrador</span>
        <div className="flex gap-2">
          <button onClick={() => navigate('/rankings')}
            className="bg-yellow-600 hover:bg-yellow-500 text-white text-sm px-3 py-2 rounded-lg transition-colors">
            🏆 Rankings
          </button>
          <button onClick={() => { localStorage.clear(); navigate('/') }}
            className="bg-red-600 hover:bg-red-500 text-white text-sm px-3 py-2 rounded-lg transition-colors">
            Salir
          </button>
        </div>
      </div>

      {mensaje && (
        <div className="max-w-2xl mx-auto px-4 pt-4">
          <p className={`font-medium text-sm px-4 py-2 rounded-lg ${
            tipoMensaje === 'ok' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
          }`}>{mensaje}</p>
        </div>
      )}

      <div className="max-w-2xl mx-auto px-4 py-6">
        <div className="flex gap-2 mb-6 border-b border-gray-200">
          {tabs.map(t => (
            <button key={t} onClick={() => setTab(t)}
              className={`pb-2 px-3 text-sm font-medium transition-colors border-b-2 ${
                tab === t ? 'border-green-700 text-green-700' : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}>
              {t.charAt(0).toUpperCase() + t.slice(1)}
            </button>
          ))}
        </div>

        {tab === 'equipos' && (
          <div className="space-y-4">
            <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4">
              <h3 className="font-semibold text-gray-700 mb-3">Equipos registrados</h3>
              {equipos.length === 0 ? <p className="text-gray-400 text-sm">Sin equipos.</p> :
                <div className="space-y-2">
                  {equipos.map(eq => (
                    <div key={eq.pais} className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">🏳️ {eq.pais}</span>
                      <button onClick={() => eliminarEquipo(eq.pais)}
                        className="text-red-500 hover:text-red-700 text-xs">Eliminar</button>
                    </div>
                  ))}
                </div>}
            </div>
            <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4">
              <h3 className="font-semibold text-gray-700 mb-3">Agregar equipo</h3>
              <form onSubmit={crearEquipo} className="space-y-3">
                <input placeholder="País" value={equipoForm.pais}
                  onChange={e => setEquipoForm({ pais: e.target.value })}
                  className={inputClass} />
                <button type="submit" className={btnPrimary}>Crear equipo</button>
              </form>
            </div>
          </div>
        )}

        {tab === 'estadios' && (
          <div className="space-y-4">
            <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4">
              <h3 className="font-semibold text-gray-700 mb-3">Estadios registrados</h3>
              {estadios.length === 0 ? <p className="text-gray-400 text-sm">Sin estadios.</p> :
                <div className="space-y-3">
                  {estadios.map(es => (
                    <div key={es.idEstadio}>
                      <div className="flex justify-between items-center">
                        <span className="text-sm text-gray-600">🏟️ <strong>{es.nombre}</strong> — {es.direccion}, {es.pais}</span>
                        <div className="flex gap-2">
                          <button onClick={() => {
                            setEditandoEstadio(es.idEstadio)
                            setEstadioEditForm({ nombre: es.nombre, direccion: es.direccion, pais: es.pais })
                          }} className="text-blue-500 hover:text-blue-700 text-xs">Editar</button>
                          <button onClick={() => eliminarEstadio(es.idEstadio)}
                            className="text-red-500 hover:text-red-700 text-xs">Eliminar</button>
                        </div>
                      </div>
                      {editandoEstadio === es.idEstadio && (
                        <form onSubmit={editarEstadio} className="mt-2 space-y-2 bg-blue-50 rounded-lg p-3">
                          {[['nombre', 'Nombre'], ['direccion', 'Dirección'], ['pais', 'País']].map(([name, label]) => (
                            <input key={name} placeholder={label} value={estadioEditForm[name]}
                              onChange={e => setEstadioEditForm({ ...estadioEditForm, [name]: e.target.value })}
                              className={inputClass} />
                          ))}
                          <div className="flex gap-2">
                            <button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white text-xs px-4 py-2 rounded-lg transition-colors">Guardar</button>
                            <button type="button" onClick={() => setEditandoEstadio(null)} className="text-gray-500 text-xs px-4 py-2 rounded-lg border border-gray-300">Cancelar</button>
                          </div>
                        </form>
                      )}
                    </div>
                  ))}
                </div>}
            </div>
            <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4">
              <h3 className="font-semibold text-gray-700 mb-3">Agregar estadio</h3>
              <form onSubmit={crearEstadio} className="space-y-3">
                {[['nombre', 'Nombre'], ['direccion', 'Dirección'], ['pais', 'País'], ['aforo', 'Aforo total']].map(([name, label]) => (
                  <input key={name} placeholder={label} value={estadioForm[name]}
                    onChange={e => setEstadioForm({ ...estadioForm, [name]: e.target.value })}
                    className={inputClass} />
                ))}
                <p className="text-xs text-gray-400">Los sectores A, B, C y D se crean automáticamente.</p>
                <button type="submit" className={btnPrimary}>Crear estadio</button>
              </form>
            </div>
          </div>
        )}

        {tab === 'encuentros' && (
          <div className="space-y-4">
            <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4">
              <h3 className="font-semibold text-gray-700 mb-3">Encuentros registrados</h3>
              {encuentros.length === 0 ? <p className="text-gray-400 text-sm">Sin encuentros.</p> :
                <div className="space-y-2">
                  {encuentros.map(enc => (
                    <div key={enc.idEncuentro} className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">
                        ⚽ {enc.equipoLocal?.pais} vs {enc.equipoVisitante?.pais} — {enc.fecha}
                      </span>
                      <button onClick={() => eliminarEncuentro(enc.idEncuentro)}
                        className="text-red-500 hover:text-red-700 text-xs">Eliminar</button>
                    </div>
                  ))}
                </div>}
            </div>
            <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4">
              <h3 className="font-semibold text-gray-700 mb-3">Agregar encuentro</h3>
              <form onSubmit={crearEncuentro} className="space-y-3">
                <select value={encuentroForm.idEstadio}
                  onChange={e => setEncuentroForm({ ...encuentroForm, idEstadio: e.target.value })}
                  className={inputClass}>
                  <option value="">Seleccioná estadio</option>
                  {estadios.map(es => <option key={es.idEstadio} value={es.idEstadio}>{es.nombre}</option>)}
                </select>
                <select value={encuentroForm.paisLocal}
                  onChange={e => setEncuentroForm({ ...encuentroForm, paisLocal: e.target.value })}
                  className={inputClass}>
                  <option value="">Equipo local</option>
                  {equipos.map(eq => <option key={eq.pais} value={eq.pais}>{eq.pais}</option>)}
                </select>
                <select value={encuentroForm.paisVisitante}
                  onChange={e => setEncuentroForm({ ...encuentroForm, paisVisitante: e.target.value })}
                  className={inputClass}>
                  <option value="">Equipo visitante</option>
                  {equipos.map(eq => <option key={eq.pais} value={eq.pais}>{eq.pais}</option>)}
                </select>
                <input type="date" value={encuentroForm.fecha}
                  onChange={e => setEncuentroForm({ ...encuentroForm, fecha: e.target.value })}
                  className={inputClass} />
                <input type="time" value={encuentroForm.hora}
                  onChange={e => setEncuentroForm({ ...encuentroForm, hora: e.target.value })}
                  className={inputClass} />
                <button type="submit" className={btnPrimary}>Crear encuentro</button>
              </form>
            </div>
          </div>
        )}

        {tab === 'funcionarios' && (
          <div className="space-y-4">
            <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4">
              <h3 className="font-semibold text-gray-700 mb-3">Funcionarios registrados</h3>
              {funcionarios.length === 0 ? <p className="text-gray-400 text-sm">Sin funcionarios.</p> :
                <div className="space-y-2">
                  {funcionarios.map(f => (
                    <div key={f.email} className="border-b border-gray-50 last:border-0 pb-3 last:pb-0">
                      <div className="flex justify-between items-center">
                        <div>
                          <p className="text-sm text-gray-700 font-medium">{f.email}</p>
                          <p className="text-xs text-gray-400">Legajo: {f.numeroLegajo} — Dispositivo: DISP-{f.numeroLegajo?.substring(4)}</p>
                        </div>
                        <button onClick={() => expandirFuncionario(f.email)}
                          className="text-blue-500 hover:text-blue-700 text-xs">
                          {funcionarioExpandido === f.email ? 'Cerrar' : 'Encuentros'}
                        </button>
                      </div>
                      {funcionarioExpandido === f.email && (
                        <div className="mt-2 bg-blue-50 rounded-lg p-3 space-y-2">
                          <p className="text-xs font-medium text-blue-700">Encuentros asignados</p>
                          {(encuentrosFuncionario[f.email] || []).length === 0
                            ? <p className="text-xs text-gray-400">Sin encuentros asignados.</p>
                            : (encuentrosFuncionario[f.email] || []).map(a => (
                              <div key={a.idEncuentro} className="flex justify-between items-center">
                                <span className="text-xs text-gray-600">
                                  {a.encuentro?.equipoLocal?.pais} vs {a.encuentro?.equipoVisitante?.pais} — {a.encuentro?.fecha}
                                </span>
                                <button onClick={() => desasignarEncuentro(f.email, a.idEncuentro)}
                                  className="text-red-400 hover:text-red-600 text-xs">Quitar</button>
                              </div>
                            ))
                          }
                          <div className="flex gap-2 pt-1">
                            <select value={asignandoEncuentro}
                              onChange={e => setAsignandoEncuentro(e.target.value)}
                              className="flex-1 border border-gray-300 rounded-lg px-2 py-1.5 text-xs focus:outline-none">
                              <option value="">Asignar encuentro...</option>
                              {encuentros.map(enc => (
                                <option key={enc.idEncuentro} value={enc.idEncuentro}>
                                  {enc.equipoLocal?.pais} vs {enc.equipoVisitante?.pais} — {enc.fecha}
                                </option>
                              ))}
                            </select>
                            <button onClick={() => asignarEncuentro(f.email)}
                              className="bg-blue-600 hover:bg-blue-700 text-white text-xs px-3 py-1.5 rounded-lg transition-colors">
                              Asignar
                            </button>
                          </div>
                        </div>
                      )}
                    </div>
                  ))}
                </div>}
            </div>
            <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4">
              <h3 className="font-semibold text-gray-700 mb-3">Crear funcionario</h3>
              <p className="text-xs text-gray-400">El legajo y dispositivo se asignan automáticamente.</p>
              <form onSubmit={crearFuncionario} className="space-y-3">
                {[
                  ['email', 'Email'],
                  ['password', 'Contraseña'],
                  ['paisDocumentoIdentidad', 'País del documento'],
                  ['tipoDocumento', 'Tipo de documento'],
                  ['numeroDocumento', 'Número de documento'],
                ].map(([name, label]) => (
                  <input key={name} placeholder={label}
                    type={name === 'password' ? 'password' : 'text'}
                    value={funcionarioForm[name]}
                    onChange={e => setFuncionarioForm({ ...funcionarioForm, [name]: e.target.value })}
                    className={inputClass} />
                ))}
                <button type="submit" className={btnPrimary}>Crear funcionario</button>
              </form>
            </div>
          </div>
        )}

        {tab === 'validaciones' && (
          <div className="space-y-3">
            <h3 className="font-semibold text-gray-700">Historial de validaciones</h3>
            {historialValidaciones.length === 0
              ? <p className="text-gray-400 text-sm">Sin validaciones registradas.</p>
              : historialValidaciones.map((v, i) => (
                <div key={i} className="bg-white rounded-xl border border-gray-100 shadow-sm p-4">
                  <div className="flex justify-between items-start">
                    <div>
                      <p className="text-sm font-semibold text-green-900">Entrada #{v.idEntrada}</p>
                      <p className="text-xs text-gray-600">
                        {v.encuentro?.equipoLocal?.pais} vs {v.encuentro?.equipoVisitante?.pais} — {v.encuentro?.fecha}
                      </p>
                      <p className="text-xs text-gray-500">Funcionario: {v.funcionario?.email}</p>
                      <p className="text-xs text-gray-400">Dispositivo: {v.idDispositivo}</p>
                    </div>
                    <span className="text-xs text-gray-400">{v.hora}</span>
                  </div>
                </div>
              ))
            }
          </div>
        )}

        {tab === 'sectores' && (
          <div className="space-y-4">
            <select value={encuentroSeleccionado}
              onChange={e => cargarSectoresHabilitados(e.target.value)}
              className={inputClass}>
              <option value="">Seleccioná un encuentro</option>
              {encuentros.map(enc => (
                <option key={enc.idEncuentro} value={enc.idEncuentro}>
                  {enc.equipoLocal?.pais} vs {enc.equipoVisitante?.pais} — {enc.fecha}
                </option>
              ))}
            </select>

            {encuentroSeleccionado && (
              <div className="space-y-2">
                {['A', 'B', 'C', 'D'].map(letra => (
                  <div key={letra} className="bg-white rounded-xl border border-gray-100 shadow-sm p-4 flex justify-between items-center">
                    <span className="font-medium text-gray-700">Sector {letra}</span>
                    {sectoresHabilitados.includes(letra) ? (
                      <div className="flex items-center gap-2">
                        <span className="text-green-600 text-sm font-medium">✅ Habilitado</span>
                        <button onClick={() => deshabilitarSector(letra)}
                          className="bg-red-500 hover:bg-red-600 text-white text-xs px-3 py-1.5 rounded-lg transition-colors">
                          Deshabilitar
                        </button>
                      </div>
                    ) : (
                      <div className="flex items-center gap-2">
                        <input type="number" placeholder="Precio" min="0"
                          value={preciosSector[letra] || ''}
                          onChange={e => setPreciosSector({ ...preciosSector, [letra]: e.target.value })}
                          className="w-24 border border-gray-300 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-500" />
                        <button onClick={() => habilitarSector(letra)}
                          className="bg-green-700 hover:bg-green-800 text-white text-xs px-3 py-1.5 rounded-lg transition-colors">
                          Habilitar
                        </button>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}