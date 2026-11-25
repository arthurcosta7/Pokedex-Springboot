import { useEffect, useRef, useState } from 'react'
import { deletePokemon, savePokemon } from '../services/pokedex.js'

const attributes = {
  hp: 'HP', attack: 'Ataque', defense: 'Defesa',
  specialAttack: 'Ataque especial', specialDefense: 'Defesa especial', speed: 'Velocidade',
}

export default function PokemonDialog({ action, types, onClose, onSaved }) {
  const dialog = useRef(null)
  const pokemon = action.pokemon
  const removing = action.mode === 'delete'
  const editing = action.mode === 'edit'
  const [pending, setPending] = useState(false)
  const [error, setError] = useState('')
  const [typeIds, setTypeIds] = useState(pokemon?.types.map(t => t.id) ?? [])
  const title = removing ? 'Excluir Pokémon' : editing ? 'Editar Pokémon' : 'Cadastrar Pokémon'

  useEffect(() => { dialog.current.showModal() }, [])

  async function submit(event) {
    event.preventDefault()
    if (pending) return
    setError('')
    const fields = new FormData(event.currentTarget)
    const payload = removing ? null : {
      id: editing ? pokemon.id : Number(fields.get('id')),
      name: fields.get('name').trim(),
      stats: Object.fromEntries(Object.keys(attributes).map(key => [key, Number(fields.get(key))])),
      typeIds,
    }
    if (payload && !payload.name) { setError('Informe o nome do Pokémon.'); return }
    if (payload && typeIds.length > 2) { setError('Selecione até dois tipos.'); return }
    setPending(true)
    try {
      const result = removing ? await deletePokemon(pokemon.id) : await savePokemon(payload, editing)
      onSaved(result, action)
    } catch (err) {
      setError(err.message)
      setPending(false)
    }
  }

  function toggleType(id) {
    setTypeIds(current => current.includes(id) ? current.filter(value => value !== id) : [...current, id])
  }

  return <dialog ref={dialog} className="pokemon-dialog" aria-labelledby="dialog-title"
    onCancel={event => { event.preventDefault(); if (!pending) onClose() }}>
    <form onSubmit={submit} aria-busy={pending}>
      <h2 id="dialog-title">{title}</h2>
      {removing ? <p>Excluir <strong>#{pokemon.id} — {pokemon.name}</strong>? Ele também será removido das equipes dos treinadores.</p> : <>
        <div className="form-fields">
          <label htmlFor="pokemon-id">Número da Pokédex<input id="pokemon-id" name="id" type="number" min="1" max="2147483647" step="1" defaultValue={pokemon?.id ?? ''} disabled={editing || pending} required autoFocus={!editing}/></label>
          <label htmlFor="pokemon-name">Nome<input id="pokemon-name" name="name" maxLength="100" defaultValue={pokemon?.name ?? ''} required disabled={pending} autoFocus={editing}/></label>
        </div>
        <fieldset disabled={pending}>
          <legend>Atributos (0 a 255)</legend>
          <div className="form-fields">{Object.entries(attributes).map(([key, label]) =>
            <label key={key} htmlFor={`stat-${key}`}>{label}<input id={`stat-${key}`} name={key} type="number" min="0" max="255" step="1" defaultValue={pokemon?.stats[key] ?? 0} required/></label>)}</div>
        </fieldset>
        <fieldset disabled={pending}>
          <legend>Tipos (até dois)</legend>
          <div className="type-options">{types.map(type => <label key={type.id}>
            <input type="checkbox" checked={typeIds.includes(type.id)} onChange={() => toggleType(type.id)}
              disabled={!typeIds.includes(type.id) && typeIds.length >= 2}/>{type.name}
          </label>)}</div>
          {!types.length && <p>Nenhum tipo cadastrado no banco.</p>}
        </fieldset>
      </>}
      {error && <p className="form-error" role="alert">{error}</p>}
      <div className="dialog-actions">
        <button type="button" onClick={onClose} disabled={pending}>Cancelar</button>
        <button type="submit" className={removing ? 'danger' : 'primary'} disabled={pending}>{pending ? 'Aguarde…' : removing ? 'Confirmar exclusão' : 'Salvar Pokémon'}</button>
      </div>
    </form>
  </dialog>
}
