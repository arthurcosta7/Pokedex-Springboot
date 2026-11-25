from pathlib import Path
import unittest
from export_seed import convert


class SeedTests(unittest.TestCase):
    def test_sql_data_and_relations(self):
        source = Path(__file__).resolve().parents[3] / 'Script_Pokedex.sql'
        data = convert(source.read_text())
        self.assertEqual((len(data['pokemons']), len(data['types']), len(data['trainers'])), (151, 18, 12))
        pokemon = {p['_id']: p for p in data['pokemons']}
        self.assertEqual(pokemon[25]['stats']['defense'], 30)
        self.assertEqual(pokemon[83]['name'], "Farfetch'd")
        self.assertEqual(pokemon[151]['types'], [])  # O SQL não associa tipos ao Mew.
        self.assertEqual(pokemon[1]['types'], [{'_id': 4, 'name': 'Grass'}, {'_id': 8, 'name': 'Poison'}])
        self.assertEqual(data['trainers'][5]['pokemonIds'], [48, 48, 48, 49])


if __name__ == '__main__':
    unittest.main()
