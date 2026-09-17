# Contexto do Projeto VOX — Para continuação por outra IA

## Estrutura do Projeto

```
frontend/
  site/vox/          → Angular 20, standalone components
  app/app_vox/       → Flutter (Android/iOS)
  API.md             → Documentação da API (atenção: tem imprecisões — ver abaixo)
```

**Backend:** Spring Boot, `http://localhost:8080`  
**LiveKit:** `ws://localhost:7880`  
**App Flutter:** aponta para `http://192.168.1.105:8080` (via `api_client.dart`)

---

## O que foi implementado nesta sessão

### 1. CRUD administrativo de usuários (site + app)
- Endpoints corretos: `GET /api/user/role/{role}`, `POST /api/user`, `PUT /api/user/{id}`, `DELETE /api/user/{id}`
- 3 abas: Vereadores (COUNCILOR), Moderadores (MODERATOR), Administradores (ADMINISTRATOR)
- Arquivos: `auth.service.ts`, `usuarios.component.ts/.html`, `usuarios_screen.dart`

### 2. Atualizar status de projeto + comentário de rejeição (site + app)
- Moderador tem botão "Atualizar Status" no detalhe do projeto
- Rejeição exige comentário obrigatório
- **IMPORTANTE:** usar sempre `PATCH /api/moderation/projects/{id}/status` com `note` — o `POST /reject` aceita feedback mas NÃO persiste no history
- Comentário fica em `GET /api/project/{id}/history`, campo `note` da entrada com `newStatus=REJECTED`
- `latestRejectionNote(history)` — função utilitária em `project.service.ts`
- Cidadão vê o comentário em "Minhas Sugestões" (`sugestoes.component.ts`) e no detalhe do projeto
- **BUG PENDENTE DE BACKEND:** `PATCH /api/moderation/projects/{id}/status` dá erro CORS no site (o `Access-Control-Allow-Methods` do backend não inclui `PATCH`). No app funciona pois não passa por CORS.

### 3. Projetos rejeitados não aparecem na lista pública
- `isVisible()` agora esconde `REJECTED` e `CANCELLED`
- Mas aparecem em "Minhas Sugestões" do cidadão com o motivo

### 4. Modal de rejeição com comentário obrigatório
- Tela de moderação (aba Pendentes): botão Rejeitar abre modal antes de confirmar
- Arquivos: `moderacao.component.ts/.html/.scss`, `moderacao_screen.dart`

### 5. Modo alto contraste no site
- Arquivo: `styles.scss` — bloco `html[data-a11y='HIGH_CONTRAST']` totalmente reescrito
- Cobre modais, cards, navbar, abas, botões, campos de formulário
- Modais escuros em dark mode: `.status-modal`, `.reject-modal`

### 6. Botão "Apoiar" (opinion APPROVE) na lista de projetos
- `POST /api/project/{id}/opinion` body `{ opinion: "APPROVE" | "NEUTRAL" }`
- Stats: `GET /api/project/{id}/opinion/stats` → `{ approved, disapproved, neutral, total }` (minúsculo — API.md estava errado)
- `GET /api/project/{id}/opinion/me` → 404 se não opinou (não retorna null)
- Desapoiar = enviar NEUTRAL (sem DELETE)
- Filtro "Mais apoiados" ordena por `approved` desc
- Arquivos: `project.service.ts` (OpinionStats, MyOpinion, setOpinion, getOpinionStats, getMyOpinion), `projetos.component.ts/.html/.scss`, `projetos_screen.dart`, `project_service.dart`

### 7. Vídeo 1:1 (quadrado) na sala de audiência
- Site: `audiencia-sala.component.scss` — `.moderator-stage` com `aspect-ratio: 1/1`, `.citizen-tile` com `aspect-ratio: 1/1`
- App: `audiencia_sala_screen.dart` — moderador tile virou `AspectRatio(1)`, citizen tile com `width: 116` (quadrado pela altura da strip)
- `VideoTrackRenderer` com `fit: VideoViewFit.cover`

### 8. Correção de parse do wrapper `{value, Count}` (sala)
- O backend retorna arrays de sala assim: `{ "value": [...], "Count": n }` em vez de array direto
- Corrigido em `sala.service.ts` (site) com `unwrapArray()` e em `sala_service.dart` (app) com `_unwrapList()`
- Afeta: `getSalas()`, `getSolicitacoesEntrada()`, `getSolicitacoesFala()`
- Arquivos: `sala.service.ts`, `sala_service.dart`

### 9. Pedidos de fala — diagnóstico (PENDENTE)
- **BUG DE BACKEND:** `POST /api/salas/{id}/solicitacoes-fala` às vezes retorna 400 "Já existe uma solicitação de fala pendente" quando o usuário já tem um pedido PENDING — comportamento correto
- O campo que identifica pedido de fala é `speechRequestStatus` dentro de `SolicitacaoEntrada`
- `GET /solicitacoes-fala` retorna corretamente as entradas com `speechRequestStatus=PENDING` (confirmado na sala 4)
- **O problema real era o wrapper `{value, Count}`** — já corrigido no item 8
- `SolicitacaoEntrada` agora tem o campo `speechRequestStatus` em ambas as plataformas

### 10. Bug de entrada não aparecendo no app
- O `_buildApprovalSection()` ficava fora da tela (overflow) porque o palco quadrado + strip + controles ocupavam toda a altura
- Corrigido: `_buildConnectedLayout()` usa `Expanded > SingleChildScrollView` para a área de vídeo, e controles + aprovação ficam fixos no rodapé
- Arquivo: `audiencia_sala_screen.dart`

---

## Bugs de Backend conhecidos (passar pro time de backend)

### 1. CORS — PATCH não permitido
```
Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS,HEAD
```
Falta `PATCH`. Afeta: `PATCH /api/moderation/projects/{id}/status`  
**Fix:** adicionar `"PATCH"` na configuração de CORS do Spring

### 2. Wrapper `{value, Count}` inconsistente
Endpoints de sala retornam `{ "value": [...], "Count": n }` em vez de array direto.  
Outros endpoints retornam array direto. Inconsistência no padrão de resposta.

### 3. `POST /reject` não persiste feedback
`POST /api/moderation/projects/{id}/reject` com `{ "feedback": "..." }` retorna 200 mas não salva o comentário no histórico. Só o `PATCH /status` com `note` grava no history.

---

## Imprecisões do API.md

| Endpoint | O que o doc diz | O que retorna de verdade |
|---|---|---|
| `GET /api/project/{id}/opinion/stats` | `{ APPROVE, DISAPPROVE, NEUTRAL }` | `{ approved, disapproved, neutral, total }` |
| `GET /api/project/{id}/opinion/me` | retorna objeto ou null | 404 quando não opinou |
| Arrays de sala | array direto | `{ value: [...], Count: n }` |

---

## Arquivos mais relevantes modificados

### Site (Angular)
- `src/app/services/auth.service.ts` — tipos LogEntry, CreateUserPayload, UserRole, métodos CRUD usuário
- `src/app/services/project.service.ts` — ProjectHistoryEntry, latestRejectionNote, updateProjectStatus, setOpinion, getOpinionStats, getMyOpinion
- `src/app/services/sala.service.ts` — unwrapArray, getSolicitacoesFala (usa fallback via entrada), speechRequestStatus
- `src/app/pages/projeto-detalhe/` — botão status, modal, comentário de rejeição
- `src/app/pages/moderacao/` — modal de rejeição com comentário obrigatório
- `src/app/pages/sugestoes/` — exibe comentário de rejeição por projeto
- `src/app/pages/projetos/` — botão Apoiar, filtro Mais apoiados
- `src/app/pages/usuarios/` — 3 abas (vereador, moderador, admin)
- `src/app/pages/audiencia-sala/` — LiveKit real, vídeo 1:1
- `src/styles.scss` — dark mode e alto contraste completos

### App (Flutter)
- `lib/services/api_client.dart` — baseUrl: `http://192.168.1.105:8080`
- `lib/services/auth_service.dart` — login, CRUD usuários, logs
- `lib/services/project_service.dart` — getProjectHistory, getRejectionNote, setOpinion, getApprovalCount, getMyOpinion
- `lib/services/sala_service.dart` — _unwrapList, getSolicitacoesEntrada, getSolicitacoesFala
- `lib/models/project.dart` — ProjectHistoryEntry adicionado
- `lib/models/sala.dart` — speechRequestStatus adicionado ao SolicitacaoEntrada
- `lib/screens/projeto_detalhe_screen.dart` — botão status, comentário de rejeição, vídeo 1:1
- `lib/screens/moderacao_screen.dart` — _promptRejectComment antes de rejeitar
- `lib/screens/sugestoes_screen.dart` — exibe comentário de rejeição
- `lib/screens/projetos_screen.dart` — botão Apoiar, filtro Mais apoiados
- `lib/screens/audiencia_sala_screen.dart` — vídeo 1:1, _buildConnectedLayout com scroll

---

## O que estava PENDENTE quando a sessão travou

### Pendente 1 — flutter analyze do app (última ação interrompida)
Executar:
```
flutter analyze lib/services/sala_service.dart lib/models/sala.dart lib/screens/audiencia_sala_screen.dart
```
Na pasta: `c:\Users\pedro\Desktop\vox2\vox\projeto\frontend\app\app_vox`

### Pendente 2 — Reverter getSolicitacoesFala no site
O método `getSolicitacoesFala` em `sala.service.ts` foi trocado por um fallback que lê as entradas e filtra `speechRequestStatus=PENDING`. Mas agora descobrimos que o endpoint `/solicitacoes-fala` funciona corretamente (sala 4 confirmou). O fallback ainda funciona (filtra entradas com PENDING), mas seria melhor usar o endpoint direto, tratando o wrapper `{value, Count}`. Ambos os caminhos funcionam.

### Pendente 3 — Testar fluxo completo de fala no site
Após o fix do wrapper, o moderador deveria ver os pedidos de fala. Precisa testar:
1. Cidadão (joao@example.com / aa) entra na sala
2. Clica "Pedir para falar"
3. Moderador (maria@example.com / aa) deve ver na sidebar

### Pendente 4 — Implementação completa do LiveKit (solicitado mas NÃO iniciado)
O usuário pediu no início da sessão para implementar toda a lógica do LiveKit na sala de audiência (criar sala, aprovar/negar entrada, pedir para falar, etc.) tanto no site quanto no app. O site JÁ tem a implementação completa com LiveKit real. O app também tem. Mas a integração está com os bugs de backend listados acima.

---

## Contas de teste

| Email | Senha | Role |
|---|---|---|
| maria@example.com | aa | MODERATOR |
| joao@example.com | aa | CITIZEN (userId 1) |

## Salas existentes no banco
| ID | Nome | Status | ModeratorId |
|---|---|---|---|
| 1 | Audiência Pública - Orçamento 2026 | OPEN | 2 |
| 2 | Sessão sobre Mobilidade Urbana | OPEN | 2 |
| 4 | (criada nos testes) | OPEN | ? |
