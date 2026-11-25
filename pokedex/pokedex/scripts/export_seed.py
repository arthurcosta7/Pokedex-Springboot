"""Converte os INSERTs do SQL da atividade em documentos para mongoimport."""
import json
from pathlib import Path
import re


def read_inserts(source):
    tables = {}
    for match in re.finditer(r"INSERT INTO (\w+)\s*\([^;]+?\)\s*VALUES\s*(.*?);", source, re.S):
        rows = []
        for row in re.finditer(r"\(((?:'[^']*(?:''[^']*)*'|[^()'])*)\)", match[2]):
            tokens = re.findall(r"'(?:[^']|'')*'|-?\d+", row[1])
            rows.append([token[1:-1].replace("''", "'") if token.startswith("'") else int(token)
                         for token in tokens])
        tables[match[1]] = rows
    return tables


def convert(source):
    tables = read_inserts(source)
    types = {row[0]: {"_id": row[0], "name": row[1]} for row in tables['Types']}
    stats = ('hp', 'attack', 'defense', 'specialAttack', 'specialDefense', 'speed')
    pokemons = {row[0]: {"_id": row[0], "name": row[1], "stats": dict(zip(stats, row[2:])), "types": []}
                for row in tables['Pokemon']}
    trainers = {row[0]: {"_id": row[0], "name": row[1], "badges": row[2], "pokemonIds": []}
                for row in tables['Trainers']}
    for pokemon_id, type_id in tables['Rel_Pokemon_Type']:
        pokemons[pokemon_id]['types'].append(types[type_id].copy())
    for trainer_id, pokemon_id in tables['Rel_Trainer_Pokemon']:
        if pokemon_id not in pokemons:
            raise ValueError(f'Pokémon referenciado não encontrado: {pokemon_id}')
        trainers[trainer_id]['pokemonIds'].append(pokemon_id)
    return {'types': list(types.values()), 'pokemons': list(pokemons.values()), 'trainers': list(trainers.values())}


if __name__ == '__main__':
    backend = Path(__file__).resolve().parents[1]
    source = backend.parents[1] / 'Script_Pokedex.sql'
    destination = backend / 'target/seed'
    documents = convert(source.read_text(encoding='utf-8'))
    destination.mkdir(parents=True, exist_ok=True)
    for collection, values in documents.items():
        path = destination / f'{collection}.json'
        path.write_text(json.dumps(values, ensure_ascii=False, indent=2), encoding='utf-8')
        print(f'{path}: {len(values)} documentos')
