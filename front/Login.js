import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';

const Login = () => {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [error, setError] = useState('');
  const { login } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!email || !senha) {
        setError('Por favor, preencha o email e a senha.');
        return;
    }

    try {
      await login(email, senha);
    } catch (err) {
      setError(err.message || 'Ocorreu um erro ao tentar logar.');
    }
  };

  // ... (Código JSX e Styles omitidos para brevidade, use o código anterior)
  // [Conteúdo do código de Login.jsx]
  return (
    <div style={styles.container}>
      <h2>Acesso ao FarmaAdmin</h2>

      <form onSubmit={handleSubmit} style={styles.form}>
        <div style={styles.formGroup}>
          <label htmlFor="email">Email:</label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            style={styles.input}
          />
        </div>

        <div style={styles.formGroup}>
          <label htmlFor="senha">Senha:</label>
          <input
            type="password"
            id="senha"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            required
            style={styles.input}
          />
        </div>

        {error && <p style={styles.error}>{error}</p>}

        <button type="submit" style={styles.button}>Entrar</button>
      </form>

      <p style={styles.registerLink}>
        Novo por aqui? <a href="/register">Crie sua conta.</a>
      </p>
    </div>
  );
};

const styles = { /* ... (Estilos definidos anteriormente) ... */
    container: {
        maxWidth: '400px',
        margin: '50px auto',
        padding: '20px',
        border: '1px solid #ccc',
        borderRadius: '8px',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
        textAlign: 'center',
        fontFamily: 'Arial, sans-serif'
    },
    form: {
        display: 'flex',
        flexDirection: 'column',
        gap: '15px',
    },
    formGroup: {
        textAlign: 'left',
    },
    input: {
        width: '100%',
        padding: '10px',
        marginTop: '5px',
        boxSizing: 'border-box',
        borderRadius: '4px',
        border: '1px solid #ddd',
    },
    button: {
        padding: '10px',
        backgroundColor: '#007bff',
        color: 'white',
        border: 'none',
        borderRadius: '4px',
        cursor: 'pointer',
    },
    error: {
        color: 'red',
        fontSize: '0.9em',
    },
    registerLink: {
        marginTop: '20px',
        fontSize: '0.9em',
    }
};

export default Login;