const BASE = import.meta.env.VITE_API_BASE_URL || '/api'

export class ErrorApi extends Error {
  constructor(mensaje, estado = 0, detalles = []) {
    super(mensaje)
    this.name = 'ErrorApi'
    this.mensaje = mensaje
    this.estado = estado
    this.detalles = detalles
  }

  porCampo() {
    return this.detalles.reduce((acumulado, detalle) => {
      if (detalle?.campo) acumulado[detalle.campo] = detalle.mensaje
      return acumulado
    }, {})
  }
}

async function peticion(ruta, opciones = {}) {
  let respuesta
  try {
    respuesta = await fetch(`${BASE}${ruta}`, {
      ...opciones,
      headers: { 'Content-Type': 'application/json', ...(opciones.headers || {}) }
    })
  } catch {
    throw new ErrorApi(
      'No se pudo contactar al servidor. Verifique que el backend esté levantado.'
    )
  }

  if (respuesta.status === 204) return null

  const texto = await respuesta.text()
  let cuerpo = null
  if (texto) {
    try {
      cuerpo = JSON.parse(texto)
    } catch {
      cuerpo = null
    }
  }

  if (!respuesta.ok) {
    throw new ErrorApi(
      cuerpo?.mensaje || `La solicitud falló con código ${respuesta.status}.`,
      respuesta.status,
      cuerpo?.detalles || []
    )
  }

  return cuerpo
}


function consulta(parametros) {
  const query = new URLSearchParams()
  Object.entries(parametros).forEach(([clave, valor]) => {
    if (valor !== undefined && valor !== null && String(valor).trim() !== '') {
      query.append(clave, String(valor).trim())
    }
  })
  const cadena = query.toString()
  return cadena ? `?${cadena}` : ''
}

export const api = {
  catalogos: () => peticion('/catalogos'),
  listar: (filtros) => peticion(`/incidencias${consulta(filtros)}`),
  resumen: () => peticion('/incidencias/resumen'),
  detalle: (id) => peticion(`/incidencias/${id}`),
  
  crear: (datos) =>
    peticion('/incidencias', { method: 'POST', body: JSON.stringify(datos) }),
  
  actualizar: (id, datos) =>
    peticion(`/incidencias/${id}`, { method: 'PUT', body: JSON.stringify(datos) }),
  
  cambiarEstado: (id, nuevoEstado, comentario) =>
    peticion(`/incidencias/${id}/estado`, {
      method: 'PATCH',
      body: JSON.stringify({ nuevoEstado, comentario })
    }),

  eliminar: (id) => peticion(`/incidencias/${id}`, { method: 'DELETE' })
}
