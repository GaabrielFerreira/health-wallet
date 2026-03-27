# Guia de Contribuição — Health Wallet

## Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Git](https://git-scm.com/)
- Editor de código (recomendado: VS Code ou IntelliJ IDEA)

---

## Setup do projeto

### 1. Clonar o repositório

```bash
git clone https://github.com/GaabrielFerreira/health-wallet.git
cd health-wallet
```

### 2. Subir o ambiente com Docker

```bash
docker compose up -d --build
```

Aguarda os 3 containers subirem:
- `healthwallet-db` — PostgreSQL na porta 5432
- `healthwallet-backend` — Spring Boot na porta 8080
- `healthwallet-frontend` — React na porta 5173

Acessa `http://localhost:5173` no navegador.

### 3. Configurações locais (opcional)

Se precisar sobrescrever alguma configuração do Docker sem afetar o repositório (ex: mudar porta do PostgreSQL por conflito com outro serviço local), crie um arquivo `docker-compose.override.yml` na raiz do projeto:

```yaml
services:
  db:
    ports:
      - "5433:5432"
```

O Docker Compose carrega esse arquivo automaticamente junto com o `docker-compose.yml`. Ele já está no `.gitignore` — cada dev cria o seu localmente se necessário.

---

## Setup do Frontend

### Stack
- React + TypeScript
- Vite
- Tailwind CSS 3
- React Router Dom
- Axios

### Criar projeto com Vite

```bash
npm create vite@latest frontend -- --template react-ts
cd frontend
npm install
```

### Instalar Tailwind CSS

```bash
npm install tailwindcss@3.4.17 postcss@8 autoprefixer
npx tailwindcss init -p
```

### Configurar `tailwind.config.js`

```js
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
};
```

### Configurar `src/index.css`

```css
@tailwind base;
@tailwind components;
@tailwind utilities;
```

### Instalar dependências extras

```bash
npm install react-router-dom axios
```

### Criar estrutura de pastas

```bash
mkdir src/components src/pages src/routes src/hooks src/context src/services src/utils
```

### Criar `src/routes/index.tsx`

```tsx
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

export function AppRoutes() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<div>Home</div>} />
      </Routes>
    </Router>
  );
}
```

### Atualizar `src/App.tsx`

```tsx
import { AppRoutes } from './routes';

function App() {
  return (
    <AppRoutes />
  );
}

export default App;
```

### Verificar `src/main.tsx`

```tsx
import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import "./index.css";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
```

### Estrutura final de pastas

```
src/
├── components/
├── pages/
├── routes/
│   └── index.tsx
├── hooks/
├── context/
├── services/
├── utils/
├── App.tsx
├── main.tsx
└── index.css
```

---

## Setup do Backend

### Stack
- Java 21
- Spring Boot 3.5.13
- Spring Data JPA + Hibernate
- Spring Security + JWT
- PostgreSQL 16

### Dependências (Spring Initializr)

| Dependência | Categoria |
|---|---|
| Spring Web | Web |
| Spring Data JPA | SQL |
| Spring Security | Security |
| PostgreSQL Driver | SQL |
| Lombok | Developer Tools |
| Spring Boot DevTools | Developer Tools |
| Validation | I/O |

### Estrutura de pacotes

```
src/main/java/com/healthwallet/
├── controller/     # @RestController — recebe e responde requisições HTTP
├── service/        # @Service — regras de negócio
├── repository/     # @Repository — acesso ao banco via JPA
├── model/          # @Entity — classes mapeadas para tabelas
├── dto/            # Objetos de transferência de dados
└── security/       # JWT + filtros do Spring Security
```

---

## Fluxo de desenvolvimento

### Branches

```
prod      # produção — só merge via PR vindo de stage
stage     # homologação/testes — só merge via PR vindo de dev
dev       # desenvolvimento — branch base do time
```

### Fluxo de merge

```
dev → stage → prod
```

### Padrão de commits

```
feat: short description in english
fix: short description in english
refactor: short description in english
docs: short description in english
test: short description in english
chore: short description in english
```

### Exemplos

```bash
git commit -m "feat: add POST /users endpoint with CPF validation"
git commit -m "fix: 500 error when registering anamnesis without blood type"
git commit -m "docs: update README with setup instructions"
```

---

## Portas dos serviços

| Serviço | Porta | URL |
|---|---|---|
| Frontend | 5173 | http://localhost:5173 |
| Backend | 8080 | http://localhost:8080 |
| PostgreSQL | 5432 | localhost:5432 |

---

## Comandos úteis

```bash
# Subir todos os containers em background
docker compose up -d --build

# Ver logs do backend
docker compose logs -f -t backend 

# Ver logs do frontend
docker compose logs -f -t frontend

# Parar todos os containers
docker compose down

# Parar e remover volumes (reseta o banco)
docker compose down -v
```