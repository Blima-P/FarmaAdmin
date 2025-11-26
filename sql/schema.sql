-- Schema SQL para FarmaAdmin
-- Cria banco, usuário e tabelas necessárias

-- Crie o banco (execute como root ou usuário com privilégios)
CREATE DATABASE IF NOT EXISTS farma_admin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE farma_admin;

-- Cria usuário e senha (ajuste conforme necessário)
-- Substitua 'farma_senha123' se quiser outra senha
CREATE USER IF NOT EXISTS 'farma_user'@'localhost' IDENTIFIED BY 'farma_senha123';
GRANT ALL PRIVILEGES ON farma_admin.* TO 'farma_user'@'localhost';
FLUSH PRIVILEGES;

-- Tabela usuario
CREATE TABLE IF NOT EXISTS usuario (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  senha VARCHAR(255) NOT NULL,
  tipo_usuario VARCHAR(50) NOT NULL
);

-- Tabela produto
CREATE TABLE IF NOT EXISTS produto (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(255) NOT NULL,
  descricao TEXT,
  preco DECIMAL(10,2) NOT NULL,
  estoque INT NOT NULL DEFAULT 0,
  farmacia_id INT NOT NULL,
  categoria VARCHAR(100),
  FOREIGN KEY (farmacia_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Tabela pedido
CREATE TABLE IF NOT EXISTS pedido (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NOT NULL,
  data_pedido DATETIME DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(50) NOT NULL,
  valor_total DECIMAL(12,2) NOT NULL,
  endereco_entrega TEXT,
  FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Tabela itens_pedido
CREATE TABLE IF NOT EXISTS itens_pedido (
  id INT AUTO_INCREMENT PRIMARY KEY,
  pedido_id INT NOT NULL,
  produto_id INT NOT NULL,
  quantidade INT NOT NULL,
  preco_unitario DECIMAL(10,2) NOT NULL,
  FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON DELETE CASCADE,
  FOREIGN KEY (produto_id) REFERENCES produto(id) ON DELETE RESTRICT
);

-- Dados iniciais (opcional)
INSERT IGNORE INTO usuario (id, nome, email, senha, tipo_usuario) VALUES
  (1, 'Farmacia Exemplo', 'farmacia@example.com', 'senha123', 'FARMACIA');

-- Exemplo de produtos
INSERT IGNORE INTO produto (nome, descricao, preco, estoque, farmacia_id, categoria) VALUES
  ('Paracetamol 500mg', 'Caixa com 20 comprimidos', 9.90, 50, 1, 'Analgesico'),
  ('Xarope Tosse', 'Frasco 200ml', 14.50, 30, 1, 'Tosse');
