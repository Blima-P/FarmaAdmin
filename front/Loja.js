import React, { useState, useEffect } from 'react';
import { listarProdutos } from '../services/api';
import CardProduto from '../components/CardProduto';

const Loja = () => {
  const [produtos, setProdutos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProdutos = async () => {
      try {
        const data = await listarProdutos();
        setProdutos(data);
      } catch (err) {
        console.error("Erro ao carregar produtos:", err);
        setError('Não foi possível carregar os produtos. Verifique se o back-end está rodando em http://localhost:8080.');
      } finally {
        setLoading(false);
      }
    };

    fetchProdutos();
  }, []);

  if (loading) {
    return <div style={lojaStyles.loading}>Carregando produtos...</div>;
  }

  if (error) {
    return <div style={lojaStyles.error}>{error}</div>;
  }

  const lojaStyles = {
    container: { /* ... */ padding: '20px', textAlign: 'center', fontFamily: 'Arial, sans-serif' },
    loading: { /* ... */ textAlign: 'center', fontSize: '1.2em', marginTop: '50px', },
    error: { /* ... */ textAlign: 'center', fontSize: '1.2em', color: 'red', marginTop: '50px', },
    grid: { /* ... */ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '20px', marginTop: '20px', }
  };

  return (
    <div style={lojaStyles.container}>
      <h1>Nossos Produtos 💊</h1>

      {produtos.length === 0 ? (
          <p>Nenhum produto disponível no momento.</p>
      ) : (
          <div style={lojaStyles.grid}>
              {produtos.map((produto) => (
                  <CardProduto key={produto.id} produto={produto} />
              ))}
          </div>
      )}
    </div>
  );
};

export default Loja;