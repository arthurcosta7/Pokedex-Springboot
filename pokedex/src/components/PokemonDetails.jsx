import { useEffect, useState } from 'react'
import { fetchPokemon } from '../services/pokedex.js'
import { PokemonImage, Stats, Types } from './Pokemon.jsx'

export default function PokemonDetails({ pokemon, trainers }) {
  const [detail, setDetail] = useState(null)
  const [error, setError] = useState('')
  const [attempt, setAttempt] = useState(0)
  useEffect(() => {
    const controller = new AbortController()
    fetchPokemon(pokemon.id, controller.signal)
      .then(setDetail)
      .catch(err => { if (!controller.signal.aborted) setError(err.message) })
    return () => controller.abort()
  }, [pokemon.id, attempt])
  function retry() { setError(''); setAttempt(value => value + 1) }
  if (error) return <div className="no-result" role="alert"><p>{error}</p><button onClick={retry}>Tentar novamente</button></div>
  if (!detail) return <div className="no-result" role="status"><p>Carregando Pokémon…</p></div>
  return <>
    <div className="identity"><span>No. {String(detail.id).padStart(3, '0')}</span><span>REGISTRADO ✓</span></div>
    <div className="specimen"><span className="crosshair top-left"/><PokemonImage pokemon={detail}/><span className="crosshair bottom-right"/></div>
    <div className="pokemon-heading"><h2>{detail.name}</h2><Types types={detail.types}/></div>
    <Stats stats={detail.stats}/>
    <p className="screen-caption">{trainers.filter(t => t.pokemonIds.includes(detail.id)).map(t => t.name).join(' · ') || 'SEM TREINADOR ASSOCIADO'}</p>
  </>
}
