# Marido de Aluguel

Plataforma web que conecta prestadores autônomos de pequenos reparos residenciais (elétrica, hidráulica, montagem de móveis, entre outros) a clientes de Maringá e região. O cliente busca, **compara até três prestadores lado a lado** e contrata por uma ordem de serviço registrada; depois da conclusão, avalia o prestador uma única vez.

Projeto acadêmico da disciplina de Engenharia de Software (Unicesumar), Atividade Extensionista de Prática (AEP), 2º Bimestre.
**ODS 8:** Trabalho Decente e Crescimento Econômico.

| Integrante | RA |
|---|---|
| Eduardo Gomes Michelon | 25068570-2 |
| Pedro Henrique de Souza Castilho | 25000689-2 |
| Pietro Borim Berbert | 25177983-2 |

## Estrutura do repositório

```
├── backend/            API REST em Java 21 + Spring Boot 3 (Maven)
│   └── src/main/java/br/unicesumar/maridodealuguel/
│       ├── controller/  rotas REST
│       ├── service/     regras de aplicação (transações, permissões por perfil)
│       ├── repository/  Spring Data JPA (inclui a busca com Haversine em SQL)
│       ├── domain/      entidades e regras de negócio (POO)
│       ├── dto/         objetos de entrada/saída com Bean Validation
│       ├── security/    JWT stateless, BCrypt, CORS
│       ├── exception/   exceções de negócio e tratamento global de erros
│       └── config/      perfil "local" (PostgreSQL embutido)
├── frontend/           SPA em HTML, CSS e JavaScript puro (módulos ES, sem build)
├── database/
│   ├── schema.sql      criação de tabelas, constraints, trigger e índices
│   ├── seed.sql        dados de exemplo de Maringá
│   └── criar-banco.sql usuário e banco num PostgreSQL instalado
├── iniciar.cmd         duplo clique: roda sem instalar nada
├── iniciar-com-postgres.cmd  duplo clique: roda com o PostgreSQL instalado
└── docker-compose.yml  PostgreSQL 16 local
```

## Como rodar

Pré-requisito: **Java 21+**. Não precisa instalar o Maven: o projeto traz o Maven Wrapper (`mvnw` / `mvnw.cmd`), que baixa a versão certa na primeira execução. A API também serve o front-end da pasta `/frontend`.

### Jeito mais fácil (Windows): duplo clique

| Arquivo | Banco usado | Precisa instalar |
|---|---|---|
| **`iniciar.cmd`** | PostgreSQL real embutido, recriado com `schema.sql` + `seed.sql` a cada execução | nada |
| **`iniciar-com-postgres.cmd`** | PostgreSQL instalado no computador; os dados ficam salvos | [PostgreSQL](https://www.postgresql.org/download/windows/) |

Os dois abrem o navegador em **http://localhost:8080** quando o sistema estiver pronto. A primeira execução demora alguns minutos (baixa o Maven e as dependências). Para encerrar, feche a janela preta.

Na primeira vez, o `iniciar-com-postgres.cmd` pede a senha do usuário `postgres` (a que você definiu na instalação) e cria sozinho o usuário `marido`, o banco `marido_aluguel` e as tabelas com os dados de exemplo (`database/criar-banco.sql`, `schema.sql` e `seed.sql`). Nas próximas vezes ele só inicia o sistema.

### Opção A pelo terminal: sem instalar nada (perfil `local`)

O perfil `local` sobe um **PostgreSQL 14 real** embutido na própria aplicação (binários oficiais, biblioteca zonky) e cria o banco com `database/schema.sql` e `database/seed.sql`. O banco é recriado a cada execução. É o que o `iniciar.cmd` faz.

Windows (PowerShell), um comando por linha:

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE = "local"
.\mvnw.cmd spring-boot:run
```

Linux / macOS:

```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

A primeira execução demora alguns minutos (baixa o Maven e as dependências). Quando aparecer `Started MaridoDeAluguelApplication`, abra **http://localhost:8080**. Para parar: `Ctrl+C`.

### Opção B pelo terminal: PostgreSQL pelo Docker (ou instalado)

1. Suba o banco. Na primeira execução o container roda `schema.sql` e `seed.sql`:

   ```bash
   docker compose up -d
   ```

   Sem Docker, com PostgreSQL instalado: crie o banco `marido_aluguel` (usuário `marido`, senha `marido123`) e rode `database/schema.sql` e depois `database/seed.sql` no `psql` ou no pgAdmin.

2. Suba a API sem perfil (no PowerShell, se antes usou a opção A na mesma janela, rode `Remove-Item Env:SPRING_PROFILES_ACTIVE`):

   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

3. Abra **http://localhost:8080**.

Contas de demonstração (senha `senha123` para todas):

| Perfil | E-mail | Observação |
|---|---|---|
| Cliente | `camila@email.com` | mora na Zona 07; tem a ordem OS-00009 concluída e **ainda sem avaliação** |
| Cliente | `juliana@email.com` | tem a ordem OS-00011 **aberta**, aguardando aceite |
| Prestador | `douglas@email.com` | nota 4,50; elétrica e hidráulica |
| Prestador | `roberto@email.com` | Sarandi, raio de 4 km: não aparece para quem está longe |

Variáveis de ambiente opcionais: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (mínimo 32 caracteres) e `FRONTEND_DIR`.
O front-end também funciona aberto por outro servidor estático (ex.: Live Server do VS Code): nesse caso ele chama `http://localhost:8080`, liberado no CORS.

### Testes

```powershell
cd backend
.\mvnw.cmd test
```

(Linux / macOS: `./mvnw test`.)

- `RegrasDeDominioTest`: regras de negócio puras (polimorfismo do orçamento, transições de status, avaliação única, recálculo da média).
- `FluxoCompletoApiTest`: teste de integração ponta a ponta contra um **PostgreSQL real embutido** (biblioteca zonky), criado com os mesmos `schema.sql` e `seed.sql`. Percorre o CRUD de serviços, a busca, a ordem de serviço do início ao fim, a avaliação duplicada, o trigger de imutabilidade, o relatório, a exportação segura e as permissões por perfil. Não precisa de Docker.
- `DesempenhoBuscaTest` (RNF01): carrega **5.000 prestadores** (10.000 serviços) no PostgreSQL e mede quatro buscas com filtros e ordenações diferentes. Limite exigido: 2 s. Medido nesta máquina: **39 a 60 ms** por busca.

### HTTPS (RNF02)

Em desenvolvimento a API roda em HTTP. Para HTTPS há o perfil `https` com certificado autoassinado:

```powershell
cd backend
keytool -genkeypair -alias marido -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 365 -storepass marido123 -dname "CN=localhost"
$env:SPRING_PROFILES_ACTIVE = "local,https"
.\mvnw.cmd spring-boot:run
```

(Com o banco do Docker, use só `"https"`.)

e acesse **https://localhost:8443**. Em produção o certificado viria de uma autoridade certificadora (ou de um proxy reverso com TLS).

## Endpoints

Todas as rotas, menos `/api/auth/**`, exigem `Authorization: Bearer <token>`. Cada rota declara o perfil aceito com `@PreAuthorize` (RNF02), e os services ainda conferem se o recurso pertence ao usuário.

| Método | Rota | Perfil | Requisito | Descrição |
|---|---|---|---|---|
| POST | `/api/auth/cadastro` | público | RF01 | Cadastro de cliente ou prestador (valida e-mail e telefone) |
| POST | `/api/auth/login` | público | RF01 | Login, devolve o token JWT |
| GET / PUT | `/api/usuarios/me` | ambos | RF01 | Ver e manter o próprio perfil |
| GET | `/api/prestadores/{id}` | ambos | RF06 | Perfil público: nota média, concluídos, serviços e avaliações |
| POST | `/api/servicos` | prestador | RF02 | **Create** de serviço (categoria, descrição, raio, cobrança, valor base) |
| GET | `/api/servicos/meus` | prestador | RF02 | **Read** dos serviços do prestador |
| GET | `/api/servicos/{id}` | ambos | RF02 | **Read** de um serviço |
| PUT | `/api/servicos/{id}` | prestador (dono) | RF02 | **Update** (inclusive trocar o modelo de cobrança) |
| DELETE | `/api/servicos/{id}` | prestador (dono) | RF02 | **Delete** (bloqueado com 409 se o serviço já foi contratado) |
| GET | `/api/servicos/categorias` | ambos | RF03 | Categorias existentes |
| GET | `/api/servicos/busca` | cliente | RF03 | `categoria` + `precoMin`, `precoMax`, `notaMin`, `distanciaMaxKm`, `ordenarPor` (`PRECO`, `NOTA`, `DISTANCIA`), `horas` |
| POST | `/api/ordens` | cliente | RF05 | Abre a ordem (`servicoId`, `horasEstimadas` se for por hora) |
| GET | `/api/ordens` | ambos | RF05 | Ordens do cliente ou recebidas pelo prestador |
| GET | `/api/ordens/{id}` | participantes | RF05 | Detalhe com histórico de status |
| PATCH | `/api/ordens/{id}/status` | prestador / ambos para cancelar | RF05 | Muda o status respeitando o ciclo |
| POST | `/api/ordens/{id}/avaliacao` | cliente da ordem | RF06 | Avalia uma única vez, só se concluída |
| GET | `/api/relatorios/prestador?inicio=&fim=` | prestador | RF07 | Concluídos, faturamento do período e evolução da nota |
| GET | `/api/relatorios/prestador/exportar?inicio=&fim=` | prestador | RF07 | **Exportação segura** em CSV: só os dados do próprio prestador (o id vem do token), sem cache, protegida contra injeção de fórmulas no Excel |

Erros seguem um formato único: `{ status, erro, mensagem, campos }`. Códigos: 400 (validação), 401 (sem login), 403 (perfil ou dono errado), 404, 409 (conflito: e-mail repetido, avaliação duplicada, serviço com histórico), 422 (regra de negócio, como transição de status inválida).

## Rastreabilidade (requisito → implementação)

| Requisito | Back-end | Tela da SPA |
|---|---|---|
| RF01 cadastro e autenticação | `AuthController`, `AuthService`, `CadastroRequest` (Bean Validation), `SecurityConfig`, `JwtService` | `#/entrar`, `#/cadastro`, `#/perfil` |
| RF02 serviços do prestador | `ServicoController` / `ServicoService` (CRUD) | `#/servicos`, `#/servicos/novo`, `#/servicos/{id}` |
| RF03 busca com filtros | `ServicoRepository.buscarComDistancia` (Haversine em SQL) | `#/buscar` |
| RF04 comparação de até 3 | dados da busca (valor estimado, nota, concluídos, distância) | `#/comparar` (estado em memória, `js/estado.js`) |
| RF05 ordem de serviço | `OrdemServico`, `StatusOrdem`, `HistoricoStatusOrdem`, `OrdemServicoService` | contratação em `#/comparar`, `#/ordens`, `#/ordens/{id}` |
| RF06 avaliação | `OrdemServico.avaliar`, `Prestador.registrarAvaliacao` | formulário em `#/ordens/{id}`, `#/prestadores/{id}` |
| RF07 relatórios e exportação segura | `RelatorioService`, `RelatorioCsv`, `RelatorioController.exportarCsv` | `#/relatorio` (botão "Exportar CSV") |
| RNF01 desempenho | filtro e ordenação no banco, índices em `categoria`, `valor_base`, `nota_media`, limite de 50 resultados; medido em `DesempenhoBuscaTest` | |
| RNF02 segurança | BCrypt, JWT stateless, `@PreAuthorize` por rota, checagem de dono nos services, perfil `https` | |
| RNF03 usabilidade | | SPA com roteamento por hash, sem recarregar entre busca, comparação e contratação; layout a partir de 320 px |
| RNF04 integridade | `UNIQUE(ordem_id)`, entidade `Avaliacao` sem setters e com `updatable=false`, trigger `trg_avaliacoes_imutaveis`, nenhuma rota de edição | |

## Onde estão os pilares de POO

- **Classe abstrata + herança:** `Usuario` (abstrata) → `Cliente` e `Prestador`, mapeadas em tabela única com o discriminador `tipo_usuario`. O método abstrato `getPerfil()` é sobrescrito (`@Override`) em cada subclasse e vira o papel de segurança no token JWT.
- **Interface + polimorfismo:** como no UML, a interface `Servico` declara `calcularOrcamento(...)` e é implementada por `ServicoPorHora` (valor base × horas) e `ServicoPorEmpreitada` (valor fechado), ambas com `@Override`. Entre a interface e as duas classes há a classe abstrata `ServicoBase implements Servico`, que existe só para o JPA mapear a hierarquia (discriminador `tipo_cobranca`). `OrdemServico` e a busca chamam `servico.calcularOrcamento(...)` sem saber o tipo concreto.
- **Mais polimorfismo:** cada constante de `StatusOrdem` sobrescreve `podeTransitarPara()`; cada exceção de negócio sobrescreve `getStatus()` e o `GlobalExceptionHandler` trata todas com um único método.
- **Encapsulamento:** atributos privados; `Prestador.notaMedia` não tem setter, muda só por `registrarAvaliacao(Avaliacao)`; `Avaliacao` não tem setters e só é criada por `OrdemServico.avaliar()`.
- **Composição 1:1 (UML):** `OrdemServico` → `Avaliacao`: no máximo uma, criada pela própria ordem e apagada junto com ela (`cascade` + `orphanRemoval` no JPA, `ON DELETE CASCADE` no banco).
- **Associações 1:N (UML):** `Cliente` solicita várias `OrdemServico`; `Prestador` oferece vários `Servico` (e executa as ordens desses serviços); cada `OrdemServico` contrata um `Servico`.

## Ajustes em relação à 1ª entrega

Referência: `AEP_1Bimestre_Marido_de_Aluguel.pdf` (requisitos, diagrama de classes da seção 5.2 e DER da seção 5.3). As tabelas, as classes e os nomes seguem o documento; abaixo estão só as mudanças que a implementação exigiu, cada uma ligada ao requisito que a justifica.

**Diagrama de classes (UML)**

1. **Dinheiro em `BigDecimal` / `NUMERIC(10,2)`.** O UML mostra `Double` em `notaMedia` e no retorno de `calcularOrcamento()`. Ponto flutuante gera erro de arredondamento em valores monetários (o próprio documento cita isso ao justificar o `NUMERIC` do PostgreSQL), então o Java usa `BigDecimal`.
2. **`calcularOrcamento(horasEstimadas)` recebe as horas.** No UML o método não tem parâmetros, mas `ServicoPorHora` "multiplica o valor base pela duração estimada" e precisa dessa duração. `ServicoPorEmpreitada` ignora o parâmetro.
3. **Classe abstrata `ServicoBase` entre a interface `Servico` e as classes `ServicoPorHora` / `ServicoPorEmpreitada`.** O JPA não persiste interfaces; a classe abstrata carrega os atributos comuns e o mapeamento da tabela `servicos`. A interface e as duas implementações continuam como no UML.
4. **`raioAtendimentoKm` fica no serviço, e não no `Prestador`.** O UML coloca o raio no prestador, mas o RF02 diz que o prestador o informa ao cadastrar cada serviço. Seguimos o requisito: cada serviço tem o próprio raio.
5. **`Usuario` ganhou `telefone`, `endereco`, `latitude`, `longitude` e `senhaHash`.** O RF01 exige validar o telefone, o RF03 exige a distância a partir do endereço, e o RNF02 exige a senha em hash.
6. **`status` é o enum `StatusOrdem`**, e não `String`, com a regra de transição dentro do enum (RF05). O `codigo` da ordem é derivado do id (`OS-00001`), sem coluna própria.

**Banco de dados (DER)**

7. **`usuarios`:** `telefone` (RF01), `endereco`, `latitude` e `longitude` (RF03: distância por Haversine, sem API externa; a SPA preenche as coordenadas pelo bairro escolhido ou pelo GPS do aparelho), `nota_media` (RF06: nota exibida no perfil público) e `quantidade_avaliacoes` (para `Prestador.registrarAvaliacao()` recalcular a média sem setter público).
8. **`servicos`:** `descricao` e `raio_atendimento_km`, os dois exigidos pelo RF02.
9. **`ordens_servico`:** `horas_estimadas` (base do orçamento por hora); `valor_final` guarda o valor calculado na abertura.
10. **Nova tabela `ordem_status_historico` (ordem_id, status, data_hora):** cumpre o "registro de data e hora de cada mudança" do RF05.
11. **`avaliacoes`:** `data_avaliacao`, `CHECK` da nota entre 1 e 5 (RF06), `ON DELETE CASCADE` em `ordem_id` (composição 1:1 do UML: "a exclusão da ordem elimina a avaliação") e um trigger que impede `UPDATE` (RNF04: imutável após o envio).
12. **Constraints `CHECK` e índices** em status, tipos, raio, valores e nas colunas de filtro da busca (RNF01).
13. **Exclusão de serviço já contratado é bloqueada (409).** A FK `ordens_servico.servico_id` não tem cascade de propósito: apagar o serviço apagaria o histórico das ordens e as avaliações, que precisam ser rastreáveis (RNF04).

**Outros**

14. **Rotas de apoio:** `GET/PUT /api/usuarios/me` ("manter meu perfil profissional", user story da Sprint 1) e `GET /api/prestadores/{id}` ("perfil público do profissional", RF06).
15. **Exportação do RF07 em CSV**, formato que abre direto no Excel. "Segura": exige token de prestador, o id vem do próprio token (não há como pedir dados de outro prestador), a resposta não fica em cache e as células são protegidas contra injeção de fórmulas.
16. **HTTPS (RNF02)** fica num perfil opcional (`https`) com certificado autoassinado, para não exigir certificado de quem só quer rodar o projeto localmente. Em produção, o certificado viria de uma autoridade certificadora.
17. **Filtro de preço** compara o valor base do serviço (preço da hora ou valor fechado), que é indexado; o valor estimado aparece no resultado e na comparação.
