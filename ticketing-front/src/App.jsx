import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Login from './pages/Login'
import Registro from './pages/Registro'
import Dashboard from './pages/Dashboard'
import Comprar from './pages/Comprar'
import Transferir from './pages/Transferir'
import Admin from './pages/Admin'
import Rankings from './pages/Rankings'
import Validacion from './pages/Validacion'


function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/registro" element={<Registro />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/comprar" element={<Comprar />} />
        <Route path="/transferir" element={<Transferir />} />
        <Route path="/admin" element={<Admin />} />
        <Route path="/rankings" element={<Rankings />} />
        <Route path="/validacion" element={<Validacion />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App