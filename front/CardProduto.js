import React from 'react';

const CardProduto = ({ produto }) => {
  const handleAddToCart = () => {
    alert(`Produto "${produto.nome}" adicionado ao carrinho! (Funcionalidade de Carrinho pendente)`);
  };

  const cardStyles = {
    // Estilos para um card simples
    card: { /* ... */ border: '1px solid #ddd', borderRadius: '6px', padding: '15px', margin: '10px', width: '300px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)', display: 'flex', flexDirection: 'column', justifyContent: 'space-between', minHeight: '200px', fontFamily: 'Arial, sans-serif' },
    title: { /* ... */ fontSize: '1.2em', marginBottom: '5px', color: '#333', },
    description: { /* ... */ fontSize: '0.9em', color: '#666', marginBottom: '10px', flexGrow: 1, },
    details: { /* ... */ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px', },
    price: { /* ... */ fontWeight: 'bold', color: '#28a745', fontSize: '1.1em', },
    stock: { /* ... */ fontSize: '0.9em', color: '#999', },
    button: { /* ... */ padding: '8px 15px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', transition: 'background-color 0.2s', },
  };

  return (
    <div style={cardStyles.card}>
      <h3 style={cardStyles.title}>{produto.nome}</h3>
      <p style={cardStyles.description}>{produto.descricao}</p>

      <div style={cardStyles.details}>
        <span style={cardStyles.price}>
          R$ {produto.preco ? produto.preco.toFixed(2).replace('.', ',') : 'N/A'}
        </span>
        <span style={cardStyles.stock}>
          Estoque: {produto.estoque || 0}
        </span>
      </div>

      <button
        onClick={handleAddToCart}
        disabled={produto.estoque <= 0}
        style={{...cardStyles.button, backgroundColor: produto.estoque <= 0 ? '#ccc' : cardStyles.button.backgroundColor}}
      >
        {produto.estoque > 0 ? 'Adicionar ao Carrinho' : 'Indisponível'}
      </button>
    </div>
  );
};

export default CardProduto;