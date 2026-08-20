export default function BarraFiltros({ catalogos, filtros, onCambiar, onLimpiar, onNueva }) {
  const hayFiltros =
    filtros.buscar || filtros.estado || filtros.prioridad || filtros.categoria

  function cambiar(campo) {
    return (evento) => onCambiar(campo, evento.target.value)
  }

  return (
    <section className="filtros" aria-label="Filtros de búsqueda">
      <div className="filtros__busqueda">
        <label className="campo__etiqueta" htmlFor="filtro-buscar">
          Buscar
        </label>
        <input
          id="filtro-buscar"
          type="search"
          className="campo__control"
          placeholder="Título, descripción o solicitante"
          value={filtros.buscar}
          onChange={cambiar('buscar')}
        />
      </div>

      <div className="filtros__campo">
        <label className="campo__etiqueta" htmlFor="filtro-estado">
          Estado
        </label>
        <select
          id="filtro-estado"
          className="campo__control"
          value={filtros.estado}
          onChange={cambiar('estado')}
        >
          <option value="">Todos</option>
          {(catalogos?.estados || []).map((opcion) => (
            <option key={opcion.codigo} value={opcion.codigo}>
              {opcion.etiqueta}
            </option>
          ))}
        </select>
      </div>

      <div className="filtros__campo">
        <label className="campo__etiqueta" htmlFor="filtro-prioridad">
          Prioridad
        </label>
        <select
          id="filtro-prioridad"
          className="campo__control"
          value={filtros.prioridad}
          onChange={cambiar('prioridad')}
        >
          <option value="">Todas</option>
          {(catalogos?.prioridades || []).map((opcion) => (
            <option key={opcion.codigo} value={opcion.codigo}>
              {opcion.etiqueta}
            </option>
          ))}
        </select>
      </div>

      <div className="filtros__campo">
        <label className="campo__etiqueta" htmlFor="filtro-categoria">
          Categoría
        </label>
        <select
          id="filtro-categoria"
          className="campo__control"
          value={filtros.categoria}
          onChange={cambiar('categoria')}
        >
          <option value="">Todas</option>
          {(catalogos?.categorias || []).map((opcion) => (
            <option key={opcion.codigo} value={opcion.codigo}>
              {opcion.etiqueta}
            </option>
          ))}
        </select>
      </div>

      <div className="filtros__campo">
        <label className="campo__etiqueta" htmlFor="filtro-orden">
          Ordenar por
        </label>
        <select
          id="filtro-orden"
          className="campo__control"
          value={filtros.orden}
          onChange={cambiar('orden')}
        >
          {(catalogos?.ordenamientos || []).map((opcion) => (
            <option key={opcion.codigo} value={opcion.codigo}>
              {opcion.etiqueta}
            </option>
          ))}
        </select>
      </div>

      <div className="filtros__acciones">
        {hayFiltros && (
          <button type="button" className="boton boton--texto" onClick={onLimpiar}>
            Limpiar filtros
          </button>
        )}
        <button type="button" className="boton boton--primario" onClick={onNueva}>
          Nueva incidencia
        </button>
      </div>
    </section>
  )
}
