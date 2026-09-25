# VOX — Mudanças da API para o Frontend

Resumo das alterações do backend que impactam o frontend (web e mobile).
Base URL: `http://localhost:8080` · Autenticação: `Authorization: Bearer <token>`.

---

## 1. Projetos — novo campo `nature` (Lei × Obra)

Todo projeto agora tem uma **natureza obrigatória**, refletindo a administração pública:

- `PUBLIC_WORK` — Obra pública (construção, reforma, praça). **Exige `latitude` e `longitude`**. Aparece no mapa do dashboard.
- `LAW` — Projeto de lei (legislação, orçamento). **Não tem local físico**: `latitude`, `longitude`, endereço e custos são opcionais. Não aparece no mapa.

**Ao criar/editar projeto (`POST`/`PUT /api/project`, multipart):**
- Enviar o campo `nature` = `LAW` ou `PUBLIC_WORK` (**obrigatório**).
- `latitude`/`longitude` deixaram de ser sempre obrigatórios:
  - `PUBLIC_WORK`: obrigatórios (se faltarem, a API retorna `400`).
  - `LAW`: podem ser omitidos.
- Sugestão de UX: mostrar um informativo explicando a diferença entre Lei e Obra ao escolher a natureza.

**No retorno de projeto (`GET`)**: passa a incluir `"nature": "PUBLIC_WORK" | "LAW"`. Projetos `LAW` podem vir com `latitude`/`longitude`/`neighborhood`/`street`/`number` nulos.

---

## 2. Eventos (módulo novo)

CRUD de eventos, **sem limitação de município** (qualquer usuário vê eventos de qualquer município).
Leitura pública; criar/editar/excluir apenas `MODERATOR` ou `ADMINISTRATOR`.

### Categorias de evento
- `GET /api/event-categories` — listar (público)
- `GET /api/event-categories/{id}`
- `POST` / `PUT /api/event-categories/{id}` / `DELETE /api/event-categories/{id}` (MODERATOR/ADMIN, JSON `{ name, description }`)

### Eventos
- `GET /api/events` — listagem pública paginada, com filtros (query params, todos opcionais):
  - `categoryId`, `municipalityId`, `search` (título/descrição)
  - `minPrice`, `maxPrice`, `free` (`true` = só gratuitos)
  - `hasImage` (`true` = só com imagem)
  - `startFrom`, `startTo` (ISO `yyyy-MM-dd'T'HH:mm:ss`) — filtra pela data de início
  - `page` (padrão 0), `size` (padrão 10, máx. 100)
  - Resposta: `PageResponse` → `{ content: Event[], page, size, totalElements }`
- `GET /api/events/{id}` — detalhe (inclui o array `images`)
- `POST /api/events` (multipart) — MODERATOR/ADMIN. Campos: `title`✅, `categoryId`✅, `description`, `price` (opcional; ausente = gratuito), `startDate`, `endDate` (ISO date-time), `location`, `file` (imagem inicial opcional).
- `PUT /api/events/{id}` (multipart) — idem; novo `file` é adicionado às imagens.
- `DELETE /api/events/{id}`
- `GET /api/events/{id}/images` (público)
- `POST /api/events/{id}/images` (multipart, campo `file`) — MODERATOR/ADMIN
- `DELETE /api/events/{id}/images/{imageId}` — MODERATOR/ADMIN

**Objeto Event:** `id, title, description, categoryId, price (nullable), startDate, endDate, location, municipalityId, authorId, createdAt, images[]`.

---

## 3. Opiniões de projeto — `DISAPPROVE` removido

Os votos/opiniões agora têm apenas **dois valores**: `APPROVE` e `NEUTRAL` (o `DISAPPROVE` foi removido).
- `POST /api/project/{id}/opinion` body: `{ "opinion": "APPROVE" | "NEUTRAL" }`
- `GET /api/project/{id}/opinion/stats` → `{ "APPROVE": n, "NEUTRAL": n }` (sem `DISAPPROVE`).

Ajustar qualquer UI que ainda mostre a opção "Desaprovar".

---

## 4. Vereador — desassociar/desvincular

- **Ocorrência:** `POST /api/issues/{id}/desassociar` — o vereador autenticado (role `COUNCILOR`) se desassocia de uma ocorrência que está atribuída **a ele mesmo**. `204` em sucesso; `400` se não estiver associada a ele.
- **Projeto:** `DELETE /api/project/{projectId}/councilor/me` — o vereador autenticado se desvincula do projeto. `204`; `403` se não for `COUNCILOR`.

(Continuam existindo os endpoints de associar: `POST /api/issues/{id}/associar` e o vínculo de vereador em projeto.)

---

## 5. Salas / Audiências — pedido de fala

- **Revogar fala aprovada:** `POST /api/salas/{id}/solicitacoes-fala/{participanteId}/revogar` (MODERATOR/ADMIN). Encerra a fala (status volta a `REJECTED`), bloqueia microfone e câmera e propaga ao LiveKit.
- **Reabrir pedido:** `POST /api/salas/{id}/solicitacoes-fala` agora permite pedir novamente mesmo após `APPROVED`/`REJECTED` (volta para `PENDING`). Só bloqueia (`400`) se já houver pedido `PENDING` em aberto.
- Bloquear microfone **e** câmera juntos também encerra a aprovação de fala.
- `{participanteId}` nesses endpoints é o **userId** do participante.

---

## 6. Dashboard administrativo (novo)

> 🔒 Apenas `ADMINISTRATOR`. Sempre escopado ao município do admin (token). Todos aceitam filtro de data opcional `?from=yyyy-MM-dd&to=yyyy-MM-dd`.

- `GET /api/admin/dashboard/overview` — KPIs (issues/projetos por status, usuários, taxas).
- `GET /api/admin/dashboard/mapa/coordenadas?precision=3` — heatmap por coordenada. **Só inclui registros com coordenada** (projetos `LAW` não aparecem).
- `GET /api/admin/dashboard/mapa/bairros` — zonas quentes por bairro.
- `GET /api/admin/dashboard/mapa/bairros/detalhe?bairro=Centro` — issues/projetos de um bairro.
- `GET /api/admin/dashboard/moderacao` — backlog, taxas de aprovação, tempo médio, ranking de moderadores.
- `GET /api/admin/dashboard/engajamento` — projetos mais assinados/opinados, distribuição de opiniões (`APPROVE`/`NEUTRAL`), issues mais seguidas, usuários mais ativos.
- `GET /api/admin/dashboard/categorias` — ranking por categoria e cruzamento categoria × status.
- `GET /api/admin/dashboard/series-temporais?granularidade=day|week|month` — volume de issues/projetos ao longo do tempo.
- `GET /api/admin/dashboard/projetos/ciclo-vida` — distribuição por status, tempo médio por etapa, execução orçamentária.

---

## 7. Logs de auditoria (novo)

- `GET /api/admin/logs` — 🔒 `ADMINISTRATOR`. Paginado. Filtros opcionais: `page`, `size`, `userId`, `method` (`POST|PUT|PATCH|DELETE`), `from`, `to` (ISO date).
- Resposta `PageResponse` de registros com: `userId, userRole, municipalityId, httpMethod, path, statusCode, success, durationMs, ipAddress, userAgent, errorMessage, createdAt`.
- O backend registra automaticamente as ações de escrita e o login; não há endpoint para o front disparar logs.

---

## 8. Perfil / Autenticação

- **`GET /api/auth/me`** agora inclui `profilePhotoUrl` (URL pública da foto; `null` se não houver). Use esse campo para exibir a foto do usuário logado.
- **Recuperação de senha:** `POST /api/auth/forgot-password` envia por e-mail **apenas o token** (não um link). A tela de redefinição deve pedir que o usuário informe o token recebido e a nova senha em `POST /api/auth/reset-password` (`{ token, newPassword }`).

---

## 9. Regras de negócio que afetam a UI

- **Limite semanal só para cidadão:** apenas usuários `CITIZEN` têm limite de **3 projetos** e **3 ocorrências por semana** (retorna `400` ao exceder). `COUNCILOR`/`MODERATOR`/`ADMINISTRATOR` não têm limite. A UI pode informar o limite só para cidadãos.
- **latitude/longitude obrigatórios em ocorrências:** ao criar ocorrência (`POST /api/issues`), `latitude` e `longitude` são **obrigatórios** (usados no mapa). Em projeto, depende da `nature` (ver seção 1).
- **Notificação automática de projeto:** quando o status de um projeto muda (via `PUT /api/project/{id}`), o backend notifica automaticamente autor + assinantes do projeto + assinantes de todos os projetos. O front não precisa disparar nada; só refletir as notificações em `GET /api/notifications`.

---

## Observações de contrato

- Erros seguem o formato `{ "status": <code>, "message": "<texto>" }`.
- Enums enviados/recebidos usam os nomes em maiúsculas (ex.: `PUBLIC_WORK`, `APPROVE`, `LAW`).
- Consulte o `backend/Vox/API.md` para o detalhamento completo de cada endpoint.
