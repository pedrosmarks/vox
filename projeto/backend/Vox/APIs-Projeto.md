# Documentação das APIs de Projeto

## Projeto (`/api/project`)


### 1. Listar todos os projetos
```
GET http://localhost:8080/api/project
```

### 2. Listar projetos por status (ex: pendentes de aprovação)
```
GET http://localhost:8080/api/project?status=PENDING_APPROVAL
```

### 3. Aprovar projeto
```
PUT http://localhost:8080/api/project/{id}/approve
```

### 4. Rejeitar projeto
```
PUT http://localhost:8080/api/project/{id}/reject
```

### 5. Buscar projeto por ID
```
GET http://localhost:8080/api/project/1
```
```json
{
  "id": 1,
  "title": "Construção de Escola",
  ...
}
```

### 6. Criar projeto
```
curl -X POST http://localhost:8080/api/project \
  -F "title=Nova Praça" \
  -F "description=Praça central" \
  -F "municipalityId=2" \
  -F "categoryId=1" \
  -F "file=@/caminho/para/imagem.jpg"
```
  - title, description, municipalityId, categoryId, type, status, authorId, createdAt, updatedAt, highlighted, isOfficial, neighborhood, street, number, latitude, longitude, startDate, expectedEndDate, endDate, financialAnalysis, estimatedCost, approvedBudget, file (opcional)
```
HTTP/1.1 201 Created
Location: /api/project/2
```

### 7. Atualizar projeto
```
PUT http://localhost:8080/api/project/1
Content-Type: application/json

{
  "title": "Nova Praça Atualizada",
  "description": "Descrição atualizada",
  "municipalityId": 2,
  "categoryId": 1
  // ...demais campos
}
```
```
HTTP/1.1 204 No Content
```

### 8. Remover projeto
```
DELETE http://localhost:8080/api/project/1
```
```
HTTP/1.1 204 No Content
```


### 2. Buscar projeto por ID
```
GET http://localhost:8080/api/project/1
```
```json
{
  "id": 1,
  "title": "Construção de Escola",
  ...
}
```


### 3. Criar projeto
```
curl -X POST http://localhost:8080/api/project \
  -F "title=Nova Praça" \
  -F "description=Praça central" \
  -F "municipalityId=2" \
  -F "categoryId=1" \
  -F "file=@/caminho/para/imagem.jpg"
```
  - title, description, municipalityId, categoryId, type, status, authorId, createdAt, updatedAt, highlighted, isOfficial, neighborhood, street, number, latitude, longitude, startDate, expectedEndDate, endDate, financialAnalysis, estimatedCost, approvedBudget, file (opcional)
```
HTTP/1.1 201 Created
Location: /api/project/2
```


### 4. Atualizar projeto
```
PUT http://localhost:8080/api/project/1
Content-Type: application/json

{
  "title": "Nova Praça Atualizada",
  "description": "Descrição atualizada",
  "municipalityId": 2,
  "categoryId": 1
  // ...demais campos
}
```
```
HTTP/1.1 204 No Content
```


### 5. Remover projeto
```
DELETE http://localhost:8080/api/project/1
```
```
HTTP/1.1 204 No Content
```


## Imagens do Projeto (`/api/project-image`)

### 1. Listar imagens
```
GET http://localhost:8080/api/project-image
```
```json
[
  {
    "id": 10,
    "projectId": 1,
    "url": "https://.../imagem1.jpg"
  }
]
```


### 2. Buscar imagem por ID
```
GET http://localhost:8080/api/project-image/10
```


### 3. Listar imagens de um projeto
```
GET http://localhost:8080/api/project-image/project-id/1
```


### 4. Criar imagem para projeto
```
curl -X POST http://localhost:8080/api/project-image \
  -F "projectId=1" \
  -F "file=@/caminho/para/imagem.jpg"
```
```
HTTP/1.1 201 Created
Location: /api/project-image/11
```


### 5. Atualizar imagem
```
curl -X PUT http://localhost:8080/api/project-image/10 \
  -F "file=@/caminho/para/nova-imagem.jpg"
```
```
HTTP/1.1 204 No Content
```


### 6. Remover imagem
```
DELETE http://localhost:8080/api/project-image/10
```
```
HTTP/1.1 204 No Content
```


> **Obs:** Para endpoints que usam autenticação JWT, envie o header:
> `Authorization: Bearer <token>`

# Documentação das APIs do Sistema

---

## Autenticação

### 1. Autenticação Básica ou JWT
- **POST /authenticate**
  - **Basic:** Retorna o usuário autenticado
  - **JWT:** Retorna um token JWT
- **Exemplo de requisição:**
```
POST http://localhost:8080/authenticate
Content-Type: application/json

{
  "email": "usuario@email.com",
  "password": "senha"
}
```
- **Exemplo de resposta (JWT):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
}
```

---

## Usuário (`/api/user`)

- **GET /api/user** — Lista todos os usuários
- **GET /api/user/{id}** — Busca usuário por ID
- **GET /api/user/email/{email}** — Busca usuário por e-mail
- **GET /api/user/role/{role}** — Lista usuários por papel
- **POST /api/user** — Cria usuário
- **PUT /api/user/{id}** — Atualiza usuário
- **DELETE /api/user/{id}** — Remove usuário
- **PUT /api/user/update-password** — Atualiza senha
- **PUT /api/user/forgot-password** — Esqueci a senha (envia e-mail)
- **PUT /api/user/reset-password** — Redefine senha (token + nova senha)

**Exemplo de criação:**
```
POST http://localhost:8080/api/user
Content-Type: application/json

{
  "name": "João",
  "email": "joao@email.com",
  "password": "senha123",
  "role": "USER"
}
```

---

## Categoria (`/api/category`)

- **GET /api/category** — Lista todas as categorias
- **GET /api/category/{id}** — Busca categoria por ID
- **POST /api/category** — Cria categoria
- **PUT /api/category/{id}** — Atualiza categoria
- **DELETE /api/category/{id}** — Remove categoria

**Exemplo de criação:**
```
POST http://localhost:8080/api/category
Content-Type: application/json

{
  "name": "Educação"
}
```

---

## Município (`/api/municipality`)

- **GET /api/municipality** — Lista todos os municípios
- **GET /api/municipality/{id}** — Busca município por ID
- **POST /api/municipality** — Cria município

**Exemplo de criação:**
```
POST http://localhost:8080/api/municipality
Content-Type: application/json

{
  "name": "São Paulo"
}
```

---

## Projeto (`/api/project`)

- **GET /api/project** — Lista projetos (pode filtrar por município via JWT)
- **GET /api/project/{id}** — Busca projeto por ID
- **POST /api/project** — Cria projeto (multipart/form-data)
- **PUT /api/project/{id}** — Atualiza projeto
- **DELETE /api/project/{id}** — Remove projeto

**Exemplo de criação:**
```
curl -X POST http://localhost:8080/api/project \
  -F "title=Nova Praça" \
  -F "description=Praça central" \
  -F "municipalityId=2" \
  -F "categoryId=1" \
  -F "file=@/caminho/para/imagem.jpg"
```

---

## Imagens do Projeto (`/api/project-image`)

- **GET /api/project-image** — Lista todas as imagens
- **GET /api/project-image/{id}** — Busca imagem por ID
- **GET /api/project-image/project-id/{projectId}** — Lista imagens de um projeto
- **POST /api/project-image** — Cria imagem (multipart/form-data)
- **PUT /api/project-image/{id}** — Atualiza imagem (multipart/form-data)
- **DELETE /api/project-image/{id}** — Remove imagem

**Exemplo de criação:**
```
curl -X POST http://localhost:8080/api/project-image \
  -F "projectId=1" \
  -F "file=@/caminho/para/imagem.jpg"
```

---

> **Obs:** Para endpoints que usam autenticação JWT, envie o header:
> `Authorization: Bearer <token>`



INSERT INTO municipality (name, state) VALUES
                                           ('São Paulo', 'SP'),
                                           ('Campinas', 'SP'),
                                           ('Rio de Janeiro', 'RJ');

INSERT INTO user_model (name, email, cpf, phone, password, role, birth_date, municipality_id)
VALUES
    ('João Ribeiro', 'joao@example.com', '00000000000', '(11) 99999-0000', crypt('aa', gen_salt('bf')), 'CITIZEN', '1990-05-15', 1);

INSERT INTO user_model (name, email, cpf, phone, password, role, birth_date, municipality_id)
VALUES
    ('Maria Antonieta', 'maria@example.com', '11111111111', '(11) 98888-1111', crypt('aa', gen_salt('bf')), 'MODERATOR', '1985-10-22', 2);

INSERT INTO user_model (name, email, cpf, phone, password, role, birth_date, municipality_id)
VALUES
    ('Carlos Costa', 'carlos@example.com', '33333333333', '(21) 97777-2222', crypt('aa', gen_salt('bf')), 'ADMINISTRATOR', '1978-03-08', 3);
