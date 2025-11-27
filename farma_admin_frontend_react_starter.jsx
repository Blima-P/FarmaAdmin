// FarmaAdmin - Professional React frontend starter (single-file for preview)
// How to use:
// 1) Create a new React app (Vite or Create React App). Place this as src/App.jsx and install dependencies:
//    npm install axios react-router-dom
// 2) Add Tailwind CSS to your project (recommended) or adjust classes.
// 3) Set environment variable REACT_APP_API_URL (e.g. http://localhost:8080/api)
// 4) Run the app (npm run dev / npm start)

import React, { useState, useEffect, createContext, useContext } from "react";
import { BrowserRouter as Router, Routes, Route, Link, Navigate, useNavigate } from "react-router-dom";
import axios from "axios";

// ---------- Configuration ----------
const API_BASE = process.env.REACT_APP_API_URL || "http://localhost:8080/api";

// ---------- Axios instance ----------
const axiosInstance = axios.create({
  baseURL: API_BASE,
  headers: { "Content-Type": "application/json" },
});

// Interceptor to attach token
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem("fa_token");
  if (token) config.headers["Authorization"] = `Bearer ${token}`;
  return config;
});

// ---------- Auth Context ----------
const AuthContext = createContext();
function useAuth() {
  return useContext(AuthContext);
}

function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem("fa_user");
    return raw ? JSON.parse(raw) : null;
  });

  const login = async (email, senha) => {
    const res = await axiosInstance.post("/login", { email, senha });
    const token = res.data.token || res.data.accessToken || res.data;
    if (!token) throw new Error("Resposta de login sem token");
    localStorage.setItem("fa_token", token);
    // optional: fetch profile
    try {
      const profile = await axiosInstance.get("/me");
      localStorage.setItem("fa_user", JSON.stringify(profile.data));
      setUser(profile.data);
    } catch (err) {
      // if API has no /me, store basic email
      const fallback = { email };
      localStorage.setItem("fa_user", JSON.stringify(fallback));
      setUser(fallback);
    }
  };

  const logout = () => {
    localStorage.removeItem("fa_token");
    localStorage.removeItem("fa_user");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// ---------- Protected Route ----------
function ProtectedRoute({ children }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return children;
}

// ---------- UI Components ----------
function Topbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  return (
    <header className="bg-white shadow p-4 flex items-center justify-between">
      <div className="flex items-center gap-3">
        <div className="font-bold text-lg">FarmaAdmin</div>
        <nav className="hidden md:flex gap-2 text-sm">
          <Link to="/" className="px-2 py-1 rounded hover:bg-gray-100">Dashboard</Link>
          <Link to="/pessoas" className="px-2 py-1 rounded hover:bg-gray-100">Pessoas</Link>
        </nav>
      </div>
      <div className="flex items-center gap-3">
        <div className="text-sm text-gray-600">{user?.email}</div>
        <button
          className="px-3 py-1 bg-red-500 text-white rounded"
          onClick={() => {
            logout();
            navigate('/login');
          }}
        >
          Sair
        </button>
      </div>
    </header>
  );
}

function Container({ children }) {
  return <div className="max-w-6xl mx-auto p-4">{children}</div>;
}

// ---------- Pages ----------
function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await login(email, senha);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Erro no login');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="w-full max-w-md bg-white rounded shadow p-6">
        <h2 className="text-2xl font-semibold mb-4">Acessar FarmaAdmin</h2>
        {error && <div className="mb-2 text-red-600">{error}</div>}
        <form onSubmit={submit} className="space-y-3">
          <div>
            <label className="block text-sm">Email</label>
            <input value={email} onChange={(e) => setEmail(e.target.value)} className="w-full border rounded p-2" placeholder="seu@exemplo.com" />
          </div>
          <div>
            <label className="block text-sm">Senha</label>
            <input type="password" value={senha} onChange={(e) => setSenha(e.target.value)} className="w-full border rounded p-2" placeholder="••••••" />
          </div>
          <div>
            <button disabled={loading} className="w-full py-2 bg-indigo-600 text-white rounded">{loading ? 'Entrando...' : 'Entrar'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}

function Dashboard() {
  return (
    <Container>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-4 bg-white rounded shadow"> 
          <h3 className="font-semibold">Visão geral</h3>
          <p className="text-sm text-gray-500">Relatórios rápidos e métricas.</p>
        </div>
        <div className="p-4 bg-white rounded shadow"> 
          <h3 className="font-semibold">Vendas</h3>
          <p className="text-sm text-gray-500">Resumo das vendas recentes.</p>
        </div>
        <div className="p-4 bg-white rounded shadow"> 
          <h3 className="font-semibold">Usuários</h3>
          <p className="text-sm text-gray-500">Atividade e novos cadastros.</p>
        </div>
      </div>
    </Container>
  );
}

// Pessoas page: list + create/edit
function PessoasPage() {
  const [pessoas, setPessoas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [editing, setEditing] = useState(null);
  const [showForm, setShowForm] = useState(false);

  const fetch = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await axiosInstance.get('/pessoas');
      setPessoas(res.data || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Falha ao carregar');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetch(); }, []);

  const handleDelete = async (id) => {
    if (!window.confirm('Confirma exclusão?')) return;
    try {
      await axiosInstance.delete(`/pessoas/${id}`);
      setPessoas((s) => s.filter((p) => p.id !== id));
    } catch (err) {
      alert('Erro ao excluir: ' + (err.message || ''));
    }
  };

  return (
    <Container>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-semibold">Pessoas</h2>
        <div>
          <button
            className="px-3 py-1 bg-green-600 text-white rounded"
            onClick={() => { setEditing(null); setShowForm(true); }}
          >
            + Nova Pessoa
          </button>
        </div>
      </div>

      {loading && <div>Carregando...</div>}
      {error && <div className="text-red-600">{error}</div>}

      <div className="bg-white rounded shadow overflow-x-auto">
        <table className="w-full table-auto">
          <thead className="bg-gray-50">
            <tr>
              <th className="text-left p-2">ID</th>
              <th className="text-left p-2">Nome</th>
              <th className="text-left p-2">Email</th>
              <th className="text-left p-2">Ações</th>
            </tr>
          </thead>
          <tbody>
            {pessoas.map((p) => (
              <tr key={p.id} className="border-t">
                <td className="p-2">{p.id}</td>
                <td className="p-2">{p.nome || p.name}</td>
                <td className="p-2">{p.email}</td>
                <td className="p-2">
                  <button className="mr-2 px-2 py-1 bg-blue-600 text-white rounded" onClick={() => { setEditing(p); setShowForm(true); }}>Editar</button>
                  <button className="px-2 py-1 bg-red-500 text-white rounded" onClick={() => handleDelete(p.id)}>Excluir</button>
                </td>
              </tr>
            ))}
            {pessoas.length === 0 && !loading && <tr><td colSpan={4} className="p-4 text-gray-500">Nenhuma pessoa encontrada.</td></tr>}
          </tbody>
        </table>
      </div>

      {showForm && (
        <PessoaForm
          initial={editing}
          onClose={() => { setShowForm(false); setEditing(null); fetch(); }}
        />
      )}
    </Container>
  );
}

function PessoaForm({ initial, onClose }) {
  const [nome, setNome] = useState(initial?.nome || initial?.name || "");
  const [email, setEmail] = useState(initial?.email || "");
  const [saving, setSaving] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      if (initial && initial.id) {
        await axiosInstance.put(`/pessoas/${initial.id}`, { nome, email });
      } else {
        await axiosInstance.post('/pessoas', { nome, email });
      }
      onClose();
    } catch (err) {
      alert('Erro ao salvar: ' + (err.response?.data?.message || err.message));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center p-4">
      <div className="w-full max-w-lg bg-white rounded shadow p-6">
        <h3 className="font-semibold mb-3">{initial ? 'Editar Pessoa' : 'Nova Pessoa'}</h3>
        <form onSubmit={submit} className="space-y-3">
          <div>
            <label className="block text-sm">Nome</label>
            <input value={nome} onChange={(e) => setNome(e.target.value)} className="w-full border rounded p-2" />
          </div>
          <div>
            <label className="block text-sm">Email</label>
            <input value={email} onChange={(e) => setEmail(e.target.value)} className="w-full border rounded p-2" />
          </div>
          <div className="flex gap-2 justify-end">
            <button type="button" onClick={onClose} className="px-3 py-1 border rounded">Cancelar</button>
            <button type="submit" disabled={saving} className="px-4 py-1 bg-indigo-600 text-white rounded">{saving ? 'Salvando...' : 'Salvar'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}

// ---------- App ----------
export default function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="min-h-screen bg-gray-100">
          <Topbar />
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
            <Route path="/pessoas" element={<ProtectedRoute><PessoasPage /></ProtectedRoute>} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}
