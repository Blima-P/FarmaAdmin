// OBS: Certifique-se de instalar 'react-router-dom': npm install react-router-dom

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Login from './pages/Login';
import Loja from './pages/Loja';

// Componente para proteger rotas
const PrivateRoute = ({ children, allowedTypes = ['CLIENTE', 'FARMACIA'] }) => {
  const { usuario, isLoading } = useAuth();

  if (isLoading) {
    return <div style={{textAlign: 'center', padding: '50px'}}>Carregando...</div>;
  }

  if (!usuario) {
    return <Navigate to="/" />;
  }

  if (!allowedTypes.includes(usuario.tipoUsuario)) {
    const redirectPath = usuario.tipoUsuario === 'FARMACIA' ? '/admin/dashboard' : '/loja';
    return <Navigate to={redirectPath} />;
  }

  return children;
};


const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<Login />} />

      {/* Rotas de Cliente */}
      <Route path="/loja" element={<PrivateRoute allowedTypes={['CLIENTE']}><Loja /></PrivateRoute>} />

      {/* Rotas de Farmácia (Admin) */}
      {/* <Route path="/admin/dashboard" element={<PrivateRoute allowedTypes={['FARMACIA']}><DashboardFarmacia /></PrivateRoute>} /> */}

      <Route path="*" element={<div style={{textAlign: 'center', padding: '50px'}}>Página não encontrada!</div>} />
    </Routes>
  );
};

const App = () => {
  return (
    <Router>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </Router>
  );
};

export default App;