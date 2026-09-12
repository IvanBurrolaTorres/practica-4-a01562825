# Bitácora · Laboratorio 4 · Sabores en red

Iván Burrola Torres · A01562825

## Ejercicio 0 · Mapa de endpoints
| Endpoint | Pantalla o acción |
| --- | --- |
| GET /restaurants | Lista de restaurantes y sus promedios |
| GET /restaurants/3 | Información de Kaze en el detalle |
| GET /reviews?restaurantId=3 | Reseñas de Kaze en el detalle |
| POST /reviews | Publicar desde el formulario de nueva reseña |
| GET /me/reviews | Mis reseñas |
| DELETE /reviews/12 | Borrar una reseña propia desde Mis reseñas o el detalle |

## Ejercicio A2 · El promedio
La lista usa ratingAverage y ratingCount del servidor, traducidos a RatingSummary: una petición basta para todas las tarjetas. Pedir las reseñas de cada restaurante provocaría peticiones innecesarias.

El detalle calcula RatingSummary.from(reviews) con las reseñas que acaba de obtener. Al publicar, editar o borrar se consulta otra vez el servidor. Así la cifra corresponde a los datos que la pantalla muestra. RestaurantEnLista vive en domain para que data no dependa de ui. Ninguna clase del dominio importa Retrofit ni serialización.

## Ejercicio B1 · Estados de una pantalla con red
- Cargando: indicador y explicación de que se está consultando al servidor.
- Éxito con resultados: tarjetas con datos y promedios.
- Éxito vacío: mensaje de que todavía no hay datos. No significa que siga cargando.
- Error de conexión: mensaje y Reintentar.
- Error HTTP: mensaje según el código del servidor; 403/422 no se solucionan repitiendo exactamente la misma petición.

UiState es sellado, de modo que el when debe contemplar carga, éxito y error. IOException significa que no se pudo completar la comunicación; HttpException significa que sí hubo una respuesta HTTP no exitosa. CancellationException se propaga.

En la rama experimento-b1 se abrió la versión del checkpoint A4 sin conexión y se registró un cierre por excepción en Main. En main, B3 sustituyó ese comportamiento por el mensaje de conexión y Reintentar. La captura y las lecturas de pantalla documentan error y recuperación.

## Bloque C · Publicación observada
Desde la app se publicó la reseña de demostración con id 95 para el restaurante 5, firmada como a01562825. GET /me/reviews confirmó su existencia. Al regresar al detalle, el contador pasó de 7 a 8 reseñas. El texto identifica expresamente que es una demostración de laboratorio.

Se usa LifecycleEventEffect(ON_RESUME) para recargar al volver al destino: LaunchedEffect(id) por sí solo no garantiza otra carga si la composición se conserva en el back stack. La confirmación del guardado se conserva en el UiState del formulario y la navegación se realiza al observarla.
