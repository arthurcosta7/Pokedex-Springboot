# Frontend da Pokédex

React + Vite integrado à API Spring Boot. A ordem é: Atlas disponível → backend
Spring Boot → este frontend. Veja o [passo a passo completo](../README.md#ordem-para-executar).

Em um segundo terminal, nesta pasta (`pokedex`), execute:

```bash
npm install
npm run dev
npm run lint
npm run build
```

O proxy do Vite encaminha `/api` para `http://localhost:8080`.
A interface permite consultar, cadastrar, editar e excluir Pokémon.
Pesquisa e filtro são aplicados à lista recebida da API; os detalhes são consultados
em `GET /api/pokemons/{id}`. Nenhum dado é carregado do JSON antigo.
