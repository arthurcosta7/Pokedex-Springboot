import { useEffect, useState } from 'react'
import { fetchPokedex } from './services/pokedex.js'
import { PokemonImage } from './components/Pokemon.jsx'
import PokemonDetails from './components/PokemonDetails.jsx'
import PokemonDialog from './components/PokemonDialog.jsx'
import './App.css'

export default function App() {
  const [data, setData] = useState({ pokemon: [], types: [], trainers: [] })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [attempt, setAttempt] = useState(0)
  const [action, setAction] = useState(null)
  const [revision, setRevision] = useState(0)
  const [notice, setNotice] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    fetchPokedex(controller.signal)
      .then(result => { if (!controller.signal.aborted) setData(result) })
      .catch(err => { if (!controller.signal.aborted) setError(err.message) })
      .finally(() => { if (!controller.signal.aborted) setLoading(false) })
    return () => controller.abort()
  }, [attempt])

  function retry() {
    setLoading(true)
    setError('')
    setAttempt(value => value + 1)
  }

  const [query, setQuery] = useState('')
  const [type, setType] = useState('')
  const [selectedId, setSelectedId] = useState(1)
  const [mode, setMode] = useState('pokemon')
  const [trainerId, setTrainerId] = useState(1)
  const search = query.trim().toLowerCase().replace(/^#/, '')
  const list = data.pokemon.filter(p => (p.name.toLowerCase().includes(search) || (/^\d+$/.test(search) && p.id === Number(search))) && (!type || p.types.some(t => t.name === type)))
  const selected = list.find(p => p.id === selectedId) || list[0]
  const index = selected ? list.indexOf(selected) : -1
  const trainer = data.trainers.find(t => t.id === trainerId) || data.trainers[0]
  function move(step) {
    if (mode === 'trainers' && trainer) {
      const position = data.trainers.indexOf(trainer)
      setTrainerId(data.trainers[(position + step + data.trainers.length) % data.trainers.length].id)
    } else if (list.length) setSelectedId(list[(index + step + list.length) % list.length].id)
  }
  function openPokemon(id) { setQuery(''); setType(''); setSelectedId(id); setMode('pokemon') }
  function saved(result, completedAction) {
    if (completedAction.mode === 'delete') {
      const id = completedAction.pokemon.id
      setData(current => ({ ...current,
        pokemon: current.pokemon.filter(p => p.id !== id),
        trainers: current.trainers.map(t => ({ ...t, pokemonIds: t.pokemonIds.filter(value => value !== id) })),
      }))
      setNotice('Pokémon excluído.')
    } else {
      setData(current => ({ ...current,
        pokemon: [...current.pokemon.filter(p => p.id !== result.id), result].sort((a, b) => a.id - b.id),
      }))
      openPokemon(result.id)
      setNotice(completedAction.mode === 'edit' ? 'Pokémon atualizado.' : 'Pokémon cadastrado.')
    }
    setRevision(value => value + 1)
    setAction(null)
  }

  if (loading || error) return <main className="workbench"><section className="no-result" role={error ? 'alert' : 'status'}><h1>POKÉDEX</h1><p>{loading ? 'Carregando dados da API…' : error}</p>{error && <button onClick={retry}>Tentar novamente</button>}</section></main>

  return <main className="workbench"><section className="device" aria-label="Pokédex interativa"><header className="device-head"><div className="sensor"><i /></div><div className="leds" aria-hidden="true"><i /><i /><i /></div><div className="device-name"><h1>POKÉDEX</h1><span>PERSONAL POKÉMON INDEX</span></div></header><div className="seam" aria-hidden="true"/><div className="device-body"><div className="screen-frame"><div className="screen-dots" aria-hidden="true"><i/><i/></div><div className="display"><div className="screen-top"><span>● {mode === 'pokemon' ? 'POKÉMON DATABASE' : 'TRAINER DATABASE'}</span><span>KANTO / 01</span></div><div className="screen-content">{mode === 'pokemon' ? selected ? <PokemonDetails key={`${selected.id}-${revision}`} pokemon={selected} trainers={data.trainers}/> : <div className="no-result"><span>?</span><h2>Sem sinal</h2><p>Nenhum Pokémon encontrado. Limpe a pesquisa ou cadastre um Pokémon.</p><button onClick={() => {setQuery('');setType('')}}>Limpar pesquisa</button></div> : trainer ? <div className="trainer-view"><span className="identity">ARQUIVO DE TREINADOR / {String(trainer.id).padStart(2,'0')}</span><div className="trainer-emblem">{trainer.name.split(' ').map(n => n[0]).join('')}</div><h2>{trainer.name}</h2><p className="badges">✧ {trainer.badges} {trainer.badges === 1 ? 'INSÍGNIA' : 'INSÍGNIAS'}</p><h3>EQUIPE POKÉMON</h3><div className="team">{trainer.pokemonIds.map((id, i) => { const p = data.pokemon.find(p => p.id === id); if (!p) return <span key={`${id}-${i}`}>Pokémon #{id} não encontrado</span>; return <button key={`${id}-${i}`} onClick={() => openPokemon(id)}><PokemonImage pokemon={p}/><span>{p.name}</span></button> })}</div></div> : <div className="no-result"><h2>Nenhum treinador cadastrado.</h2></div>}</div><div className="screen-bottom"><span>{mode === 'pokemon' ? `${String(index + 1).padStart(3,'0')} / ${String(list.length).padStart(3,'0')}` : `${trainer?.id ?? 0} / ${data.trainers.length}`}</span><span>▰▰▰ BATTERY</span></div></div><div className="frame-bottom"><i className="red-light"/><span className="speaker" aria-hidden="true">━━━━<br/>━━━━<br/>━━━━</span></div></div><div className="record-actions" aria-label="Gerenciar Pokémon">
      <button onClick={() => { setNotice(''); setAction({ mode: 'create' }) }}>Cadastrar Pokémon</button>
      <button disabled={mode !== 'pokemon' || !selected} onClick={() => { setNotice(''); setAction({ mode: 'edit', pokemon: selected }) }}>Editar</button>
      <button disabled={mode !== 'pokemon' || !selected} onClick={() => { setNotice(''); setAction({ mode: 'delete', pokemon: selected }) }}>Excluir</button>
    </div><p className="mutation-notice" role="status">{notice}</p><div className="control-deck"><div className="controls-main"><div className="mode-switch" aria-label="Modo de consulta"><button className={mode === 'pokemon' ? 'mode active' : 'mode'} aria-pressed={mode === 'pokemon'} onClick={() => setMode('pokemon')}>POKÉMON</button><button className={mode === 'trainers' ? 'mode active' : 'mode'} aria-pressed={mode === 'trainers'} onClick={() => setMode('trainers')}>TREINADORES</button></div><div className="input-panel"><label htmlFor="search">BUSCAR NOME / Nº</label><input id="search" value={query} placeholder="Ex.: Pikachu ou 025" disabled={mode !== 'pokemon'} onChange={e => setQuery(e.target.value)}/><label htmlFor="type">FILTRO DE TIPO</label><select id="type" value={type} disabled={mode !== 'pokemon'} onChange={e => setType(e.target.value)}><option value="">Todos os tipos</option>{data.types.map(t => <option key={t.id}>{t.name}</option>)}</select></div></div><div className="navigation"><div className="dpad"><button className="up" aria-label="Registro anterior" onClick={() => move(-1)}>▲</button><button className="left" aria-label="Registro anterior" onClick={() => move(-1)}>◀</button><span/><button className="right" aria-label="Próximo registro" onClick={() => move(1)}>▶</button><button className="down" aria-label="Próximo registro" onClick={() => move(1)}>▼</button></div><span className="control-label">NAVEGAR</span><button disabled={!data.pokemon.length} className="random-button" onClick={() => {openPokemon(data.pokemon[Math.floor(Math.random()*data.pokemon.length)].id)}}>SCAN</button><span className="control-label">ALEATÓRIO</span></div></div><div className="index-panel"><label htmlFor="index">{mode === 'pokemon' ? 'ÍNDICE DE ESPÉCIES' : 'ÍNDICE DE TREINADORES'}</label><select id="index" aria-label={mode === 'pokemon' ? 'Selecionar Pokémon' : 'Selecionar treinador'} value={mode === 'pokemon' ? selected?.id ?? '' : trainer?.id ?? ''} onChange={e => mode === 'pokemon' ? setSelectedId(Number(e.target.value)) : setTrainerId(Number(e.target.value))}>{mode === 'pokemon' ? list.length ? list.map(p => <option key={p.id} value={p.id}>#{String(p.id).padStart(3,'0')} — {p.name}</option>) : <option value="">Nenhum resultado</option> : data.trainers.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}</select><span className="index-count" role="status">{mode === 'pokemon' ? `${list.length} REGISTROS ENCONTRADOS` : `${trainer?.pokemonIds.length ?? 0} POKÉMON NA EQUIPE`}</span></div><div className="device-foot"><span>Silph Co.</span><span>MODEL PX–151</span><span>◆</span></div></div><div className="hinge" aria-hidden="true"/></section><p className="desk-caption">POKÉDEX / REGIÃO DE KANTO<span>Pesquise, selecione e use o direcional para explorar.</span></p>{action && <PokemonDialog key={`${action.mode}-${action.pokemon?.id ?? "new"}`} action={action} types={data.types} onClose={() => setAction(null)} onSaved={saved}/>}</main>
}
