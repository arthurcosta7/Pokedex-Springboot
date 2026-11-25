# Pokédex — Atividade 04

Pokédex da Atividade 03 integrada a React + Vite → Spring Boot REST → MongoDB.
Os dados iniciais vêm exclusivamente do [Script_Pokedex.sql](Script_Pokedex.sql) fornecido pelo professor.
Não é necessário instalar ou executar MySQL.

[Enunciado da Atividade 04](https://github.com/joaopauloaramuni/desenvolvimento-e-integracao-de-aplicacoes-web/blob/main/ATIVIDADES/ATIVIDADE%2004.md).

## Requisitos implementados

- Consulta dos Pokémon e dos detalhes por ID através da API.
- Pesquisa por nome/número, filtro por tipo e exibição dos seis atributos.
- Cadastro, edição e exclusão por formulários na Pokédex, com persistência no MongoDB.
- Validação de ID, nome, atributos e tipos; respostas para ID duplicado e registro inexistente.
- Interface original preservada, incluindo consulta dos treinadores e equipes persistidas no MongoDB (desafio opcional do enunciado).
- SQL original da atividade incluído no projeto.

## Organização

```text
Script_Pokedex.sql                 # fonte original dos dados
pokedex/                           # frontend React + Vite
  src/App.jsx                      # consulta, navegação e integração dos formulários
  src/components/PokemonDialog.jsx # cadastrar, editar e confirmar exclusão
  src/components/PokemonDetails.jsx# consulta de detalhes via API
  src/services/pokedex.js           # fetch para todos os endpoints
  pokedex/                         # backend Spring Boot
    pom.xml
    src/main/java/com/diaw/pokedex/
      controller/                  # endpoints e tratamento de erros
      dto/PokemonRequest.java      # dados de entrada e validação
      model/                       # documentos e atributos
      repository/                  # acesso ao MongoDB
      service/PokemonService.java  # regras e operações CRUD
```

## Ordem para executar

Use esta ordem: **MongoDB Atlas disponível → backend Spring Boot → frontend React**.
O Atlas roda na nuvem: não é necessário iniciar um MongoDB local para usar seu cluster.
Mantenha o backend e o frontend abertos em dois terminais separados.

### 1. Conferir os requisitos

```bash
java -version
node --version
```

O backend exige **JDK 25** (não apenas o JRE). O frontend exige Node.js compatível
com Vite 8: **22.12+ ou 24+**. O Maven Wrapper `./mvnw` já está no projeto;
não é obrigatório instalar Maven separadamente. Na primeira execução, ele precisa
de internet para baixar Maven e as dependências.

### 2. Conferir a conexão com o Atlas

No painel do Atlas:

- Confirme que o cluster está disponível.
- Em **Database Access**, confira o usuário do banco e suas permissões de leitura e escrita.
- Em **Network Access**, permita o IP atual da máquina que executa o backend.
- Em **Connect → Drivers**, copie a URI e preencha usuário, senha e nome do banco.

O usuário do banco é diferente da conta usada para entrar no site do Atlas.
O nome do banco na URI deve ser exatamente o utilizado no seu cluster.
Os comandos abaixo usam `pokedex` como exemplo. Caracteres especiais em usuário
e senha precisam de codificação de URL. Não salve credenciais no README nem no GitHub.

### 3. Iniciar o backend — terminal 1

```bash
cd /home/arthur/Documentos/DIAW_Atv3_2110/pokedex/pokedex
export MONGODB_URI='mongodb+srv://USUARIO:SENHA@SEU_CLUSTER/pokedex'
./mvnw spring-boot:run
```

**Substitua a URI de exemplo pela sua conexão real antes de executar.**
Defina `MONGODB_URI` no mesmo terminal em que executar `./mvnw`.
Ao abrir um novo terminal, será necessário definir a variável novamente.
O Spring deste projeto não carrega arquivos `.env` automaticamente.
Se iniciar pela IDE, configure `MONGODB_URI` nas variáveis de ambiente da execução.

Execute o comando na pasta que contém o `pom.xml`: `pokedex/pokedex`.
Se estiver usando Maven instalado, o equivalente é `mvn spring-boot:run`.
No Windows, use `mvnw.cmd spring-boot:run` e configure a variável de ambiente
conforme seu terminal.

Aguarde a mensagem **`Started PokedexApplication`**. O backend fica em
`http://localhost:8080` por padrão. A variável `PORT`, se definida, muda essa porta.
Não feche esse terminal enquanto estiver usando a aplicação.

### 4. Verificar se o backend acessa o banco

Em outro terminal, execute:

```bash
curl -i http://localhost:8080/api/pokemons
```

O esperado é **HTTP 200** e uma lista JSON. Uma lista `[]` indica que a coleção
consultada está vazia; não é erro de inicialização. Se retornar 503 ou erro de
conexão, confira o terminal do Spring e a configuração do Atlas antes de prosseguir.
A mensagem `Started PokedexApplication` sozinha não comprova a conexão com o banco:
o comando acima verifica também uma consulta real.

### 5. Iniciar o frontend — terminal 2

Na primeira execução, ou após mudança nas dependências:

```bash
cd /home/arthur/Documentos/DIAW_Atv3_2110/pokedex
npm install
```

Em seguida, execute nessa mesma pasta:

```bash
npm run dev
```

Abra o endereço mostrado pelo Vite, normalmente **http://localhost:5173**.
A porta 8080 pertence à API; a interface é acessada pela porta informada pelo Vite.
Não feche os dois terminais durante o uso. Para encerrar, pressione `Ctrl+C` em cada um.

O proxy em `vite.config.js` encaminha `/api` ao Spring em `localhost:8080`.
Se alterar a porta do backend, ajuste esse proxy.

### Próximas execuções

Com o Atlas disponível, basta repetir o passo 3 no terminal 1 e executar
`npm run dev` na pasta do frontend no terminal 2. Não é necessário executar
`npm install`, testes ou build a cada inicialização.

### Se o Spring Boot apresentar erro

| Mensagem ou situação | O que conferir |
| --- | --- |
| `No plugin found for prefix 'springboot'` | O comando correto é `./mvnw spring-boot:run`, com hífen em `spring-boot`. |
| `There is no POM in this directory` | Entre em `pokedex/pokedex`, onde está o `pom.xml`. |
| `release version 25 not supported` ou `invalid target release: 25` | Use o JDK 25; confira também o Java selecionado pela IDE e `JAVA_HOME`. |
| `Permission denied` ao executar `./mvnw` | Na pasta do backend, execute `chmod +x mvnw` e tente novamente. |
| `Port 8080 was already in use` | Encerre a outra execução que ocupa a porta antes de iniciar novamente. |
| Tentativa de conexão com `localhost:27017` quando usa Atlas | `MONGODB_URI` não chegou à execução; defina-a no mesmo terminal ou na configuração da IDE. |
| `Authentication failed` | Confira usuário do banco, senha, codificação de caracteres especiais e permissões no Atlas. |
| `UnknownHost`, erro de DNS/SRV ou timeout | Confira o endereço do cluster, sua conexão, disponibilidade do cluster e IP permitido em Network Access. |
| `BUILD FAILURE` | Essa é a conclusão do Maven; leia o erro anterior e as linhas `Caused by` para identificar a causa. |

Se o erro continuar, compartilhe o comando executado e o trecho final do terminal,
incluindo `Caused by`, **sem senhas ou URI de conexão**.

## Uso

Na Pokédex, use **Cadastrar Pokémon**, **Editar** e **Excluir**.
Na edição, o número do Pokémon permanece fixo. A exclusão exige confirmação
e remove todas as ocorrências desse Pokémon das equipes de treinadores.
O formulário preserva os valores em caso de falha e impede envios durante a operação.

Para produção, execute `npm run build` e sirva `dist/`, encaminhando `/api` ao
backend no mesmo domínio. O Spring não serve o frontend automaticamente.
`VITE_API_URL` permite mudar a base da API no build; se usar domínios distintos,
configure também CORS no backend para a origem exata do frontend.

## API REST

| Método | Endpoint | Resposta |
| --- | --- | --- |
| GET | `/api/pokemons` | 200, lista ordenada por ID |
| GET | `/api/pokemons/{id}` | 200, detalhes; 404 se inexistente |
| POST | `/api/pokemons` | 201 com `Location`; 409 se ID duplicado |
| PUT | `/api/pokemons/{id}` | 200; 404 se inexistente |
| DELETE | `/api/pokemons/{id}` | 204 sem corpo; 404 se inexistente |
| GET | `/api/types` | Tipos disponíveis para filtro e formulário |
| GET | `/api/trainers` | Treinadores e equipes da interface anterior |

Exemplo de corpo para POST/PUT (ID da URL e do corpo devem coincidir no PUT):

```json
{
  "id": 152,
  "name": "Chikorita",
  "stats": {
    "hp": 45,
    "attack": 49,
    "defense": 65,
    "specialAttack": 49,
    "specialDefense": 65,
    "speed": 45
  },
  "typeIds": [4]
}
```

`id` deve ser um inteiro positivo; nome é obrigatório (até 100 caracteres).
Os seis atributos são inteiros de 0 a 255. `typeIds` é uma lista de até dois IDs
existentes em `types`, sem repetição. Lista vazia é permitida, conforme os dados do SQL.
As respostas resolvem `typeIds` para objetos `types: [{id, name}]`.
Dados inválidos retornam 400; indisponibilidade do banco retorna 503.
Erros incluem a mensagem em `detail`.

No MongoDB, as coleções são `pokemons`, `types` e `trainers`; o ID numérico é salvo
como `_id`. Os tipos embutidos também usam `_id` no banco e `id` nas respostas.
Não há CRUD de treinadores/tipos, autenticação ou paginação nesta atividade.

## Verificação

```bash
cd pokedex
npm run lint
npm run build
cd pokedex
./mvnw test
```

Os testes Java cobrem inicialização, endpoints, validação, conflitos e regras do
serviço. Os testes unitários usam mocks; para verificar persistência, com o MongoDB
rodando, cadastre um registro, recarregue a página, edite, recarregue novamente e
exclua. Os endpoints também podem ser testados pelo Postman, Insomnia ou curl:

```bash
curl http://localhost:8080/api/pokemons
curl http://localhost:8080/api/pokemons/25
```

## Integrantes

- Matheus Oliveira
- Arthur Costa

A atividade pede participação em dupla com Pair Programming e entrega do link do
repositório no Canvas pelos dois integrantes.

## Fontes

- Dados: `Script_Pokedex.sql` do professor, incluído sem alterações.
- Imagens: [PokeAPI sprites](https://github.com/PokeAPI/sprites), carregadas por ID.
  As imagens precisam de internet; novos IDs sem imagem mostram uma alternativa.
