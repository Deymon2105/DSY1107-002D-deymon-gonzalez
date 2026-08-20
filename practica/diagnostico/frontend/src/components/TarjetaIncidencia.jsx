import MedidorPrioridad, { EtiquetaEstado } from './MedidorPrioridad'
import { claseEstado, etiquetaDe, tiempoRelativo } from '../formato'

const ACCION_POR_ESTADO = {
    EN_PROGRESO: 'Tomar',
    RESUELTA: 'Resolver',
    ABIERTA: 'Reabrir'
}

export default function TarjetaIncidencia({
    incidencia,
    catalogos,
    ocupada,
    onVer,
    onEditar,
    onEliminar,
    onCambiarEstado
}) {
    const {
        id,
        titulo,
        descripcion,
        estado,
        prioridad,
        categoria,
        solicitante,
        fechaCreacion,
        transicionesPermitidas = []
    } = incidencia

    const nivelPrioridad = (catalogos?.prioridades || []).find(
        (opcion) => opcion.codigo === prioridad
    )?.nivel

    return (
        <article className={`incidencia ${claseEstado(estado)}`} aria-busy={ocupada}>
        <div className="incidencia__cabecera">
            <span className="incidencia__id">#{id}</span>
            <h3 className="incidencia__titulo">
            <button type="button" className="enlace" onClick={() => onVer(id)}>
                {titulo}
            </button>
            </h3>
            <EtiquetaEstado estado={estado} etiqueta={etiquetaDe(catalogos?.estados, estado)} />
        </div>

        <p className="incidencia__descripcion">{descripcion}</p>

        <div className="incidencia__meta">
            <MedidorPrioridad
            prioridad={prioridad}
            nivel={nivelPrioridad}
            etiqueta={etiquetaDe(catalogos?.prioridades, prioridad)}
            />
            <span className="incidencia__dato">{etiquetaDe(catalogos?.categorias, categoria)}</span>
            <span className="incidencia__dato">{solicitante}</span>
            <span className="incidencia__dato incidencia__dato--tenue">
            {tiempoRelativo(fechaCreacion)}
            </span>
        </div>

        <div className="incidencia__acciones">
            {transicionesPermitidas.map((destino) => (
            <button
                key={destino}
                type="button"
                className="boton boton--secundario"
                disabled={ocupada}
                onClick={() => onCambiarEstado(incidencia, destino)}
            >
                {ACCION_POR_ESTADO[destino] || destino}
            </button>
            ))}
            <span className="incidencia__separador" />
            <button type="button" className="boton boton--texto" onClick={() => onVer(id)}>
            Ver detalle
            </button>
            <button
            type="button"
            className="boton boton--texto"
            disabled={ocupada || transicionesPermitidas.length === 0}
            title={
                transicionesPermitidas.length === 0
                ? 'Una incidencia resuelta ya no se puede editar'
                : undefined
            }
            onClick={() => onEditar(incidencia)}
            >
            Editar
            </button>
            <button
            type="button"
            className="boton boton--texto boton--peligro"
            disabled={ocupada}
            onClick={() => onEliminar(incidencia)}
            >
            Eliminar
            </button>
        </div>
        </article>
    )
}
