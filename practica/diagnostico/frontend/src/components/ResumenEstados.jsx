export default function ResumenEstados({ resumen, cargando }) {
    const tarjetas = [
        { clave: 'abiertas', titulo: 'Abiertas', modificador: 'abierta' },
        { clave: 'enProgreso', titulo: 'En progreso', modificador: 'en_progreso' },
        { clave: 'resueltas', titulo: 'Resueltas', modificador: 'resuelta' },
        { clave: 'total', titulo: 'Total', modificador: 'total' }
    ]

    return (
        <section className="resumen" aria-label="Resumen de incidencias por estado">
        {tarjetas.map(({ clave, titulo, modificador }) => (
            <article key={clave} className={`resumen__tarjeta resumen__tarjeta--${modificador}`}>
            <span className="resumen__cifra">
                {cargando || !resumen ? '·' : resumen[clave]}
            </span>
            <span className="resumen__titulo">{titulo}</span>
            </article>
        ))}
        </section>
    )
}