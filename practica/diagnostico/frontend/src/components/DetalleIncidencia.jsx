import { useEffect, useState } from 'react'
import Modal from './Modal'
import MedidorPrioridad, { EtiquetaEstado } from './MedidorPrioridad'
import { api } from '../api/cliente'
import { etiquetaDe, fechaLarga } from '../formato'

export default function DetalleIncidencia({ id, catalogos, onCerrar }) {
  const [detalle, setDetalle] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let vigente = true
    setCargando(true)
    setError(null)

    api
      .detalle(id)
      .then((datos) => {
        if (vigente) setDetalle(datos)
      })
      .catch((problema) => {
        if (vigente) setError(problema.mensaje || 'No fue posible cargar la incidencia.')
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })

    return () => {
      vigente = false
    }
  }, [id])

  const incidencia = detalle?.incidencia
  const nivelPrioridad = (catalogos?.prioridades || []).find(
    (opcion) => opcion.codigo === incidencia?.prioridad
  )?.nivel

  return (
    <Modal titulo={`Incidencia #${id}`} onCerrar={onCerrar} ancho="ancho">
      {cargando && <p className="estado-vacio">Cargando incidencia…</p>}
      {error && <p className="formulario__error-general">{error}</p>}

      {incidencia && (
        <div className="detalle">
          <header className="detalle__cabecera">
            <h3 className="detalle__titulo">{incidencia.titulo}</h3>
            <EtiquetaEstado
              estado={incidencia.estado}
              etiqueta={etiquetaDe(catalogos?.estados, incidencia.estado)}
            />
          </header>

          <p className="detalle__descripcion">{incidencia.descripcion}</p>

          <dl className="detalle__datos">
            <div>
              <dt>Prioridad</dt>
              <dd>
                <MedidorPrioridad
                  prioridad={incidencia.prioridad}
                  nivel={nivelPrioridad}
                  etiqueta={etiquetaDe(catalogos?.prioridades, incidencia.prioridad)}
                />
              </dd>
            </div>
            <div>
              <dt>Categoría</dt>
              <dd>{etiquetaDe(catalogos?.categorias, incidencia.categoria)}</dd>
            </div>
            <div>
              <dt>Solicitante</dt>
              <dd>{incidencia.solicitante}</dd>
            </div>
            <div>
              <dt>Registrada</dt>
              <dd>{fechaLarga(incidencia.fechaCreacion)}</dd>
            </div>
            <div>
              <dt>Última actualización</dt>
              <dd>{fechaLarga(incidencia.fechaActualizacion)}</dd>
            </div>
            <div>
              <dt>Resuelta</dt>
              <dd>{fechaLarga(incidencia.fechaResolucion)}</dd>
            </div>
          </dl>

          <section className="historial">
            <h4 className="historial__titulo">Historial de estados</h4>
            <ol className="historial__lista">
              {(detalle.historial || []).map((cambio) => (
                <li key={cambio.id} className="historial__entrada">
                  <span className={`historial__punto estado--${cambio.estadoNuevo.toLowerCase()}`} />
                  <div className="historial__contenido">
                    <p className="historial__transicion">
                      {cambio.estadoAnterior
                        ? `${etiquetaDe(catalogos?.estados, cambio.estadoAnterior)} → ${etiquetaDe(
                            catalogos?.estados,
                            cambio.estadoNuevo
                          )}`
                        : `Registrada como ${etiquetaDe(catalogos?.estados, cambio.estadoNuevo)}`}
                    </p>
                    {cambio.comentario && (
                      <p className="historial__comentario">{cambio.comentario}</p>
                    )}
                    <p className="historial__fecha">{fechaLarga(cambio.fecha)}</p>
                  </div>
                </li>
              ))}
            </ol>
          </section>
        </div>
      )}
    </Modal>
  )
}