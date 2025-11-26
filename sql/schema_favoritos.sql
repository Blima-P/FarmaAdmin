-- Schema for Favoritos module
-- Run this after the main schema (usuario, produto tables must exist)

CREATE TABLE IF NOT EXISTS favorito (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NOT NULL,
  produto_id INT NOT NULL,
  criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_usuario_produto (usuario_id, produto_id),
  CONSTRAINT fk_favorito_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_favorito_produto FOREIGN KEY (produto_id) REFERENCES produto(id) ON DELETE CASCADE ON UPDATE CASCADE
);
