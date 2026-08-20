import { useEffect, useRef } from 'react'

/* Ventana modal */

export default function Modal({ titulo, children, onCerrar, ancho = 'normal' }) {
    const contenedor = useRef(null)

    useEffect(() => {
        function alPresionar(evento) {
            if (evento.key === 'Escape') onCerrar()
        }
        document.addEventListener('keydown', alPresionar)
        document.body.style.overflow = 'hidden'
        contenedor.current?.focus()

        return () => {
            document.removeEventListener('keydown', alPresionar)
            document.body.style.overflow = ''
        }
    }, [onCerrar])

    return (
        <div className="modal__fondo" onMouseDown={(e) => e.target === e.currentTarget && onCerrar()}>
            <div
                className={`modal modal--${ancho}`}
                role="dialog"
                aria-modal="true"
                aria-label={titulo}
                tabIndex={-1}
                ref={contenedor}
            >
                <header className="modal__cabecera">
                    <h2 className="modal__titulo">{titulo}</h2>
                    <button type="button" className="modal__cerrar" onClick={onCerrar} aria-label="Cerrar">
                        ×
                    </button>
                </header>
                <div className="modal__cuerpo">{children}</div>
            </div>
        </div>
    )
}
