# EventHub Frontend

Frontend React para o sistema EventHub - Sistema de Gestão de Eventos.

## 🚀 Tecnologias

- **React 19** - Biblioteca UI
- **TypeScript 5** - Tipagem estática
- **Vite 7** - Build tool
- **TailwindCSS 4** - Estilização
- **React Router 7** - Roteamento
- **TanStack Query 5** - Gerenciamento de estado servidor
- **Axios** - Cliente HTTP
- **Lucide React** - Ícones
- **date-fns** - Manipulação de datas

## 📁 Estrutura

```
src/
├── api/              # Cliente Axios e serviços de API
│   ├── client.ts     # Configuração Axios com interceptors JWT
│   └── index.ts      # Funções de API (auth, events, tickets, chat)
├── components/       # Componentes reutilizáveis
│   ├── EventCard.tsx # Card de evento
│   ├── Layout.tsx    # Layout com navbar e footer
│   ├── ProtectedRoute.tsx # Proteção de rotas autenticadas
│   └── TicketCard.tsx # Card de ingresso
├── contexts/         # Contextos React
│   └── AuthContext.tsx # Autenticação (login, logout, register)
├── pages/            # Páginas da aplicação
│   ├── AdminPage.tsx # Painel admin (CRUD eventos)
│   ├── ChatPage.tsx  # Chat com IA (Ollama)
│   ├── EventDetailPage.tsx # Detalhes + compra
│   ├── EventsPage.tsx # Lista de eventos
│   ├── HomePage.tsx  # Landing page
│   ├── LoginPage.tsx # Login
│   ├── MyTicketsPage.tsx # Meus ingressos
│   └── RegisterPage.tsx # Cadastro
├── types/            # Definições TypeScript
│   └── index.ts      # Interfaces (Event, Ticket, User, etc)
├── App.tsx           # Configuração de rotas
├── index.css         # Estilos globais + Tailwind
└── main.tsx          # Entry point
```

## 🛠️ Desenvolvimento Local

```bash
# Instalar dependências
npm install

# Rodar em modo desenvolvimento (porta 5173)
npm run dev

# Build de produção
npm run build

# Preview do build
npm run preview
```

## 🐳 Docker

O frontend é servido por Nginx em produção:

```bash
# Build da imagem
docker build -t eventhub-frontend .

# Rodar container
docker run -p 3000:80 eventhub-frontend
```

## 🔗 Configuração de Proxy

### Desenvolvimento (Vite)
O `vite.config.ts` está configurado para proxy de `/api` para `localhost:8080`.

### Produção (Nginx)
O `nginx.conf` faz proxy de `/api/*` para o container `backend:8080`.

## 📱 Páginas

| Rota | Componente | Descrição | Acesso |
|------|------------|-----------|--------|
| `/` | HomePage | Landing page | Público |
| `/login` | LoginPage | Login | Público |
| `/register` | RegisterPage | Cadastro | Público |
| `/events` | EventsPage | Lista de eventos | Público |
| `/events/:id` | EventDetailPage | Detalhes + compra | Público (compra requer login) |
| `/my-tickets` | MyTicketsPage | Meus ingressos | Autenticado |
| `/chat` | ChatPage | Chat com IA | Autenticado |
| `/admin` | AdminPage | CRUD de eventos | Admin |

## 🎨 Design

- **TailwindCSS** para estilização utility-first
- **Lucide React** para ícones consistentes
- **Gradientes** azul/indigo como cores principais
- **Design responsivo** mobile-first
- **Animações sutis** para melhor UX
