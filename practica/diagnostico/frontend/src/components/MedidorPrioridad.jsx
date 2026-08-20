import { claseEstado } from '../formato'

export default function MedidorPrioridad({ prioridad, nivel, etiqueta }) {
    const alto = nivel || 1
    const texto = etiqueta || prioridad

    return (
        <span
        className={`medidor medidor--${String(prioridad).toLowerCase()}`}
        title={`Prioridad ${texto}`}
        >
        <span className="medidor__barras" aria-hidden="true">
            {[1, 2, 3].map((paso) => (
            <i
                key={paso}
                className={paso <= alto ? 'medidor__barra medidor__barra--activa' : 'medidor__barra'}
            />
            ))}
        </span>
        <span className="medidor__texto">{texto}</span>
        </span>
    )
}

export function EtiquetaEstado({ estado, etiqueta }) {
    return <span className={`pildora ${claseEstado(estado)}`}>{etiqueta || estado}</span>
}