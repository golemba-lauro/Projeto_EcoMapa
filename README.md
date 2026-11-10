# EcoMapa — O destino certo para cada resíduo

Sistema digital para localização e descarte adequado de resíduos.

**AEP — Atividade de Estudo Programado 2026.2**
4º Semestre · Análise e Desenvolvimento de Sistemas e Engenharia de Software · UniCesumar · Maringá/PR

---

## Integrantes

| Nome | R.A. | GitHub |
|---|---|---|
| Lauro Golemba Junior | 25362737-2 | `@golemba-lauro` |
| Enzo Dell'Agnolo | 25017806-2 | `@enzoagnolo` |

**Repositório:** https://github.com/golemba-lauro/Projeto_EcoMapa

---

## Sobre o projeto

O descarte correto de resíduos depende não apenas da existência de locais apropriados, mas do acesso da população à informação sobre onde e como realizar esse descarte. Materiais como eletrônicos, pilhas, óleo de cozinha, roupas, plásticos, vidros e papelão têm destinações diferentes, e o cidadão frequentemente não sabe qual estabelecimento recebe cada um deles, onde fica e em que horário funciona.

O **EcoMapa** centraliza essas informações. O cidadão pesquisa pelo material que precisa descartar e o sistema retorna os pontos de coleta ativos que o recebem, com endereço, telefone, horário de funcionamento e a lista completa de materiais aceitos. Administradores mantêm os cadastros atualizados e podem desativar pontos que deixaram de receber determinado resíduo.

### Alinhamento com os ODS

- **ODS 11 — Cidades e Comunidades Sustentáveis:** atua sobre a meta 11.6 (redução do impacto ambiental das cidades e gestão de resíduos municipais), removendo a barreira de informação que leva o resíduo reciclável ao lixo comum.
- **ODS 12 — Consumo e Produção Responsáveis:** atua sobre a meta 12.5 (redução da geração de resíduos por reciclagem e reutilização), organizando a informação necessária para que o descarte correto seja uma escolha simples.

---

## Tecnologias e arquitetura

| Item | Escolha |
|---|---|
| Linguagem | Java — JDK 21 (LTS) |
| Banco de dados | MySQL 8 |
| Acesso a dados | JDBC com MySQL Connector/J |
| Padrão arquitetural | Arquitetura em camadas com padrão DAO |
| Interface | Console (menus em modo texto) |

**Camadas:**

| Camada | Responsabilidade |
|---|---|
| `model` | Classes de domínio: `Usuario` (abstrata), `Cidadao`, `Administrador`, `PontoColeta`, `Material`, `Feedback`. |
| `dao` | Acesso ao banco. Concentra todo o SQL e o uso do JDBC. |
| `service` | Regras de negócio (RN01 a RN07). Valida os dados antes de chamar o DAO. |
| `view` | Menus de console, separados em área do cidadão e área administrativa. |
| `util` | `ConexaoMySQL.java` — abertura e fechamento da conexão. |

---

## Lista de requisitos

| ID | Requisito | Origem |
|---|---|---|
| R01 | O sistema deve permitir ao administrador cadastrar pontos de coleta, com nome, endereço, telefone, horário de funcionamento e situação (ativo ou inativo). | Dor 2.2 · RN01 |
| R02 | O sistema deve permitir ao administrador cadastrar materiais, com nome, descrição e categoria (eletrônicos, pilhas e baterias, óleo de cozinha, roupas, plástico, vidro ou papelão), impedindo o cadastro de dois materiais com o mesmo nome. | Dor 2.2 · RN02 |
| R03 | O sistema deve permitir ao administrador vincular a cada ponto de coleta um ou mais materiais aceitos, exigindo pelo menos um material por ponto e impedindo o vínculo repetido do mesmo material. | RN03 |
| R04 | O sistema deve permitir ao cidadão consultar a lista de pontos de coleta ativos, exibindo nome, endereço, telefone, horário de funcionamento e materiais aceitos, ordenados por nome. | Dor 2.2 · RN04 |
| R05 | O sistema deve permitir ao cidadão pesquisar pontos de coleta pelo nome do material, retornando apenas os pontos ativos que aceitam aquele material, com nome, endereço e horário de funcionamento. | Dor 2.2 · RN06 |
| R06 | O sistema deve permitir ao administrador atualizar nome, endereço, telefone, horário de funcionamento, situação e materiais aceitos de um ponto de coleta já cadastrado. | Dor 2.2 · RN05 |
| R07 | O sistema deve permitir ao administrador excluir um ponto de coleta, removendo também seus vínculos com materiais e os feedbacks associados a ele. | RN05 · RN07 |
| R08 | O sistema deve permitir ao cidadão registrar feedback sobre um ponto de coleta, com descrição, data de envio e situação (pendente ou analisado), vinculado ao cidadão que o enviou e ao ponto avaliado. | RN07 |
| R09 | O sistema deve permitir o cadastro e a autenticação de usuários, com nome, e-mail, senha e tipo (cidadão ou administrador), liberando as operações de cadastro, alteração e exclusão apenas aos usuários do tipo administrador. | RN04 · RN05 |

---

## Regras de negócio

| ID | Regra | Onde será aplicada |
|---|---|---|
| RN01 | Um ponto de coleta deve possuir obrigatoriamente nome, endereço e horário de funcionamento. | `NOT NULL` na tabela `PONTO_COLETA` + validação em `PontoColetaService` |
| RN02 | Um material deve possuir um nome único dentro do sistema. | Restrição `UNIQUE` em `MATERIAL.nome` |
| RN03 | Um ponto de coleta deve estar relacionado a pelo menos um tipo de material aceito. | `PontoColetaService`, no cadastro e na atualização |
| RN04 | O cidadão poderá consultar informações dos pontos de coleta sem possuir acesso administrativo. | Camada `view` (menu público) |
| RN05 | Somente usuários administradores poderão cadastrar, alterar ou excluir pontos de coleta. | Verificação de tipo em `UsuarioService` |
| RN06 | Um ponto de coleta marcado como indisponível não deverá aparecer como opção ativa para descarte. | Filtro por `status` nas consultas do `PontoColetaDAO` |
| RN07 | Um feedback deverá estar relacionado a um cidadão e a um ponto de coleta. | Chaves estrangeiras obrigatórias na tabela `FEEDBACK` |

---

## Modelo de dados

O banco é composto por quatro entidades principais — `USUARIO`, `PONTO_COLETA`, `MATERIAL` e `FEEDBACK` — mais a entidade associativa `PONTO_MATERIAL`, que resolve o relacionamento N:N entre pontos e materiais.

| Tabela | Chaves | Cardinalidade |
|---|---|---|
| `USUARIO` | PK: `id` · UNIQUE: `email` · coluna discriminadora: `tipo` | 1:N com `FEEDBACK` |
| `PONTO_COLETA` | PK: `id` · NOT NULL: `nome`, `endereco`, `horarioFuncionamento` | 1:N com `FEEDBACK` e 1:N com `PONTO_MATERIAL` |
| `MATERIAL` | PK: `id` · UNIQUE: `nome` | 1:N com `PONTO_MATERIAL` |
| `PONTO_MATERIAL` | PK composta: (`idPontoColeta`, `idMaterial`) · FK para `PONTO_COLETA` e `MATERIAL` | Resolve o N:N entre ponto e material |
| `FEEDBACK` | PK: `id` · FK: `idUsuario`, `idPontoColeta` (ambas obrigatórias) | N:1 com `USUARIO` e N:1 com `PONTO_COLETA` |

A coluna `tipo` de `USUARIO` funciona como discriminadora: é ela que mapeia a herança `Usuario → Cidadao / Administrador` do diagrama de classes para uma única tabela no banco relacional.

---

## Operações CRUD

O CRUD exigido na segunda entrega é implementado sobre a entidade principal, `PontoColeta`:

| Operação | Função no sistema | Implementação prevista |
|---|---|---|
| CREATE | Cadastro de novos pontos de coleta (R01). | `PontoColetaDAO.inserir(PontoColeta p)` — INSERT via JDBC |
| READ | Consulta dos pontos cadastrados e pesquisa por material (R04 e R05). | `PontoColetaDAO.listar()` e `buscarPorMaterial(String nome)` |
| UPDATE | Atualização das informações dos pontos (R06). | `PontoColetaDAO.atualizar(PontoColeta p)` |
| DELETE | Exclusão de pontos de coleta (R07). | `PontoColetaDAO.excluir(int id)`, com remoção em cascata |

---

## Cronograma de execução

O cronograma cobre as duas entregas da AEP: as etapas **1 a 9** correspondem ao primeiro bimestre (planejamento e estruturação) e as etapas **10 a 17** ao segundo bimestre (implementação e integração).

| Etapa | Atividade | Responsável | Data |
|---|---|---|---|
| 1 | Levantamento do problema e pesquisa | Enzo | 04/09/2026 |
| 2 | Definição da solução e alinhamento com os ODS | Lauro | 05/09/2026 |
| 3 | Definição da lista de requisitos e regras de negócio | Lauro | 08/09/2026 |
| 4 | Criação do Diagrama de Classes | Lauro e Enzo | 09/09/2026 |
| 5 | Criação do DER | Lauro e Enzo | 09/09/2026 |
| 6 | Definição da arquitetura e da justificativa técnica | Lauro e Enzo | 10/09/2026 |
| 7 | Criação do repositório e da estrutura de pastas no GitHub | Lauro e Enzo | 10/09/2026 |
| 8 | Redação e revisão do documento da 1ª entrega | Lauro e Enzo | 11/09/2026 |
| 9 | Entrega do documento da 1ª etapa | Lauro e Enzo | 11/09/2026 |
| 10 | Criação do banco de dados e do script.sql | Lauro | 06/10/2026 |
| 11 | Implementação das classes de domínio (camada model) | Enzo | 13/10/2026 |
| 12 | Implementação da camada DAO e da conexão JDBC | Lauro e Enzo | 20/10/2026 |
| 13 | Implementação da camada de serviço (regras de negócio) | Enzo | 27/10/2026 |
| 14 | Implementação da camada de visão e do CRUD completo | Lauro e Enzo | 03/11/2026 |
| 15 | Testes, correções e ajustes de integração | Lauro e Enzo | 10/11/2026 |
| 16 | Documentação final e README com instruções de execução | Lauro e Enzo | 17/11/2026 |
| 17 | Entrega final e apresentação | Lauro e Enzo | *(data a definir)* |

---

## Estrutura do repositório

```
EcoMapa/
├── src/
│   └── br/unicesumar/ecomapa/
│       ├── model/      → classes de domínio
│       ├── dao/        → acesso ao banco (JDBC)
│       ├── service/    → regras de negócio
│       ├── view/       → menus do console
│       ├── util/       → ConexaoMySQL.java
│       └── Main.java
├── docs/
│   ├── diagrama-classe.png
│   └── der.png
├── database/
│   └── script.sql
├── README.md
└── .gitignore
```

---

## Como executar

### Pré-requisitos

- JDK 21 ou superior (`java -version` para conferir)
- MySQL 8 instalado e em execução
- Driver MySQL Connector/J (`mysql-connector-j-<versão>.jar`)

O projeto roda em Windows, macOS e Linux sem alteração de código, por ser executado sobre a JVM.

### Passos

1. Clonar o repositório:
   ```bash
   git clone https://github.com/golemba-lauro/Projeto_EcoMapa.git
   cd Projeto_EcoMapa
   ```

2. Criar o banco executando o script no MySQL:
   ```bash
   mysql -u root -p < database/script.sql
   ```

3. Informar usuário e senha do banco em `src/br/unicesumar/ecomapa/util/ConexaoMySQL.java`.

4. Compilar (Linux/macOS):
   ```bash
   javac -cp .:lib/mysql-connector-j.jar -d bin $(find src -name "*.java")
   ```
   No Windows, trocar `:` por `;` no classpath.

5. Executar:
   ```bash
   java -cp bin:lib/mysql-connector-j.jar br.unicesumar.ecomapa.Main
   ```

---

## Documentação

- `docs/diagrama-classe.png` — Diagrama de Classes (herança, polimorfismo e composição 1:N entre `PontoColeta` e `Feedback`)
- `docs/der.png` — Diagrama Entidade-Relacionamento, com chaves primárias, chaves estrangeiras e cardinalidades
- `database/script.sql` — script de criação das tabelas

---

## Status do projeto

| Entrega | Foco | Situação |
|---|---|---|
| 1ª (1º bimestre) | Concepção, planejamento e estruturação | Em andamento |
| 2ª (2º bimestre) | Software funcional com POO, CRUD e persistência | Não iniciada |
