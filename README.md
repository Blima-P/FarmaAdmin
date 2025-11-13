💊 FarmaAdmin - Marketplace de Farmácias

Este repositório contém o projeto de Programação Orientada a Objetos (POO) para o desenvolvimento do nosso marketplace de farmácias, a "FarmaAdmin". O projeto visa implementar todos os fluxos de usuário e administração, com persistência de dados em um banco de dados relacional.

🌟 Funcionalidades Destacadas

Implementamos os seguintes perfis e fluxos completos:

1. Perfil do Usuário

Fluxo de Compra Completo: Cadastro/Login, busca de produtos, adição ao carrinho, checkout e finalização do pedido.

Lista de Favoritos: Funcionalidade de adicionar e gerenciar produtos favoritos.

Histórico de Pedidos: Acompanhamento de pedidos ativos e visualização de compras anteriores.

2. Perfil do Administrador do Estabelecimento (Farmácia)

CRUD de Produtos: Criação, Leitura, Atualização e Deleção (CRUD) de produtos e gestão de estoque.

Gestão de Pedidos: Acompanhamento em tempo real de novos pedidos, alteração de status (Em preparo, Em transporte, Entregue).

Visualização de Métricas: Painel simples com resumo de vendas e produtos mais vendidos.

3. Conectividade e Persistência

Conexão estável com um Banco de Dados Relacional (ex: MySQL, PostgreSQL) via JDBC.

Mapeamento de classes POO para tabelas (tendo em vista a persistência e recuperação de objetos).

🚀 Configuração do Ambiente (IntelliJ IDEA)

Este projeto foi desenvolvido utilizando o IntelliJ IDEA. Siga as instruções abaixo para configurar seu ambiente.

Pré-requisitos

Java Development Kit (JDK): Versão 17 ou superior.

IntelliJ IDEA: Edição Community ou Ultimate.

Banco de Dados: Servidor MySQL ou PostgreSQL instalado e em execução.

1. Clonagem e Abertura do Projeto

Clone o repositório:

git clone [URL_DO_SEU_REPOSITORIO]



No IntelliJ, selecione "Open" e navegue até a pasta raiz do projeto clonado. O IntelliJ deve reconhecer o projeto Java automaticamente.

2. Configuração do Banco de Dados

O projeto requer um banco de dados ativo.

Crie um banco de dados vazio com o nome farma_admin (ou outro nome a ser padronizado pelo grupo).

Driver JDBC: Você precisará do JAR do driver JDBC correspondente (ex: mysql-connector-j.jar ou postgresql-42.x.x.jar).

No IntelliJ, vá em File > Project Structure... > Libraries.

Clique no +, selecione Java e adicione o arquivo JAR do driver.

Arquivo de Configuração:

Edite o arquivo (a ser criado pelo grupo, e.g., db.properties ou em uma classe DatabaseConfig) para inserir suas credenciais de acesso local:

DB_URL=jdbc:mysql://localhost:3306/farma_admin
DB_USER=seu_usuario
DB_PASSWORD=sua_senha



3. Execução

Após a configuração das bibliotecas e do banco de dados:

Navegue até a classe src/Main.java.

Clique com o botão direito e selecione "Run 'Main.main()'".

🧩 Módulo Complementar (Fase Final)

O módulo complementar, a ser entregue até o dia 20, será desenvolvido dentro do pacote src/modulo_complementar. Sugestões incluem:

Integração com uma API de cálculo de frete.

Funcionalidade de Receitas Digitais (Upload, validação, e restrição de compra).

Sistema de Avaliação e Review de Produtos/Farmácias.

Geração de Relatórios em PDF/CSV.
