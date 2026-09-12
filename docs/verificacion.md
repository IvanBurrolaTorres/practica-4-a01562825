# Verificación de la entrega

## Herramientas
Android Studio/JDK integrado, Gradle Wrapper 9.7.1 y emulador Medium Phone. Compilación con SDK 37.

`./gradlew :app:assembleDebug :app:lintDebug :app:assembleRelease --offline --console=plain`

Resultado: BUILD SUCCESSFUL; lint: No issues found. Se verificó también que ningún archivo de domain importe Retrofit o kotlinx.serialization. No se agregaron pruebas automatizadas.

## Operaciones comprobadas contra la API real
| Operación | Resultado observado |
| --- | --- |
| Lista | Cinco restaurantes desde GET /restaurants, con promedios del servidor |
| Detalle | Datos y reseñas desde sus endpoints; promedio calculado con RatingSummary |
| POST | Reseña #95 de a01562825 creada desde el formulario |
| PATCH | #95 pasó de 4 a 2 estrellas sin cambiar el comentario |
| DELETE propio | #95 eliminada; GET /me/reviews confirmó [] |
| Sin conexión | Modo avión y radios desactivados: mensaje y Reintentar; recuperación al volver a conectar |
| HTTP 422 | Comentario abc enviado en experimento-c2; servidor lo rechazó y el formulario siguió abierto |
| HTTP 403 | DELETE de #2, autor profesor, rechazado; GET posterior confirmó que seguía existiendo |
| Publicación del video | Reseña #96, 5 estrellas, creada con a01562825 desde la app; permanece en el servidor como evidencia |

La reseña del video se identifica como demostración del laboratorio. El promedio de Brasa 33 pasó de 3.9 (7 reseñas) a 4.0 (8 reseñas) durante esa grabación. La API es compartida, por lo que estas cifras son una observación de ese momento.

## Video
`entregables/Laboratorio_4_Sabores_en_red.mp4`: 60 segundos. Muestra lista real, creación de #96, modo avión con error, reintento y rechazo 403 desde la app. La grabación se aceleró uniformemente aproximadamente 1.22× para ajustar su duración, conservando todos los eventos, y se prolongó el fotograma final para llegar a 60 segundos. No se simularon respuestas del servidor.

## Evidencias y alcance
Los XML, capturas, respuestas JSON y logs en docs/evidencias documentan estas observaciones. Las ramas de experimentos contienen el comportamiento que se cambió a propósito; main conserva la validación y el manejo de errores.

Esta verificación fue realizada por el asistente en el emulador. No sustituye la defensa oral ni afirma que el estudiante haya ejecutado personalmente una sesión de dos horas. Las fechas de Git son las reales.
