# VOX — Mudança da API para o Frontend

Base URL: `http://localhost:8080` · Autenticação: `Authorization: Bearer <token>`.

---

## Projetos — novo campo `nature` (Lei × Obra)

Todo projeto agora tem uma **natureza obrigatória**, refletindo a distinção da administração pública:

- `PUBLIC_WORK` — Obra pública (construção, reforma, praça). **Exige `latitude` e `longitude`**. Aparece no mapa do dashboard.
- `LAW` — Projeto de lei (legislação, orçamento, nome de rua). **Não tem local físico**: `latitude`, `longitude`, endereço e custos são opcionais e o projeto **não** aparece no mapa.

### Ao criar/editar projeto (`POST` / `PUT /api/project`, `multipart/form-data`)
- Enviar o campo **`nature`** = `LAW` ou `PUBLIC_WORK` (**obrigatório**).
- `latitude`/`longitude` deixaram de ser sempre obrigatórios:
  - `PUBLIC_WORK`: **obrigatórios** — se faltarem, a API retorna `400`.
  - `LAW`: podem ser omitidos.
- Sugestão de UX: exibir um informativo explicando a diferença entre Lei e Obra ao escolher a natureza.

**Exemplo (form fields) — Obra pública:**
```
nature=PUBLIC_WORK
categoryId=1
title=Revitalização da Praça Central
description=...
latitude=-22.25230000
longitude=-45.70280000
```

**Exemplo (form fields) — Projeto de lei:**
```
nature=LAW
categoryId=2
title=Lei de Incentivo à Saúde Preventiva
description=...
(latitude/longitude/endereço não são necessários)
```

### No retorno de projeto (`GET /api/project`, `GET /api/project/{id}`)
- O objeto passa a incluir `"nature": "PUBLIC_WORK" | "LAW"`.
- Projetos `LAW` podem vir com `latitude`, `longitude`, `neighborhood`, `street`, `number` nulos — a UI deve tratar esses campos como opcionais conforme a natureza.

### Dashboard / mapa
- O mapa de zonas quentes (`GET /api/admin/dashboard/mapa/coordenadas`) considera apenas registros **com coordenada**. Projetos `LAW` (sem local) não aparecem no mapa — comportamento esperado.

---

### Observações
- Os valores do enum são enviados/recebidos em maiúsculas: `PUBLIC_WORK`, `LAW`.
- Erros seguem o formato `{ "status": <code>, "message": "<texto>" }`.
