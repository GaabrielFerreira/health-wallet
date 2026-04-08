# Health Wallet

Sistema de gerenciamento de dados de saúde pessoal desenvolvido como projeto acadêmico na disciplina de Engenharia de Software — UniFacens, Engenharia de Computação, 7º Semestre.

## Sobre o projeto

O Health Wallet permite ao usuário centralizar informações de saúde como histórico de vacinas, anamnese, consultas clínicas e relatórios de saúde em um único lugar, com controle de acesso e compartilhamento seguro com profissionais de saúde.

## Stack

| Camada | Tecnologia |
|---|---|
| Frontend | React + TypeScript + Vite + Tailwind CSS |
| Backend | Java 21 + Spring Boot 3.5 + Spring Security |
| Banco de dados | PostgreSQL 16 |
| ORM | Spring Data JPA + Hibernate |
| Autenticação | JWT |
| Infraestrutura | Docker + Docker Compose |
| Versionamento | GitHub |

## Equipe

| Nome | RA | Papel |
|---|---|---|
| Bruno da Silveira Escanhoela | 236793 | Tech Lead / Dev |
| Gabriel Ferreira do Nascimento | 236085 | Scrum Master |
| João Guilherme Volta Kinol | 235255 | Product Owner |

## Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado

Não é necessário instalar Java, Node.js ou PostgreSQL — tudo roda dentro dos containers Docker.

## Como rodar o projeto

```bash
# 1. Clone o repositório
git clone https://github.com/software-eng-2026S1/health-wallet.git
cd health-wallet

# 2. Suba os containers
docker compose up -d --build

# 3. Acesse no navegador
# Frontend: http://localhost:5173
# Backend:  http://localhost:8080
# Banco:    localhost:5432
```

## Estrutura do repositório

```
health-wallet/
├── backend/                    # Spring Boot
│   ├── src/main/java/com/healthwallet/
│   │   ├── controller/         # REST Controllers
│   │   ├── service/            # Regras de negócio
│   │   ├── repository/         # Spring Data JPA
│   │   ├── model/              # Entities JPA
│   │   ├── dto/                # Data Transfer Objects
│   │   └── security/           # JWT + Spring Security
│   ├── src/main/resources/
│   │   └── application.yml     # Configurações
│   └── Dockerfile
├── frontend/                   # React + TypeScript
│   ├── src/
│   │   ├── components/         # Componentes reutilizáveis
│   │   ├── pages/              # Telas da aplicação
│   │   ├── routes/             # Gerenciamento de rotas
│   │   ├── services/           # Chamadas HTTP (Axios)
│   │   ├── context/            # Estado global
│   │   ├── hooks/              # Hooks personalizados
│   │   └── utils/              # Funções auxiliares
│   └── Dockerfile
├── docker-compose.yml
├── .gitignore
└── README.md
```

## Requisitos Funcionais

- RF1 — Cadastro de Usuários
- RF2 — Registrar Anamnese
- RF3 — Cadastrar Histórico de Vacinação
- RF4 — Registrar Histórico de Consultas Clínicas
- RF5 — Gerar Relatório de Saúde

## Gerenciamento do projeto

O projeto é gerenciado via Jira com metodologia SCRUMBAN, dividido em 4 sprints com estimativas por Planning Poker.
