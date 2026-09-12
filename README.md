# Laboratorio 4 · Sabores en red

**Iván Burrola Torres · A01562825**

TC2007B · Kotlin y Jetpack Compose

Aplicación basada en el [proyecto inicial del curso](https://github.com/rpversontec/practica-4-sabores-red), con consultas, publicación, edición y borrado de reseñas contra `https://startdroid.com/api/`.

## Ejecutar
1. Abre esta carpeta en Android Studio y espera a que Gradle sincronice.
2. Usa el JDK integrado de Android Studio. La configuración utiliza AGP 9.3.1, Kotlin 2.4.10, Gradle 9.7.1 y SDK 37.
3. Ejecuta `app` en un emulador o dispositivo Android 7 o posterior con internet.

También puedes instalar [entregables/Sabores-en-red-debug.apk](entregables/Sabores-en-red-debug.apk) desde la copia local/ZIP. El APK no se versiona en Git.

La matrícula ya está configurada como `a01562825` en `data/remote/Network.kt`. Cada solicitud lleva X-Alumno. No es autenticación fuerte: es el mecanismo de propiedad definido por esta API educativa.

## Funcionalidad
- Lista y detalle obtenidos del servidor, con promedio y número de reseñas.
- Estados explícitos de carga, éxito (incluido vacío) y error, con reintento.
- Publicación con 1 a 5 estrellas y comentario de 15 a 240 caracteres, validado después de recortar espacios.
- Formulario conservado tras un error; botón bloqueado mientras guarda.
- Mis reseñas: edición con PATCH y borrado con confirmación.
- Controles de edición/borrado únicamente para la matrícula propia.
- Actualización de lista, detalle y Mis reseñas al regresar a la pantalla.
- Errores HTTP 401, 403, 404 y 422 con mensajes útiles; errores de conexión y formato manejados por separado.

## Entregables
- [Bitácora](docs/bitacora.md): ejercicios 0, A2, B1 y C2.
- [Video de 60 segundos](entregables/Laboratorio_4_Sabores_en_red.mp4).
- [Verificación](docs/verificacion.md) y [evidencias](docs/evidencias).
- Seis commits de checkpoint en main, además del commit original del profesor.

| Checkpoint | Cambio |
| --- | --- |
| checkpoint a1: gradle sincroniza | Dependencias, serialización, permiso de internet y wrapper |
| checkpoint a3: capa de red lista | DTO, mapeadores, interfaz Retrofit e interceptor con matrícula |
| checkpoint a4: lista desde el servidor | Repositorio remoto y adaptación del ViewModel y la UI |
| checkpoint b3: cargando, error y reintento | UiState, manejo de excepciones y recuperación de conexión |
| checkpoint c3: publicar resenas | POST, prevención de envíos simultáneos y recarga del detalle |
| checkpoint d2: editar y borrar | PATCH, DELETE, propiedad, video y verificación final |

Las ramas `experimento-b1` y `experimento-c2` conservan las observaciones del fallo sin red y del envío sin validación local. La rama main contiene la versión con las correcciones.

## Mostrar el 403
En la APK debug: menú de tres puntos → **Comprobar permisos (403)** → **Comprobar**. Se consulta una reseña del profesor y se solicita su borrado con la matrícula propia; el servidor lo rechaza. Los botones normales nunca permiten administrar reseñas ajenas. Este menú no aparece en la versión release.

## Compilación verificada
```sh
./gradlew :app:assembleDebug :app:lintDebug :app:assembleRelease
```

No hay base de datos local. La API es compartida: los ejemplos publicados se identifican como demostraciones del laboratorio. Los resultados remotos pueden cambiar cuando otros alumnos utilizan el servidor.
