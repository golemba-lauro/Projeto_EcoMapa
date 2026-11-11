# Roteiro de testes — EcoMapa

Etapa 15 do cronograma: testes, correções e ajustes de integração.

Os testes abaixo foram executados sobre o sistema integrado (camadas `view`, `service`, `dao` e banco MySQL), com o banco recriado a partir de `database/script.sql`.

**Ambiente:** JDK 21 · MySQL 8 · MySQL Connector/J · Windows 11

---

## Casos de teste

| # | Caso | Requisito / regra | Procedimento | Resultado esperado | Resultado |
|---|---|---|---|---|---|
| 01 | Criar conta de administrador | R09 | Menu inicial → `2` → tipo `2` | Conta criada e login liberado | OK |
| 02 | Criar conta de cidadão | R09 | Menu inicial → `2` → tipo `1` | Conta criada e login liberado | OK |
| 03 | E-mail repetido | R09 | Criar conta com e-mail já existente | Cadastro recusado | OK |
| 04 | Autenticação com senha errada | R09 | Entrar com senha incorreta | "E-mail ou senha incorretos" | OK |
| 05 | Direcionamento por tipo de usuário | RN04 · RN05 | Entrar como cidadão e, depois, como administrador | Cada um cai na sua área, com permissões diferentes | OK |
| 06 | Cadastrar material | R02 | Área administrativa → `6` | Material gravado com código | OK |
| 07 | Material com nome repetido | R02 · RN02 | Cadastrar "Plástico" duas vezes | Segundo cadastro recusado | OK |
| 08 | Material repetido sem acento | RN02 | Cadastrar "plastico" depois de "Plástico" | Recusado — a collation do banco ignora acento e maiúsculas | OK |
| 09 | **CREATE** — cadastrar ponto de coleta | R01 · RN01 | Área administrativa → `2` | Ponto gravado com os materiais vinculados | OK |
| 10 | Ponto sem material vinculado | R03 · RN03 | Tentar cadastrar ponto antes de existir material | Cadastro bloqueado com aviso da RN03 | OK |
| 11 | Ponto com campo obrigatório vazio | RN01 | Deixar nome ou endereço em branco | Cadastro recusado | OK |
| 12 | Vínculo repetido do mesmo material | R03 | Informar o mesmo código duas vezes | Vínculo gravado uma única vez | OK |
| 13 | **READ** — listar pontos (cidadão) | R04 · RN06 | Área do cidadão → `1` | Apenas pontos ativos, ordenados por nome, com materiais aceitos | OK |
| 14 | **READ** — pesquisar por material | R05 | Área do cidadão → `2` | Apenas pontos ativos que aceitam o material pesquisado | OK |
| 15 | Pesquisa sem resultado | R05 | Pesquisar material não aceito por nenhum ponto | Mensagem de nenhum ponto encontrado | OK |
| 16 | **UPDATE** — atualizar ponto | R06 | Área administrativa → `3` | Dados e materiais atualizados no banco | OK |
| 17 | Desativar ponto de coleta | RN06 | Atualizar o ponto marcando situação como inativo | Some das listagens do cidadão, continua visível ao administrador | OK |
| 18 | **DELETE** — excluir ponto | R07 | Área administrativa → `4` | Ponto, vínculos e feedbacks removidos em cascata | OK |
| 19 | Registrar feedback | R08 · RN07 | Área do cidadão → `4` | Feedback gravado com data e situação "pendente" | OK |
| 20 | Analisar feedback | R08 | Área administrativa → `7` | Situação alterada para "analisado" | OK |
| 21 | Listar os próprios feedbacks | R08 | Área do cidadão → `5` | Apenas os feedbacks do cidadão autenticado | OK |
| 22 | Persistência entre execuções | — | Encerrar e reabrir o sistema | Todos os dados continuam no banco | OK |
| 23 | Opção inválida no menu | — | Digitar letra ou número fora da faixa | "Opcao invalida", sem encerrar o programa | OK |

---

## Correções aplicadas nesta etapa

| Problema | Causa | Correção |
|---|---|---|
| Palavras com acento chegavam corrompidas ao banco, e a RN02 deixava de recusar duplicatas sem acento | O Java 21 assume UTF-8 por padrão, enquanto o console do Windows em português entrega os caracteres em outra codificação | `Main.java` passou a ler a codificação real do console com `System.console().charset()` e a usá-la no `Scanner` |
| Divergência entre o `script.sql` e as classes Java | O script inicial usava nomes de colunas diferentes dos usados pelos DAOs | Script reescrito a partir do DER, com os mesmos nomes de colunas dos DAOs |

---

## Observações

- O sistema não impede dois pontos de coleta com o mesmo nome: nenhuma regra de negócio exige nome único para ponto de coleta — a restrição `UNIQUE` existe apenas em `MATERIAL.nome` (RN02) e em `USUARIO.email` (R09).
- A exclusão de um ponto de coleta remove seus vínculos e feedbacks por `ON DELETE CASCADE`; já a exclusão de um usuário é bloqueada por `ON DELETE RESTRICT` enquanto houver feedback vinculado a ele, preservando a RN07.
