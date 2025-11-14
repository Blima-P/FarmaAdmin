import React, { createContext, useState, useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { login as loginApi } from '../services/api';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [usuario, setUsuario] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const storedUser = localStorage.getItem('usuarioLogado');
    if (storedUser) {
      setUsuario(JSON.parse(storedUser));
    }
    setIsLoading(false);
  }, []);

  const login = async (email, senha) => {
    try {
      const user = await loginApi(email, senha);

      const userData = {
        id: user.id,
        nome: user.nome,
        email: user.email,
        tipoUsuario: user.tipoUsuario, // CLIENTE ou FARMACIA
      };

      setUsuario(userData);
      localStorage.setItem('usuarioLogado', JSON.stringify(userData));

      if (user.tipoUsuario === 'FARMACIA') {
        navigate('/admin/dashboard');
      } else {
        navigate('/loja');
      }

      return true;

    } catch (error) {
      console.error("Falha no login:", error);
      const errorMessage = error.response?.data?.message || error.response?.data || "Erro de conexão ou credenciais inválidas.";
      throw new Error(errorMessage);
    }
  };

  const logout = () => {
    setUsuario(null);
    localStorage.removeItem('usuarioLogado');
    navigate('/');
  };

  return (
    <AuthContext.Provider value={{ usuario, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  return useContext(AuthContext);
};