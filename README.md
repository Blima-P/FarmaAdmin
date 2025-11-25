# 💊 FarmaAdmin - Marketplace de Farmácia

> Projeto de Programação Orientada a Objetos (POO)
>
> **Status do Projeto:** Versão final

---

## Visão Geral do Projeto

A **FarmaAdmin** é um marketplace digital desenvolvido para conectar farmácias (parceiros administradores) a clientes finais, oferecendo uma experiência de compra ágil, segura e orientada por princípios de POO.

Este projeto visa demonstrar a aplicação de conceitos avançados de Orientação a Objetos, Design Patterns (como DAO) e persistência de dados em um contexto de negócio real.

### Equipe (Grupo 7)

| Nome do Integrante | Matrícula (UC) |
| :--- | :--- |
| Pedro Braga de Lima | UC24101578 |
| Nicole Reinaldo de Carvalho | UC24101278 |
| Maria Clara Paiva Oliveira Camelo | UC24102551 |
| Maria Clara Ferreira dos Santos | UC24201543 |
| Lívia Fernandes Ribeiro Bezerra | UC24101407 |

---

## Funcionalidades e Fluxos Implementados

O projeto cobre dois perfis de usuário principais, focando na **conexão completa com o Banco de Dados** em todos os fluxos.

### Perfil do Usuário Comum

| Fluxo | Descrição | Status |
| :--- | :--- | :--- |
| **Compra Rápida** | Cadastro/Login, Busca, Adição ao Carrinho, Checkout e Finalização de Pedido. | ✅ Completo |
| **Favoritos** | Gerenciamento de uma lista personalizada de produtos para compra futura. | ✅ Completo |
| **Histórico** | Visualização de pedidos passados, status de pedidos ativos e detalhes da compra. | ✅ Completo |

### Perfil do Administrador (Farmácia Parceira)

| Funcionalidade | Descrição | Status |
| :--- | :--- | :--- |
| **Gestão de Produtos** (CRUD) | Criação, Edição, Deleção e Consulta de medicamentos e itens de estoque. | ✅ Completo |
| **Monitoramento de Pedidos** | Acompanhamento em tempo real de novos pedidos, alteração de status (Preparando, Enviado, Entregue). | ✅ Completo |
| **Métricas** | Visualização de relatórios básicos de vendas e estoque. | ✅ Completo |

### Módulo Complementar (Entrega Final - 20/11)

O módulo complementar é a funcionalidade extra que aprimora o projeto.

> **Módulo Escolhido:** 

---

## Estrutura e Tecnologia

### Linguagem & Ambiente

- **Linguagem:** Java (JDK 21 LTS recomendado)
- **IDE Padrão:** IntelliJ IDEA / VS Code
- **Princípio de Design:** Figma (visual definido pelo time)

### Persistência de Dados (Terminal / Backend)

- **Banco de Dados:** MySQL (ex.: 8.x)
- **Conexão:** JDBC nativo via `DatabaseConfig`
- **Padrão Utilizado:** Data Access Object (DAO)

### Como preparar o banco de dados

1. Abra seu cliente MySQL (root) e execute `sql/schema.sql` para criar o banco, usuário e tabelas.

```sql
-- No MySQL CLI como root
SOURCE sql/schema.sql;
```

2. Verifique que o usuário e senha definidos em `src/main/java/br/com/farmaadmin/util/DatabaseConfig.java` batem com o criado no script.

### Como rodar a aplicação (terminal)

Copie/cole no PowerShell (Windows):

```powershell
$env:JAVA_HOME = "C:\Users\pedroblima\.jdk\jdk-21.0.8"
$env:PATH = "C:\Users\pedroblima\.maven\maven-3.9.11\bin;$env:PATH"
cd C:\Users\pedroblima\ll\FarmaAdmin

# Compilar
mvn clean compile

# Executar (modo terminal - usa a classe Main)
mvn exec:java
```

Ou execute o helper `run.ps1`:

```powershell
.\run.ps1
```

Todos os dados de usuários, produtos, pedidos e itens são gravados diretamente no banco MySQL através das classes DAO.
