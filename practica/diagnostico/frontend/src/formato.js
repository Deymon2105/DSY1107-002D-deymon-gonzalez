const MINUTO = 60_000
const HORA = 60 * MINUTO
const DIA = 24 * HORA

/** Busca la etiqueta legible de un código dentro de un catálogo. */
export function etiquetaDe(opciones, codigo) {
  if (!codigo) return '—'
  const encontrada = (opciones || []).find((opcion) => opcion.codigo === codigo)
  return encontrada?.etiqueta || codigo
}

/** Clase CSS del estado: ABIERTA → estado--abierta */
export function claseEstado(estado) {
  if (!estado) return ''
  return `estado--${String(estado).toLowerCase()}`
}

function aFecha(valor) {
  if (!valor) return null
  const fecha = new Date(valor)
  return Number.isNaN(fecha.getTime()) ? null : fecha
}

/** "hace 3 h", "hace 2 días", etc. */
export function tiempoRelativo(valor) {
  const fecha = aFecha(valor)
  if (!fecha) return '—'

  const diferencia = Date.now() - fecha.getTime()
  if (diferencia < MINUTO) return 'hace instantes'
  if (diferencia < HORA) {
    const minutos = Math.floor(diferencia / MINUTO)
    return `hace ${minutos} min`
  }
  if (diferencia < DIA) {
    const horas = Math.floor(diferencia / HORA)
    return `hace ${horas} h`
  }
  const dias = Math.floor(diferencia / DIA)
  if (dias < 30) return `hace ${dias} ${dias === 1 ? 'día' : 'días'}`
  return fechaCorta(valor)
}

/** "11 ago 2026" */
export function fechaCorta(valor) {
  const fecha = aFecha(valor)
  if (!fecha) return '—'
  return fecha.toLocaleDateString('es-CL', {
    day: '2-digit',
    month: 'short',
    year: 'numeric'
  })
}

/** "11 de agosto de 2026, 14:35" */
export function fechaLarga(valor) {
  const fecha = aFecha(valor)
  if (!fecha) return '—'
  return fecha.toLocaleString('es-CL', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
