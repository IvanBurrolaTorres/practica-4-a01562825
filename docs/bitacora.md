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

## Ejercicio C2 · Validación del servidor
En la rama experimento-c2 se permitió enviar un comentario de tres letras, abc, manteniendo una puntuación válida. La app hizo POST /reviews y el servidor respondió HTTP 422. Se mostró su mensaje: «comment debe tener al menos 15 caracteres». El formulario quedó abierto, conservó abc, volvió a habilitar Publicar y siguió utilizable. El log HTTP y la captura están en docs/evidencias/c2-*. En main se conserva ReviewValidator.

El cliente valida para dar ayuda inmediata; el servidor valida para todos los clientes, incluidos los que omiten deliberadamente las reglas. Esa doble validación responde a responsabilidades diferentes.

## Bloque D · PATCH, DELETE y propiedad
Desde Mis reseñas se editó la reseña #95, cambiando de 4 a 2 estrellas y conservando exactamente el comentario. PATCH envía solo las propiedades modificadas: los valores nulos no se serializan. La API confirmó el cambio.

Después se borró #95 desde la app; GET /me/reviews devolvió [] y la pantalla mostró que no había reseñas propias. El borrado solo navega/actualiza tras la confirmación, y no se elimina una tarjeta de forma optimista.

La demostración de permisos obtuvo una reseña cuyo autor es profesor (#2, restaurante 1) y solicitó DELETE con X-Alumno: a01562825. El servidor respondió HTTP 403. La app mostró el rechazo, permaneció utilizable y una lectura posterior confirmó que #2 seguía existiendo. El menú para esta comprobación solo se muestra en la versión debug del laboratorio. Los controles normales de editar/borrar solo se muestran cuando author coincide con la matrícula; además los ViewModel comprueban la propiedad antes de enviar.

## Decisiones y límites
- El formulario conserva el borrador en su ViewModel al girar. El éxito se representa en el estado y la navegación observa esa confirmación.
- Durante una escritura se ignoran cambios del formulario y se bloquea otro envío antes de iniciar la corrutina. No se reintenta automáticamente una escritura cuya respuesta se perdió: el mensaje pide revisar Mis reseñas antes de reenviar.
- Cada carga cancela la anterior de su mismo tipo para evitar que una respuesta vieja reemplace datos de una petición más reciente.
- La lista, el detalle y Mis reseñas se recargan al reanudar el destino; un cambio en otro teléfono requiere actualizar, porque la API no ofrece notificaciones en tiempo real.
- No hay base de datos local ni pruebas automatizadas. Hay compilación, análisis estático y comprobaciones interactivas contra la API real.

## Registro del trabajo
Se conservó el commit inicial del repositorio del profesor. Cada checkpoint corresponde a cambios y comprobaciones realizadas durante esta preparación; las fechas del historial son las reales y no se ajustaron para aparentar una sesión de dos horas. La implementación y estas comprobaciones se realizaron con asistencia de Codex. No se atribuyen al estudiante experimentos que no haya realizado personalmente.
