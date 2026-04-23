import React, { useEffect, useState } from 'react'
import { Navigate, Routes, Route, useLocation } from 'react-router-dom'
import { NavigationListener } from './listener/NavigationListener.jsx'
import { useAuthStore } from '../src/store/useAuthStore.js'

import Navbar from './components/Navbar.jsx'
import Home from './pages/Home.jsx'
import LoginForm from './pages/LoginForm.jsx'
import SignUpForm from './pages/SignUpForm.jsx'
import SearchByPrice from './pages/SearchByPrice.jsx'

export default function App() {
  const location = useLocation()
  const { setUser, setToken, token } = useAuthStore()
  const [initialized, setInitialized] = useState(false)

  useEffect(() => {
    const storedUser = localStorage.getItem('user')
    const storedToken = localStorage.getItem('token')

    if (storedUser && storedToken) {
      setUser(JSON.parse(storedUser))
      setToken(storedToken)
    }

    setInitialized(true)
  }, [setUser, setToken])

  const isAuthenticated = !!token

  if (!initialized) {
    return <div>Carregando...</div>
  }

  return (
    <div className="flex flex-col h-screen w-screen" data-theme="dark">
      <NavigationListener />
      <Navbar />

      <Routes key={location.pathname}>
        <Route path="/" element={isAuthenticated ? <Home /> : <Navigate to="/login" />} />

        <Route path="/price-search" element={isAuthenticated ? <SearchByPrice /> : <Navigate to="/login" />} />

        <Route
          path="/login"
          element={!isAuthenticated ? <LoginForm /> : <Navigate to="/" />}
        />

        <Route
          path="/signup"
          element={!isAuthenticated ? <SignUpForm /> : <Navigate to="/" />}
        />
      </Routes>
    </div>
  )
}


/*
function App() {
  const isAuthenticated = useUserStore((state) => state.isAuthenticated())

  const handleLogin = async () => {
    try {
      await login('test@email.com','Estrela24!!!')
      alert('Login realizado com sucesso')
    } catch (err) {
      alert('Credenciais inválidas')
      console.error(err)
    }
  }

  return (
    <div className="App">
      <h1>Atlantic Mercure App</h1>
      {isAuthenticated ? (
        <p>Usuário autenticado</p>
      ) : (
        <button onClick={handleLogin}>Login</button>
      )}

      <button className="btn btn-primary">Button</button>
    </div>
  )
}
*/

/*


*/





