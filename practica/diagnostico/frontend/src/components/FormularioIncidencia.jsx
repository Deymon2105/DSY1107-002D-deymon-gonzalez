import { useState } from 'react'
import Modal from './Modal'
import { ErrorApi } from '../api/cliente'

const VALORES_INICIALES = {
    titulo: '',
    descripcion: '',
    categoria: 'ACCESOS',
    prioridad: 'MEDIA',
    solicitante: ''
}

export default function formularioIncidencia({ modo, incidencia, catalogos, onGuardar, onCerrar }) {

    const [valores, setValores] = useState(() =>
        modo === 'editar'
            ? {
                titulo: incidencia.titulo,
                descripcion: incidencia.descripcion,
                categoria: incidencia.categoria,
                prioridad: incidencia.prioridad,
                solicitante: incidencia.solicitante
            }
            : VALORES_INICIALES
    )
    const [errores, setErrores] = useState({})
    const [errorGeneral, setErrorGeneral] = useState(null)
    const [guardando, setGuardando] = useState(false)

    function cambiar(campo) {
        return (evento) => {
            const valor = evento.target.value
            setValores((previos) => ({ ...previos, [campo]: valor }))
            setErrores((previos) => ({ ...previos, [campo]: undefined }))
        }
    }

    function validarEnCliente() {
        const encontrados = {}
        if (valores.titulo.trim().length < 5) {
            encontrados.titulo = 'El título debe tener al menos 5 caracteres.'
        }
        if (valores.descripcion.trim().length < 10) {
            encontrados.descripcion = 'La descripción debe tener al menos 10 caracteres.'
        }
        if (valores.solicitante.trim().length < 3) {
            encontrados.solicitante = 'Indique quién reporta la incidencia.'
        }
        return encontrados
    }

    async function enviar(evento) {
        evento.preventDefault()
        setErrorGeneral(null)

        const encontrados = validarEnCliente()
        if (Object.keys(encontrados).length > 0) {
            setErrores(encontrados)
            return
        }

        setGuardando(true)
        try {
            await onGuardar({
                titulo: valores.titulo.trim(),
                descripcion: valores.descripcion.trim(),
                categoria: valores.categoria,
                prioridad: valores.prioridad,
                solicitante: valores.solicitante.trim()
            })
        } catch (error) {
            if (error instanceof ErrorApi) {
                setErrores(error.porCampo())
                setErrorGeneral(error.mensaje)
            } else {
                setErrorGeneral('No fue posible guardar la incidencia.')
            }
        } finally {
            setGuardando(false)
        }
    }

    const titulo = modo === 'editar' ? `Editar incidencia #${incidencia.id}` : 'Nueva incidencia'

    return (
        <Modal titulo={titulo} onCerrar={onCerrar}>
            <form className="formulario" onSubmit={enviar} noValidate>
                {errorGeneral && <p className="formulario__error-general">{errorGeneral}</p>}

                <div className="campo">
                    <label className="campo__etiqueta" htmlFor="titulo">
                        Título
                    </label>
                    <input
                        id="titulo"
                        className="campo__control"
                        value={valores.titulo}
                        onChange={cambiar('titulo')}
                        placeholder="Resuma el problema en una línea"
                        maxLength={120}
                        autoFocus
                        aria-invalid={Boolean(errores.titulo)}
                    />
                    {errores.titulo && <span className="campo__error">{errores.titulo}</span>}
                </div>

                <div className="campo">
                    <label className="campo__etiqueta" htmlFor="descripcion">
                        Descripción
                    </label>
                    <textarea
                        id="descripcion"
                        className="campo__control campo__control--area"
                        value={valores.descripcion}
                        onChange={cambiar('descripcion')}
                        placeholder="Qué ocurre, desde cuándo, qué se intentó y a quién afecta"
                        rows={5}
                        maxLength={2000}
                        aria-invalid={Boolean(errores.descripcion)}
                    />
                    {errores.descripcion && <span className="campo__error">{errores.descripcion}</span>}
                </div>

                <div className="campo campo--doble">
                    <div>
                        <label className="campo__etiqueta" htmlFor="categoria">
                            Categoría
                        </label>
                        <select
                            id="categoria"
                            className="campo__control"
                            value={valores.categoria}
                            onChange={cambiar('categoria')}
                        >
                            {(catalogos?.categorias || []).map((opcion) => (
                                <option key={opcion.codigo} value={opcion.codigo}>
                                    {opcion.etiqueta}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div>
                        <label className="campo__etiqueta" htmlFor="prioridad">
                            Prioridad
                        </label>
                        <select
                            id="prioridad"
                            className="campo__control"
                            value={valores.prioridad}
                            onChange={cambiar('prioridad')}
                        >
                            {(catalogos?.prioridades || []).map((opcion) => (
                                <option key={opcion.codigo} value={opcion.codigo}>
                                    {opcion.etiqueta}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>

                <div className="campo">
                    <label className="campo__etiqueta" htmlFor="solicitante">
                        Solicitante
                    </label>
                    <input
                        id="solicitante"
                        className="campo__control"
                        value={valores.solicitante}
                        onChange={cambiar('solicitante')}
                        placeholder="Nombre de quien reporta"
                        maxLength={80}
                        aria-invalid={Boolean(errores.solicitante)}
                    />
                    {errores.solicitante && <span className="campo__error">{errores.solicitante}</span>}
                </div>

                {modo === 'crear' && (
                    <p className="formulario__nota">
                        La incidencia se registrará en estado <strong>Abierta</strong>. El estado inicial lo
                        define el backend y no se envía desde este formulario.
                    </p>
                )}

                <footer className="formulario__acciones">
                    <button type="button" className="boton boton--texto" onClick={onCerrar}>
                        Cancelar
                    </button>
                    <button type="submit" className="boton boton--primario" disabled={guardando}>
                        {guardando ? 'Guardando…' : modo === 'editar' ? 'Guardar cambios' : 'Registrar incidencia'}
                    </button>
                </footer>
            </form>
        </Modal>
    )
}
