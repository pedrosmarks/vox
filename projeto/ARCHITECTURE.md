# VOX — Estrutura e Arquitetura

Plataforma municipal de participação cidadã. O ecossistema é composto por **três aplicações**:

| Aplicação | Tecnologia | Local |
|---|---|---|
| Backend (API REST) | Java 21 + Spring Boot 4 | `backend/Vox` |
| Frontend Web | Angular 20 | `frontend/site/vox` |
| App Mobile | Flutter (Dart 3.11) | `frontend/app/app_vox` |

Serviço auxiliar: **LiveKit** (servidor de vídeo/áudio em tempo real para audiências públicas), em `livekit_1.13.5_windows_amd64`.

---

## 1. Backend — `backend/Vox`

### Stack
- **Java 21**, **Spring Boot 4.0.5** (Spring MVC, Security, JDBC, Mail, Validation, DevTools)
- **PostgreSQL** (driver 42.6.0) — acesso via **JDBC puro** (sem JPA/Hibernate), pool **HikariCP**
- **JWT** (jjwt 0.11.2) para autenticação
- **Cloudinary** (upload de imagens), **LiveKit Server SDK** (0.15.0), **spring-dotenv** (.env)
- **Lombok**, **springdoc-openapi** (Swagger UI)
- Build: **Maven**

### Arquitetura: Hexagonal (Ports & Adapters)
O código separa **contratos** (`port`) das **implementações** (`implementation`), com o domínio no centro. Isso mantém a lógica de negócio independente de framework e banco.

```
br.com.fai.Vox
├── VoxApplication.java            # entrypoint Spring Boot
│
├── domain/                        # modelo de domínio (POJOs) — o núcleo
│   ├── Event, Project, IssueReport, UserModel, ConferenceRoom, ...
│   ├── dto/                       # DTOs de entrada/saída
│   │   └── dashboard/             # DTOs específicos dos dashboards
│   └── enums/                     # todos os enums (sufixo *Enum)
│
├── port/                          # INTERFACES (contratos)
│   ├── service/<área>/            # contratos de serviço (regras de negócio)
│   └── dao/<área>/                # contratos de persistência
│
├── implementation/                # IMPLEMENTAÇÕES dos ports
│   ├── service/<área>/            # regras de negócio (@Service)
│   │   ├── authentication/ (jwt, helper)
│   │   ├── livekit/  drive/ (Cloudinary)  email/  tools/
│   │   └── ... (project, issuereport, event, dashboard, auditlog, ...)
│   └── dao/<área>/                # DAOs Postgres (JDBC) + connection manager
│
├── controller/                    # camada web (REST controllers) + GlobalExceptionHandler
│
└── configuration/                 # wiring e infraestrutura
    ├── AppConfiguration.java      # beans (DAOs, services externos, CORS, OpenAPI)
    ├── security/                  # BasicSecurityConfiguration, JwtSecurityConfiguration
    └── audit/                     # AuditLogFilter, AuditAsyncConfiguration, AuditRetentionJob
```

### Fluxo de uma requisição
```
HTTP → Controller → Port(Service) ← ServiceImpl → Port(DAO) ← PostgresDaoImpl → PostgreSQL
                         ↑                                ↑
              AuthenticatedUserHelper            Connection (HikariCP)
```
- Filtros na cadeia: CORS → **JwtRequestFilter** (autenticação por token) → **AuditLogFilter** (registra mutações) → autorização.
- `GlobalExceptionHandler` traduz exceções em respostas HTTP (400/403/500).

### Perfis (Spring Profiles)
- **`jwt`** (ativo): autenticação stateless por Bearer token.
- **`basic`**: autenticação básica (alternativa).
- Cada perfil tem seu `application-<perfil>.properties` e sua classe de Security.

### Domínios funcionais (áreas)
Projetos comunitários · Ocorrências (issues) · Eventos · Moderação · Categorias · Vereadores ·
Assinaturas/apoios · Opiniões/votos · Notificações · Audiências públicas (salas LiveKit) ·
Usuários & configurações · Dashboard administrativo · Auditoria (logs) · Municípios.

### Banco de dados
- Scripts em `src/main/resources/db-scripts/`: `create-tables-postgres.sql` (schema + tipos ENUM),
  `insert-data-postgres-jwt.sql` / `insert-data-postgres-basic.sql` (seeds).
- Enums do domínio espelham tipos `ENUM` do Postgres (conversão via `CAST(? AS tipo)` + `.name()`/`valueOf()`).
- Auditoria: tabela `audit_log` com retenção de 90 dias (job agendado).

### Documentação da API
- `API.md` (na raiz do backend) + Swagger UI em `/swagger-ui/index.html`.

---

## 2. Frontend Web — `frontend/site/vox`

### Stack
- **Angular 20** (standalone components, sem NgModules)
- **RxJS 7.8**, **TypeScript 5.9**, **SCSS**
- **MapLibre GL** (mapas — zonas quentes, seleção de local)
- **livekit-client** (participação em audiências por vídeo)
- Testes: **Karma + Jasmine**

### Estrutura
```
src/
├── main.ts, index.html, styles.scss   # bootstrap + tema global (cores da marca)
└── app/
    ├── app.ts / app.html / app.scss    # shell raiz
    ├── app.config.ts                   # providers (router, http, interceptor)
    ├── app.routes.ts                   # rotas
    ├── auth.interceptor.ts             # injeta o Bearer token nas requisições
    ├── role.guard.ts                   # protege rotas por papel (ex.: dashboard/logs = ADMIN)
    ├── components/                      # reutilizáveis
    │   ├── navbar/
    │   └── map-picker/                 # seleção de lat/long no mapa
    ├── pages/                           # telas (1 pasta por rota)
    │   ├── login, cadastro, recuperar-senha
    │   ├── projetos, projeto-detalhe, sugestoes
    │   ├── problemas, problema-detalhe, relatar-problema
    │   ├── moderacao (+ *-detalhe-moderacao de projeto e problema)
    │   ├── audiencia, audiencia-sala (+ livekit-video.directive)
    │   ├── comunidade, perfil, configuracoes, usuarios
    │   └── dashboard, logs                # área administrativa
    ├── services/                         # comunicação com a API (HttpClient)
    │   ├── auth, project, issue, sala, dashboard
    │   ├── notification, subscription, settings, municipality
    └── utils/
        └── status-labels.ts             # rótulos amigáveis dos enums de status
```

### Padrões
- **Componentes standalone** por página, roteados em `app.routes.ts`.
- **Services** isolam o acesso HTTP; o `auth.interceptor` adiciona o token automaticamente.
- **role.guard** faz controle de acesso no cliente (o backend valida de verdade).
- Tema e paleta da marca centralizados em `styles.scss` (azul `#1b3f8b`, amarelo `#f5a800`, azul claro `#2d6fcc`).

---

## 3. App Mobile — `frontend/app/app_vox`

### Stack
- **Flutter** (Dart SDK 3.11), Material Design
- **http** + **shared_preferences** (API e sessão local)
- **flutter_map** + **latlong2** + **geolocator** (mapas e localização)
- **livekit_client** (audiências), **image_picker** (fotos)
- **pdf** + **printing** + **share_plus** (geração/compartilhamento de relatórios)
- **google_fonts**, **intl** (i18n), **permission_handler**

### Estrutura (Flutter padrão)
```
app_vox/
├── lib/                # código Dart (telas, serviços, widgets)
├── assets/             # imagens (logo VOX etc.)
├── android/ ios/ web/ windows/ macos/ linux/   # plataformas
└── pubspec.yaml        # dependências + ícone do app (flutter_launcher_icons)
```

O app consome a **mesma API REST** do backend e participa das audiências via LiveKit, espelhando as funcionalidades do frontend web para dispositivos móveis.

---

## Visão integrada

```
┌─────────────┐     ┌─────────────┐     ┌───────────────────────┐
│ Web (Angular)│    │ Mobile (Flutter)│  │  LiveKit (vídeo/áudio) │
└──────┬──────┘     └──────┬──────┘     └───────────▲───────────┘
       │  HTTPS + JWT      │  HTTPS + JWT            │ token/salas
       └─────────┬─────────┘                        │
                 ▼                                   │
        ┌───────────────────────────────────────────┴───┐
        │  Backend Spring Boot (Ports & Adapters)         │
        │  Controller → Service(port/impl) → DAO(port/impl)│
        └───────────────────────┬────────────────────────┘
                                 ▼
                          ┌────────────┐   Cloudinary (imagens)
                          │ PostgreSQL │   SMTP (e-mail)
                          └────────────┘
```
