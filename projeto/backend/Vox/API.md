# VOX — Documentação da API

**Base URL:** `http://localhost:8080`  
**Autenticação:** JWT Bearer Token (exceto `/authenticate` e `/api/auth/forgot-password`, `/api/auth/reset-password`)

> Adicione o header `Authorization: Bearer <token>` em todas as requisições autenticadas.

---

## Sumário

- [Autenticação](#autenticação)
- [Usuários](#usuários)
- [Municípios](#municípios)
- [Categorias](#categorias)
- [Vereadores](#vereadores)
- [Projetos](#projetos)
- [Imagens de Projetos](#imagens-de-projetos)
- [Ocorrências](#ocorrências)
- [Moderação](#moderação)
- [Notificações](#notificações)
- [Assinaturas](#assinaturas)
- [Salas de Conferência (LiveKit)](#salas-de-conferência-livekit)

---

## Autenticação

### Login
```
POST /authenticate
```
**Body:**
```json
{
  "email": "usuario@email.com",
  "password": "senha123"
}
```
**Resposta `200`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

### Dados do usuário autenticado
```
GET /api/auth/me
Authorization: Bearer <token>
```
**Resposta `200`:**
```json
{
  "id": 1,
  "name": "Maria Silva",
  "email": "maria@email.com",
  "cpf": "000.000.000-00",
  "phone": "(11) 99999-9999",
  "birthDate": "1990-05-20",
  "role": "MODERATOR",
  "municipalityId": 1,
  "acceptedTerms": true,
  "acceptedPrivacyPolicy": true
}
```

---

### Esqueci minha senha
```
POST /api/auth/forgot-password
```
**Body:**
```json
{
  "email": "usuario@email.com"
}
```
**Resposta `200`** — token de reset enviado (por e-mail, quando implementado).

---

### Redefinir senha
```
POST /api/auth/reset-password
```
**Body:**
```json
{
  "token": "uuid-do-token-de-reset",
  "newPassword": "novaSenha123"
}
```
**Resposta `200`**

---

## Usuários

> 🔒 Todos os endpoints de `/api/user` requerem role `ADMINISTRATOR`.

### Listar todos os usuários
```
GET /api/user
Authorization: Bearer <token>
```
**Resposta `200`:** array de `UserModel`

---

### Buscar usuário por ID
```
GET /api/user/{id}
Authorization: Bearer <token>
```
**Resposta `200`:**
```json
{
  "id": 1,
  "name": "João Silva",
  "email": "joao@email.com",
  "cpf": "111.222.333-44",
  "phone": "(11) 98888-7777",
  "role": "CITIZEN",
  "municipalityId": 1,
  "birthDate": "1985-03-10"
}
```

---

### Buscar usuário por e-mail
```
GET /api/user/email/{email}
Authorization: Bearer <token>
```

---

### Buscar usuários por papel (role)
```
GET /api/user/role/{role}
Authorization: Bearer <token>
```
**Valores de `role`:** `CITIZEN`, `COUNCILOR`, `MODERATOR`, `ADMINISTRATOR`

---

### Criar usuário
```
POST /api/user
Authorization: Bearer <token>
Content-Type: application/json
```
**Body:**
```json
{
  "name": "Ana Costa",
  "email": "ana@email.com",
  "cpf": "555.666.777-88",
  "phone": "(21) 97777-6666",
  "password": "senha123",
  "birthDate": "1995-08-15",
  "role": "CITIZEN",
  "municipalityId": 1,
  "acceptedTerms": true,
  "acceptedPrivacyPolicy": true
}
```
**Resposta `201`** com `Location: /api/user/{id}`

---

### Atualizar usuário
```
PUT /api/user/{id}
Authorization: Bearer <token>
Content-Type: application/json
```
**Body:** mesmo formato da criação  
**Resposta `204`**

---

### Atualizar senha
```
PUT /api/user/update-password
Authorization: Bearer <token>
Content-Type: application/json
```
**Body:**
```json
{
  "id": 1,
  "oldPassword": "senhaAtual",
  "newPassword": "novaSenha123"
}
```
**Resposta `200`**

---

### Deletar usuário
```
DELETE /api/user/{id}
Authorization: Bearer <token>
```
**Resposta `204`**

---

## Municípios

### Criar município
```
POST /api/municipality
Authorization: Bearer <token>
Content-Type: application/json
```
**Body:**
```json
{
  "name": "São Paulo",
  "state": "SP"
}
```
**Resposta `201`** com `Location: /api/municipality/{id}`

---

### Listar municípios
```
GET /api/municipality
Authorization: Bearer <token>
```
**Resposta `200`:**
```json
[
  { "id": 1, "name": "São Paulo", "state": "SP" }
]
```

---

### Buscar município por ID
```
GET /api/municipality/{id}
Authorization: Bearer <token>
```

---

## Categorias

### Listar categorias
```
GET /api/categories
Authorization: Bearer <token>
```
**Resposta `200`:**
```json
[
  { "id": 1, "name": "Infraestrutura", "description": "Obras e vias" }
]
```

---

### Buscar categoria por ID
```
GET /api/categories/{id}
Authorization: Bearer <token>
```

---

### Criar categoria
```
POST /api/categories
Authorization: Bearer <token>
Content-Type: application/json
```
**Body:**
```json
{
  "name": "Saúde",
  "description": "Serviços de saúde pública"
}
```
**Resposta `201`**

---

### Atualizar categoria
```
PUT /api/categories/{id}
Authorization: Bearer <token>
Content-Type: application/json
```
**Body:** mesmo formato da criação  
**Resposta `204`**

---

### Deletar categoria
```
DELETE /api/categories/{id}
Authorization: Bearer <token>
```
**Resposta `204`**

---

## Vereadores

### Listar vereadores
```
GET /api/users/councilors
Authorization: Bearer <token>
```
**Resposta `200`:** array de `UserModel` com `role: "COUNCILOR"`

---

### Buscar vereador por ID
```
GET /api/users/councilors/{id}
Authorization: Bearer <token>
```

---

## Projetos

### Listar projetos do município
```
GET /api/project
Authorization: Bearer <token>
```
**Query params opcionais:**
- `page` (número da página, base 0)
- `size` (itens por página, padrão `10`)

**Exemplos:**
```
GET /api/project
GET /api/project?page=0&size=10
```

**Resposta sem paginação `200`:** array de `Project`  
**Resposta com paginação `200`:**
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 42
}
```

---

### Meus projetos
```
GET /api/project/my
Authorization: Bearer <token>
```

---

### Buscar projeto por ID
```
GET /api/project/{id}
Authorization: Bearer <token>
```

---

### Criar projeto
```
POST /api/project
Authorization: Bearer <token>
Content-Type: multipart/form-data
```
**Form fields:**
| Campo | Tipo | Obrigatório |
|---|---|---|
| `categoryId` | number | ✅ |
| `type` | `CITIZEN` \| `CHAMBER` | ✅ |
| `title` | string | ✅ |
| `description` | string | ✅ |
| `neighborhood` | string | ❌ |
| `street` | string | ❌ |
| `number` | string | ❌ |
| `latitude` | decimal | ❌ |
| `longitude` | decimal | ❌ |
| `startDate` | `YYYY-MM-DD` | ❌ |
| `expectedEndDate` | `YYYY-MM-DD` | ❌ |
| `estimatedCost` | decimal | ❌ |
| `file` | imagem | ❌ |

**Resposta `201`**

---

### Atualizar projeto
```
PUT /api/project/{id}
Authorization: Bearer <token>
Content-Type: application/json
```
**Body:** objeto `Project` completo  
**Resposta `204`**

---

### Deletar projeto
```
DELETE /api/project/{id}
Authorization: Bearer <token>
```
**Resposta `204`**

---

### Histórico de status do projeto
```
GET /api/project/{id}/history
Authorization: Bearer <token>
```

---

### Imagens do projeto (via ProjectRestController)

**Adicionar imagem:**
```
POST /api/project/{id}/image
Authorization: Bearer <token>
Content-Type: multipart/form-data
```
Form field: `file` (imagem)

**Listar imagens:**
```
GET /api/project/{id}/image
Authorization: Bearer <token>
```

**Deletar imagem:**
```
DELETE /api/project/{id}/image/{imageId}
Authorization: Bearer <token>
```

---

### Opiniões do projeto

**Registrar opinião:**
```
POST /api/project/{id}/opinion
Authorization: Bearer <token>
Content-Type: application/json
```
**Body:**
```json
{
  "opinion": "APPROVE"
}
```
Valores: `APPROVE`, `DISAPPROVE`, `NEUTRAL`

**Listar opiniões:**
```
GET /api/project/{id}/opinion
Authorization: Bearer <token>
```

**Contagem por tipo:**
```
GET /api/project/{id}/opinion/stats
Authorization: Bearer <token>
```
**Resposta:**
```json
{ "APPROVE": 15, "DISAPPROVE": 3, "NEUTRAL": 2 }
```

**Minha opinião:**
```
GET /api/project/{id}/opinion/me
Authorization: Bearer <token>
```

---

### Vereadores vinculados ao projeto

**Vincular vereador:**
```
POST /api/project/{projectId}/councilor/{councilorId}
Authorization: Bearer <token>
```

**Desvincular vereador:**
```
DELETE /api/project/{projectId}/councilor/{councilorId}
Authorization: Bearer <token>
```

**Listar vereadores do projeto:**
```
GET /api/project/{projectId}/councilor
Authorization: Bearer <token>
```

---

## Imagens de Projetos

> Endpoint alternativo ao sub-recurso em `/api/project/{id}/image`.

### Listar todas as imagens
```
GET /api/project-image
Authorization: Bearer <token>
```

### Buscar imagem por ID
```
GET /api/project-image/{id}
Authorization: Bearer <token>
```

### Buscar imagens por projeto
```
GET /api/project-image/project-id/{projectId}
Authorization: Bearer <token>
```

### Fazer upload de imagem
```
POST /api/project-image
Authorization: Bearer <token>
Content-Type: multipart/form-data
```
Form fields: `projectId` (number), `file` (imagem)

### Atualizar imagem
```
PUT /api/project-image/{id}
Authorization: Bearer <token>
Content-Type: multipart/form-data
```
Form field: `file` (nova imagem)

### Deletar imagem
```
DELETE /api/project-image/{id}
Authorization: Bearer <token>
```

---

## Ocorrências

### Criar ocorrência
```
POST /api/issues
Authorization: Bearer <token>
Content-Type: multipart/form-data
```
**Form fields:**
| Campo | Tipo | Obrigatório |
|---|---|---|
| `councilorId` | number | ✅ |
| `title` | string | ✅ |
| `description` | string | ✅ |
| `neighborhood` | string | ❌ |
| `street` | string | ❌ |
| `number` | string | ❌ |
| `latitude` | decimal | ❌ |
| `longitude` | decimal | ❌ |
| `file` | imagem | ❌ |

**Resposta `201`**

---

### Listar ocorrências do município
```
GET /api/issues
Authorization: Bearer <token>
```
**Query params:** `page`, `size`

---

### Minhas ocorrências
```
GET /api/issues/my
Authorization: Bearer <token>
```

---

### Buscar ocorrência por ID
```
GET /api/issues/{id}
Authorization: Bearer <token>
```

---

### Atualizar ocorrência
```
PUT /api/issues/{id}
Authorization: Bearer <token>
Content-Type: application/json
```
**Body:** objeto `IssueReport`  
**Resposta `204`**

---

### Deletar ocorrência
```
DELETE /api/issues/{id}
Authorization: Bearer <token>
```
**Resposta `204`**

---

### Histórico de status da ocorrência
```
GET /api/issues/{id}/history
Authorization: Bearer <token>
```

---

### Imagens da ocorrência

**Adicionar imagem:**
```
POST /api/issues/{id}/images
Authorization: Bearer <token>
Content-Type: multipart/form-data
```
Form field: `file`

**Listar imagens:**
```
GET /api/issues/{id}/images
Authorization: Bearer <token>
```

**Deletar imagem:**
```
DELETE /api/issues/{id}/images/{imageId}
Authorization: Bearer <token>
```

---

## Moderação

> 🔒 Requer role `ADMINISTRATOR` ou `MODERATOR`.

### Projetos pendentes de aprovação
```
GET /api/moderation/projects/pending
Authorization: Bearer <token>
```
**Query params:** `page`, `size`

---

### Aprovar projeto
```
POST /api/moderation/projects/{id}/approve
Authorization: Bearer <token>
Content-Type: application/json
```
**Body (opcional):**
```json
{
  "feedback": "Projeto aprovado conforme critérios."
}
```
**Resposta `200`**

---

### Rejeitar projeto
```
POST /api/moderation/projects/{id}/reject
Authorization: Bearer <token>
Content-Type: application/json
```
**Body (opcional):**
```json
{
  "feedback": "Projeto fora dos critérios estabelecidos."
}
```
**Resposta `200`**

---

### Atualizar status do projeto
```
PATCH /api/moderation/projects/{id}/status
Authorization: Bearer <token>
Content-Type: application/json
```
**Body:**
```json
{
  "status": "IN_EXECUTION",
  "note": "Obras iniciadas em 01/09/2026."
}
```
**Status disponíveis:** `PENDING_APPROVAL`, `REJECTED`, `PUBLISHED`, `IN_VOTING`, `SELECTED_BY_COUNCIL`, `APPROVED_BY_COUNCIL`, `IN_EXECUTION`, `COMPLETED`, `ARCHIVED`, `CANCELLED`  
**Resposta `204`**

---

### Ocorrências pendentes de aprovação
```
GET /api/moderation/issues/pending
Authorization: Bearer <token>
```
**Query params:** `page`, `size`

---

### Aprovar ocorrência
```
POST /api/moderation/issues/{id}/approve
Authorization: Bearer <token>
Content-Type: application/json
```
**Body (opcional):**
```json
{
  "feedback": "Ocorrência verificada e aprovada."
}
```

---

### Rejeitar ocorrência
```
POST /api/moderation/issues/{id}/reject
Authorization: Bearer <token>
Content-Type: application/json
```
**Body (opcional):**
```json
{
  "feedback": "Ocorrência duplicada."
}
```

---

### Atualizar status da ocorrência
```
PATCH /api/moderation/issues/{id}/status
Authorization: Bearer <token>
Content-Type: application/json
```
**Body:**
```json
{
  "status": "IN_PROGRESS",
  "note": "Equipe de campo acionada."
}
```
**Status disponíveis:** `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`  
**Resposta `204`**

---

## Notificações

### Listar todas as notificações do usuário
```
GET /api/notifications
Authorization: Bearer <token>
```
**Resposta `200`:** array de `Notification`

---

### Listar notificações não lidas
```
GET /api/notifications/unread
Authorization: Bearer <token>
```

---

### Contagem de não lidas
```
GET /api/notifications/count
Authorization: Bearer <token>
```
**Resposta `200`:**
```json
{ "count": 5 }
```

---

### Marcar uma notificação como lida
```
PATCH /api/notifications/{id}/read
Authorization: Bearer <token>
```
**Resposta `204`**

---

### Marcar todas como lidas
```
PATCH /api/notifications/read-all
Authorization: Bearer <token>
```
**Resposta `204`**

---

## Assinaturas

> Assinaturas permitem que o usuário receba notificações sobre recursos específicos.

### Listar minhas assinaturas
```
GET /api/subscriptions
Authorization: Bearer <token>
```

---

### Assinar / Cancelar todos os projetos
```
POST   /api/subscriptions/all-projects
DELETE /api/subscriptions/all-projects
Authorization: Bearer <token>
```

### Assinar / Cancelar todas as ocorrências
```
POST   /api/subscriptions/all-issues
DELETE /api/subscriptions/all-issues
Authorization: Bearer <token>
```

### Assinar / Cancelar projeto específico
```
POST   /api/subscriptions/projects/{projectId}
DELETE /api/subscriptions/projects/{projectId}
Authorization: Bearer <token>
```

### Assinar / Cancelar ocorrência específica
```
POST   /api/subscriptions/issues/{issueId}
DELETE /api/subscriptions/issues/{issueId}
Authorization: Bearer <token>
```

### Assinar / Cancelar categoria
```
POST   /api/subscriptions/categories/{categoryId}
DELETE /api/subscriptions/categories/{categoryId}
Authorization: Bearer <token>
```

### Assinar / Cancelar vereador
```
POST   /api/subscriptions/councilors/{councilorId}
DELETE /api/subscriptions/councilors/{councilorId}
Authorization: Bearer <token>
```

---

## Salas de Conferência (LiveKit)

> Audiências públicas e sessões de conferência com controle de permissões via LiveKit.

### Fluxo completo

```
1. Moderador cria sala
2. Cidadão solicita entrada
3. Moderador lista e aprova solicitação
4. Cidadão obtém token LiveKit → conecta ao LiveKit
5. Moderador obtém token LiveKit → conecta ao LiveKit
6. Moderador controla microfone/câmera conforme necessário
7. Moderador encerra a sala
```

---

### Criar sala
> 🔒 Requer role `MODERATOR` ou `ADMINISTRATOR`

```
POST /api/salas
Authorization: Bearer <token>
Content-Type: application/json
```
**Body:**
```json
{
  "name": "Audiência Pública 001",
  "description": "Sessão ordinária da câmara municipal"
}
```
**Resposta `201`** com `Location: /api/salas/{id}`

---

### Listar salas do município
```
GET /api/salas
Authorization: Bearer <token>
```
**Resposta `200`:**
```json
[
  {
    "id": 1,
    "name": "Audiência Pública 001",
    "description": "Sessão ordinária",
    "moderatorId": 5,
    "municipalityId": 1,
    "status": "OPEN",
    "createdAt": "2026-08-15T10:00:00"
  }
]
```

---

### Buscar sala por ID
```
GET /api/salas/{id}
Authorization: Bearer <token>
```

---

### Encerrar sala
> 🔒 Apenas o moderador da sala ou `ADMINISTRATOR`

```
DELETE /api/salas/{id}
Authorization: Bearer <token>
```
**Resposta `204`**

---

### Solicitar entrada na sala
> Qualquer usuário autenticado (tipicamente um cidadão)

```
POST /api/salas/{id}/solicitacoes-entrada
Authorization: Bearer <token>
```
**Resposta `200`**

---

### Listar solicitações de entrada
> 🔒 Requer role `MODERATOR` ou `ADMINISTRATOR`

```
GET /api/salas/{id}/solicitacoes-entrada
Authorization: Bearer <token>
```
**Resposta `200`:**
```json
[
  {
    "id": 10,
    "roomId": 1,
    "userId": 7,
    "status": "PENDING",
    "canPublishAudio": false,
    "canPublishVideo": false,
    "requestedAt": "2026-08-15T10:05:00"
  }
]
```
**Status possíveis:** `PENDING`, `APPROVED`, `REJECTED`, `REMOVED`

---

### Aprovar solicitação de entrada
> 🔒 Requer role `MODERATOR` ou `ADMINISTRATOR`

```
POST /api/salas/{id}/solicitacoes-entrada/{participanteId}/aprovar
Authorization: Bearer <token>
```
> `participanteId` é o **userId** do cidadão (não o id da solicitação).

**Resposta `200`**

---

### Rejeitar solicitação de entrada
> 🔒 Requer role `MODERATOR` ou `ADMINISTRATOR`

```
POST /api/salas/{id}/solicitacoes-entrada/{participanteId}/rejeitar
Authorization: Bearer <token>
```
**Resposta `200`**

---

### Liberar microfone
> 🔒 Requer role `MODERATOR` ou `ADMINISTRATOR`  
> Aplica a permissão imediatamente no LiveKit se o participante estiver conectado.

```
POST /api/salas/{id}/participantes/{participanteId}/microfone/liberar
Authorization: Bearer <token>
```
**Resposta `200`**

---

### Bloquear microfone
> 🔒 Requer role `MODERATOR` ou `ADMINISTRATOR`

```
POST /api/salas/{id}/participantes/{participanteId}/microfone/bloquear
Authorization: Bearer <token>
```
**Resposta `200`**

---

### Liberar câmera
> 🔒 Requer role `MODERATOR` ou `ADMINISTRATOR`  
> Microfone e câmera são permissões independentes.

```
POST /api/salas/{id}/participantes/{participanteId}/camera/liberar
Authorization: Bearer <token>
```
**Resposta `200`**

---

### Bloquear câmera
> 🔒 Requer role `MODERATOR` ou `ADMINISTRATOR`

```
POST /api/salas/{id}/participantes/{participanteId}/camera/bloquear
Authorization: Bearer <token>
```
**Resposta `200`**

---

### Expulsar participante
> 🔒 Requer role `MODERATOR` ou `ADMINISTRATOR`  
> Remove o participante do banco e desconecta do LiveKit.

```
DELETE /api/salas/{id}/participantes/{participanteId}
Authorization: Bearer <token>
```
**Resposta `204`**

---

### Gerar token LiveKit
> Cidadão: deve estar com status `APPROVED` para receber token.  
> Moderador: recebe token com permissões administrativas completas.  
> O token gerado é usado pelo frontend para conectar diretamente ao LiveKit.

```
POST /api/salas/{id}/token
Authorization: Bearer <token>
```
**Resposta `200`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE..."
}
```

**Permissões no token por papel:**

| Permissão | MODERATOR / ADMIN | CITIZEN aprovado |
|---|---|---|
| Entrar na sala | ✅ | ✅ |
| Assinar tracks (ouvir/ver) | ✅ | ✅ |
| Publicar áudio | ✅ | Somente se liberado |
| Publicar vídeo | ✅ | Somente se liberado |
| Publicar dados | ✅ | ❌ |
| Administrar sala | ✅ | ❌ |

---

## Códigos de Resposta

| Código | Significado |
|---|---|
| `200` | OK |
| `201` | Criado com sucesso |
| `204` | Sem conteúdo (operação realizada) |
| `400` | Requisição inválida (ver campo `message`) |
| `401` | Não autenticado |
| `403` | Sem permissão |
| `404` | Recurso não encontrado |
| `500` | Erro interno |

**Formato de erro:**
```json
{
  "status": 400,
  "message": "Descrição do erro"
}
```

---

## Roles do Sistema

| Role | Descrição |
|---|---|
| `CITIZEN` | Cidadão — acesso básico |
| `COUNCILOR` | Vereador |
| `MODERATOR` | Moderador — gerencia salas e conteúdo |
| `ADMINISTRATOR` | Administrador — acesso total |
