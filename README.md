# 🎫 EventHub - Sistema de Gerenciamento de Eventos

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?logo=springboot)
![React](https://img.shields.io/badge/React-18-blue?logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-5-blue?logo=typescript)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)

Sistema completo de gerenciamento de eventos com autenticação, compra de ingressos e chat com IA.

## 📋 Índice
- [Requisitos](#requisitos)
- [Como Rodar](#como-rodar)
- [Rotas e Acessos](#rotas-e-acessos)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)


---

## 📦 Requisitos

- **Docker** (versão 24.x ou superior)
- **Docker Compose** (versão 2.x ou superior)

Apenas isso é necessário para rodar o projeto completo!

---

## 🚀 Como Rodar

```bash
# 1. Clone o repositório
git clone <url-do-repositorio>
cd eventhub

# 2. Configure as variáveis de ambiente
cp .env.example .env

# 3. Inicie os serviços
docker-compose up -d
```

Pronto! Aguarde 2-3 minutos e acesse http://localhost:3000

### (Opcional) Baixar Modelo de IA para Chat

```bash
docker exec eventhub-ollama ollama pull llama3.2:3b
```

> 💡 **Dica**: O arquivo `.env.example` já contém valores padrão funcionais. Você pode editar o `.env` se precisar customizar

---

## 🌐 Rotas e Acessos

Após iniciar os serviços, acesse:

| Serviço | URL | Descrição |
|---------|-----|-----------|
| **Frontend** | http://localhost:3000 | Interface do usuário (React) |
| **API Backend** | http://localhost:8080/api | API REST (Spring Boot) |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Documentação interativa da API |
| **Ollama API** | http://localhost:11434 | API do modelo de IA |

### Credenciais Padrão

| Papel | Usuário | Senha |
|-------|---------|-------|
| **Admin** | admin | admin123 |
| **Usuário** | user | user123 |

### Parar os Serviços

```bash
# Parar serviços (mantém dados)
docker-compose stop

# Parar e remover containers (mantém volumes)
docker-compose down

# Remover tudo incluindo dados
docker-compose down -v
```


---

## 🛠️ Tecnologias Utilizadas

### Backend

| Tecnologia | Versão | Propósito |
|------------|---------|-----------|
| **Java** | 17 LTS | Linguagem principal |
| **Spring Boot** | 3.2.x | Framework principal |
| **Spring Security** | 6.x | Autenticação/Autorização (JWT) |
| **Spring Data JPA** | 3.x | ORM e persistência |
| **Hibernate** | 6.x | Implementação JPA |
| **PostgreSQL** | 16 | Banco de dados principal |
| **Redis** | 7.x | Cache em memória |
| **Flyway** | 9.x | Migração de banco de dados |
| **Lombok** | 1.18.x | Redução de código boilerplate |
| **MapStruct** | 1.5.x | Mapeamento de objetos/DTOs |
| **JUnit 5** | 5.10.x | Framework de testes |
| **Mockito** | 5.x | Mocking para testes |
| **Testcontainers** | 1.19.x | Testes de integração |
| **SpringDoc OpenAPI** | 2.x | Documentação da API (Swagger) |

### Frontend

| Tecnologia | Versão | Propósito |
|------------|---------|-----------|
| **React** | 18.x | Framework UI |
| **TypeScript** | 5.x | Tipagem estática |
| **Vite** | 5.x | Build tool e dev server |
| **React Router** | 6.x | Roteamento |
| **React Query** | 5.x | Gerenciamento de dados remotos |
| **Zustand** | 4.x | Gerenciamento de estado |
| **Axios** | 1.x | Cliente HTTP |
| **React Hook Form** | 7.x | Gerenciamento de formulários |
| **Zod** | 3.x | Validação de schemas |
| **TailwindCSS** | 3.x | Framework CSS |
| **Shadcn/ui** | Latest | Biblioteca de componentes |
| **date-fns** | 3.x | Manipulação de datas |

### DevOps & Infraestrutura

| Tecnologia | Propósito |
|------------|-----------|
| **Docker** | Containerização |
| **Docker Compose** | Orquestração de containers |
| **Nginx** | Servidor web e proxy reverso |
| **Ollama** | Hospedagem local de LLM |

---

## 📄 Licença

Este projeto é um projeto de estudo e demonstração.
