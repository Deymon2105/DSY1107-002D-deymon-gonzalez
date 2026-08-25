# Actividad 1.1.2 - Creando primer API Manager en AWS

### 1. Creación de API Gateway

Se crea un nuevo API Gateway en AWS.

![Creación de API Gateway](evidencias/creacion-api-gateway.png)

### 2. Configuración de rutas

Se configura una ruta con método GET y endpoint `/datos`.

![Configuración de rutas: Método GET + ruta /datos](evidencias/config-rutas-api-gateway.png)

### 3. Asociar integración

Se asocia la integración apuntando a la API que se desea proteger.

![Detalles de la integración](evidencias/detalle-integraciones.png)

### 4. Detalles de la integración

Se revisan los detalles de la integración configurada.

![Asociar integración: API que vamos a proteger](evidencias/integraciones.png)

### 5. Detalles de la API Gateway creada

Se verifica la configuración completa de la API Gateway.

![Detalles de la API Gateway creada](evidencias/detalles-api-gateway.png)

### 6. Creación de Stage para el deploy

Se crea un Stage para realizar el despliegue de la API.

![Creación de Stage para el deploy de la API](evidencias/creacion-deploy-api-gateway.png)

### 7. Pruebas de la API implementada en Postman

Se prueba el endpoint de la API Gateway con el método GET

![Implementación de pruebas de la API con Postman](evidencias/pruebas-postman-api-gateway.png)

---

# Actividad 1.1.4 - Configurando CORS en el API Gateway


## Configuración de CORS

Se habilitan las configuraciones principales de CORS en la API Gateway para permitir el acceso desde el navegador.

![Configuraciones principales de CORS](evidencias/configuracion-cors.png)
