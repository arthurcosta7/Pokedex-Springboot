# Backend da Pokédex

Java 25, Spring Boot 4.1.2, Spring Web, Spring Data MongoDB e Bean Validation.
Veja o [README principal](../../README.md#ordem-para-executar) para a ordem de
execução e a solução dos erros mais comuns de inicialização.

## Iniciar com MongoDB Atlas

Com o cluster disponível, execute nesta pasta (`pokedex/pokedex`, onde fica o `pom.xml`):

```bash
export MONGODB_URI='mongodb+srv://arthurcosta1973_db_user:PUb9UD1UpRm8li5m@pokedex.eceijyn.mongodb.net'
./mvnw spring-boot:run
```

Substitua a URI pela sua conexão real. Defina a variável no mesmo terminal do Spring.
Um arquivo `.env` não é carregado automaticamente. Pela IDE, configure a variável
no ambiente da execução. Sem ela, o projeto tenta usar `localhost:27017/pokedex`.

Aguarde `Started PokedexApplication`. Mantenha este terminal aberto e, em outro,
verifique a consulta com `curl -i http://localhost:8080/api/pokemons` antes de
iniciar o frontend. O esperado é HTTP 200.

## Testes

```bash
./mvnw test
```

Os testes são uma verificação separada; não precisam ser executados para iniciar o projeto.

## Organização

A aplicação usa `controller`, `service`, `repository`, `model` e DTOs validados.
Os cinco endpoints CRUD de Pokémon estão em `/api/pokemons`.
Os catálogos de leitura `/api/types` e `/api/trainers` preservam as funcionalidades
anteriores. As coleções MongoDB são `pokemons`, `types` e `trainers`.

A propriedade de conexão `spring.mongodb.uri` segue a
[documentação do Spring Boot](https://docs.spring.io/spring-boot/4.0/reference/data/nosql.html).
