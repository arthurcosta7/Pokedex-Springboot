const API_URL = (import.meta.env.VITE_API_URL || '/api').replace(/\/$/, '')

async function request(path, options = {}) {
  let response
  try {
    response = await fetch(`${API_URL}${path}`, options)
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new Error('Não foi possível conectar à API. Verifique se o backend está em execução.')
  }
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.detail || `A operação falhou (HTTP ${response.status}).`)
  }
  return response.status === 204 ? null : response.json()
}

export async function fetchPokedex(signal) {
  const [pokemon, types, trainers] = await Promise.all([
    request('/pokemons', { signal }),
    request('/types', { signal }),
    request('/trainers', { signal }),
  ])
  if (![pokemon, types, trainers].every(Array.isArray)) {
    throw new Error('A API retornou dados em um formato inválido.')
  }
  return { pokemon, types, trainers }
}

export const fetchPokemon = (id, signal) => request(`/pokemons/${id}`, { signal })
export const deletePokemon = id => request(`/pokemons/${id}`, { method: 'DELETE' })
export function savePokemon(pokemon, editing) {
  return request(editing ? `/pokemons/${pokemon.id}` : '/pokemons', {
    method: editing ? 'PUT' : 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(pokemon),
  })
}
