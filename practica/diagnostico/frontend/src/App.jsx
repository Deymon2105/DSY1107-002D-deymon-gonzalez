import { useCallback, useEffect, useRef, useState } from 'react'
import { api } from './api/cliente'
import Avisos from './components/Avisos'
import BarraFiltros from './components/BarraFiltros'
import DetalleIncidencia from './components/DetalleIncidencia'
import FormularioIncidencia from './components/FormularioIncidencia'
import ResumenEstados from './components/ResumenEstados'
import TarjetaIncidencia from './components/TarjetaIncidencia'
import { etiquetaDe } from './formato'

const FILTROS_INICIALES = {
  buscar: '',
  estado: '',
  prioridad: '',
  categoria: '',
  orden: 'RECIENTES'
}

const RETARDO_BUSQUEDA_MS = 350

export default function App() {
  const [catalogos, setCatalogos] = useState(null)
  const [incidencias, setIncidencias] = useState([])
  const [resumen, setResumen] = useState(null)
  const [filtros, setFiltros] = useState(FILTROS_INICIALES)
  const [busquedaAplicada, setBusquedaAplicada] = useState('')
  const [cargando, setCargando] = useState(true)
  const [errorGlobal, setErrorGlobal] = useState(null)
  const [avisos, setAvisos] = useState([])
  const [formulario, setFormulario] = useState(null)
  const [detalleId, setDetalleId] = useState(null)
  const [idOcupado, setIdOcupado] = useState(null)

  const contadorAvisos = useRef(0)

  const publicarAviso = useCallback((tipo, texto) => {
    const id = ++contadorAvisos.current
    setAvisos((previos) => [...previos, { id, tipo, texto }])
    setTimeout(() => {
      setAvisos((previos) => previos.filter((aviso) => aviso.id !== id))
    }, 6000)
  }, [])

  const cerrarAviso = useCallback((id) => {
    setAvisos((previos) => previos.filter((aviso) => aviso.id !== id))
  }, [])

  useEffect(() => {
    api.catalogos().then(setCatalogos).catch(() => setCatalogos(null))
  }, [])

  useEffect(() => {
    const temporizador = setTimeout(() => setBusquedaAplicada(filtros.buscar), RETARDO_BUSQUEDA_MS)
    return () => clearTimeout(temporizador)
  }, [filtros.buscar])

  const cargar = useCallback(async () => {
    setCargando(true)
    setErrorGlobal(null)
    try {
      const [lista, totales] = await Promise.all([
        api.listar({
          buscar: busquedaAplicada,
          estado: filtros.estado,
          prioridad: filtros.prioridad,
          categoria: filtros.categoria,
          orden: filtros.orden
        }),
        api.resumen()
      ])
      setIncidencias(lista)
      setResumen(totales)
    } catch (error) {
      setErrorGlobal(error.mensaje || 'No fue posible cargar las incidencias.')
    } finally {
      setCargando(false)
    }
  }, [busquedaAplicada, filtros.estado, filtros.prioridad, filtros.categoria, filtros.orden])

  useEffect(() => {
    cargar()
  }, [cargar])

  function cambiarFiltro(campo, valor) {
    setFiltros((previos) => ({ ...previos, [campo]: valor }))
  }


  async function guardar(datos) {
    if (formulario.modo === 'crear') {
      const creada = await api.crear(datos)
      publicarAviso('exito', `Incidencia #${creada.incidencia.id} registrada correctamente.`)
    } else {
      await api.actualizar(formulario.incidencia.id, datos)
      publicarAviso('exito', `Incidencia #${formulario.incidencia.id} actualizada.`)
    }
    setFormulario(null)
    await cargar()
  }

  async function cambiarEstado(incidencia, nuevoEstado) {
    setIdOcupado(incidencia.id)
    try {
      await api.cambiarEstado(incidencia.id, nuevoEstado)
      publicarAviso(
        'exito',
        `Incidencia #${incidencia.id} pasó a ${etiquetaDe(catalogos?.estados, nuevoEstado)}.`
      )
      await cargar()
    } catch (error) {
      publicarAviso('error', error.mensaje || 'No fue posible cambiar el estado.')
    } finally {
      setIdOcupado(null)
    }
  }

  async function eliminar(incidencia) {
    const confirmado = window.confirm(
      `¿Eliminar la incidencia #${incidencia.id}?\n\n"${incidencia.titulo}"\n\nEsta acción no se puede deshacer.`
    )
    if (!confirmado) return

    setIdOcupado(incidencia.id)
    try {
      await api.eliminar(incidencia.id)
      publicarAviso('exito', `Incidencia #${incidencia.id} eliminada.`)
      await cargar()
    } catch (error) {
      publicarAviso('error', error.mensaje || 'No fue posible eliminar la incidencia.')
    } finally {
      setIdOcupado(null)
    }
  }

  const hayFiltrosActivos = Boolean(
    filtros.buscar || filtros.estado || filtros.prioridad || filtros.categoria
  )

  return (
    <div className="aplicacion">
      <header className="cabecera">
        <div className="cabecera__marca">
          <span className="cabecera__eyebrow">Soporte interno</span>
          <h1 className="cabecera__titulo">Mini Help Desk</h1>
        </div>
        <p className="cabecera__bajada">
          Registro y seguimiento de incidencias técnicas de la organización.
        </p>
      </header>

      <main className="contenido">
        <ResumenEstados resumen={resumen} cargando={cargando && !resumen} />

        <BarraFiltros
          catalogos={catalogos}
          filtros={filtros}
          onCambiar={cambiarFiltro}
          onLimpiar={() => setFiltros(FILTROS_INICIALES)}
          onNueva={() => setFormulario({ modo: 'crear' })}
        />

        {errorGlobal && (
          <div className="alerta">
            <p>{errorGlobal}</p>
            <button type="button" className="boton boton--secundario" onClick={cargar}>
              Reintentar
            </button>
          </div>
        )}

        <section className="listado" aria-label="Listado de incidencias" aria-busy={cargando}>
          <div className="listado__cabecera">
            <h2 className="listado__titulo">Incidencias</h2>
            <span className="listado__conteo">
              {cargando ? 'Cargando…' : `${incidencias.length} en pantalla`}
            </span>
          </div>

          {!cargando && incidencias.length === 0 && !errorGlobal && (
            <p className="estado-vacio">
              {hayFiltrosActivos
                ? 'Ninguna incidencia coincide con los filtros aplicados. Pruebe con otros criterios o limpie los filtros.'
                : 'Todavía no hay incidencias registradas. Cree la primera con el botón “Nueva incidencia”.'}
            </p>
          )}

          {incidencias.map((incidencia) => (
            <TarjetaIncidencia
              key={incidencia.id}
              incidencia={incidencia}
              catalogos={catalogos}
              ocupada={idOcupado === incidencia.id}
              onVer={setDetalleId}
              onEditar={(elegida) => setFormulario({ modo: 'editar', incidencia: elegida })}
              onEliminar={eliminar}
              onCambiarEstado={cambiarEstado}
            />
          ))}
        </section>
      </main>

      <footer className="pie">
        <span>DSY1107 · Desarrollo Cloud Native I · Diagnóstico fullstack</span>
        <span className="pie__stack">React + Vite · Spring Boot 4 · PostgreSQL 16 · Docker</span>
      </footer>

      {formulario && (
        <FormularioIncidencia
          modo={formulario.modo}
          incidencia={formulario.incidencia}
          catalogos={catalogos}
          onGuardar={guardar}
          onCerrar={() => setFormulario(null)}
        />
      )}

      {detalleId && (
        <DetalleIncidencia
          id={detalleId}
          catalogos={catalogos}
          onCerrar={() => setDetalleId(null)}
        />
      )}

      <Avisos avisos={avisos} onCerrar={cerrarAviso} />
    </div>
  )
}
