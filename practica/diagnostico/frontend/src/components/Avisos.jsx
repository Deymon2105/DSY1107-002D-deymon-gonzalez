export default function Avisos({ avisos, onCerrar }) {
  if (avisos.length === 0) return null

  return (
    <div className="avisos" role="status" aria-live="polite">
      {avisos.map((aviso) => (
        <div key={aviso.id} className={`aviso aviso--${aviso.tipo}`}>
          <span className="aviso__texto">{aviso.texto}</span>
          <button
            type="button"
            className="aviso__cerrar"
            onClick={() => onCerrar(aviso.id)}
            aria-label="Cerrar aviso"
          >
            ×
          </button>
        </div>
      ))}
    </div>
  )
}
