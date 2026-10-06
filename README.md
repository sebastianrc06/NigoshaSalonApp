# Nigosha Salon App — panel de cliente

Proyecto Android en Java y XML, con navegación por Fragments, ViewModel/LiveData y Firebase Realtime Database. Mantiene los colores verde, crema y dorado del proyecto.

## Abrir esta entrega

1. Conserva tu proyecto anterior como respaldo. Descomprime este ZIP en una carpeta nueva.
2. Copia tu `local.properties` anterior a la raíz del proyecto nuevo. Contiene la ruta local del SDK y tu `MAPS_API_KEY`. No publiques ese archivo.
3. Abre la carpeta `NigoshaSalonApp` en Android Studio y sincroniza Gradle. Se conservaron las versiones de tu proyecto; instala los componentes que solicite Android Studio.
4. Ejecuta en tu emulador o teléfono. El proyecto conserva `app/google-services.json`; no hay que crear otra base de datos.
5. Si el mapa aparece vacío, comprueba la clave, Maps SDK for Android y sus restricciones de paquete `pe.uch.nigosha` y SHA-1 de tu instalación.

No copies solamente las clases Java sobre tu proyecto antiguo: esta entrega también modifica XML, navegación, estilos y manifest.

## Organización

| Carpeta bajo `app/src/main/java/pe/uch/nigosha` | Responsabilidad |
| --- | --- |
| `app` | Inicialización y contenedor de repositorios |
| `core/session` | Identidad de la sesión de prueba |
| `core/ui` | Estados visuales reutilizables |
| `core/util` | Fechas, importes, validación y calendario |
| `domain/models` | Cita, servicio, salón, reglas de horarios y resumen financiero |
| `domain/repository` | Contratos para consultar y guardar datos |
| `data/firebase` | Acceso centralizado a Firebase |
| `data/repository` | Implementaciones de los contratos |
| `feature/cliente` | Contenedor y navegación inferior |
| `feature/inicio` | Bienvenida y próxima cita |
| `feature/catalogo` y `feature/servicio` | Catálogo y detalle |
| `feature/reserva` | Fecha, horarios, revisión y resultado de la solicitud |
| `feature/citas` | Listado, detalle y cancelación |
| `feature/pago` | Consulta del adelanto, sin cobro real |
| `feature/salon` | Ubicación, mapa, teléfono y acceso a WhatsApp |

Los XML de pantalla están en `res/layout`, el grafo en `res/navigation/nav_cliente.xml`, la navegación inferior en `res/menu`, los colores y estilos en `res/values` y `res/values-night`. No se crean subcarpetas arbitrarias dentro de `res/layout`.

Las pantallas observan sus ViewModels. Los ViewModels usan contratos de repositorios; las implementaciones se obtienen de `AppContainer`. No hace falta un XML para cada clase: modelos, validadores y repositorios no representan pantallas.

## Qué se completó

- Inicio consulta la próxima cita de la sesión, con estados de carga, error y ausencia de citas.
- Reserva conserva datos durante recreaciones compatibles con SavedStateHandle; presenta horarios disponibles, datos de contacto, revisión previa y resultado después de guardar.
- La escritura revalida disponibilidad en una transacción y conserva una identificación de solicitud para reintentos. La agenda actual tiene capacidad de una atención simultánea.
- Detalle de cita tiene pantalla propia, importes legibles, estado y cancelación cuando corresponde.
- El calendario abre el editor del dispositivo para una cita confirmada y futura. El usuario decide guardarla; no hay sincronización automática con Google Calendar.
- Pago muestra adelanto requerido, abonado y saldo por separado. Un adelanto calculado no se presenta como dinero pagado.
- Salón centraliza dirección, coordenadas, teléfono y WhatsApp. Puede leer ajustes opcionales desde `/salon`; conserva los datos locales cuando esa configuración no existe.
- Se completaron las clases útiles del flujo y se retiraron las pantallas antiguas y los clientes de API vacíos. No se creó una API de pagos ficticia.

## Datos y decisiones que faltan antes de producción

**Todavía no hay login ni separación real de clientes:** se utiliza `CLI_DEMO`. Los dispositivos de prueba comparten esa identidad. Firebase Auth está como dependencia, pero no se implementó autenticación. Antes de publicar hay que definir reglas de seguridad y asociar citas a UID autenticados.

El horario del prototipo es lunes a sábado, 09:00–18:00, inicios cada 30 minutos y anticipación mínima de 30 minutos. No son horarios comerciales confirmados. La duración debe caber dentro de la jornada. Ajusta `ConfiguracionReservas.java` con la dueña antes de usarlo con clientes.

Se mantiene el adelanto del 50% como regla provisional. La pasarela, creación del pago en servidor, comprobación mediante webhook y confirmación de la cita quedan pendientes. Esta app no cobra ni declara pagada una reserva al pulsar un botón.

La validación actual utiliza la hora del dispositivo y una transacción de Realtime Database. Una versión pública necesita validación autoritativa en servidor, reglas de acceso y una agenda por profesional cuando se agregue personal. No abras las reglas de Firebase para resolver errores de permisos.

No se borran ni se cargan servicios automáticamente. Se conserva la estructura `/servicios` y `/citas`. Registros antiguos con fechas incompletas pueden bloquear disponibilidad por precaución; revísalos manualmente antes de migrar datos.

El salón conserva Av. San Felipe 272, Comas 15313, el enlace de Google Maps proporcionado y teléfono/WhatsApp 990 938 752. Confirma estos datos y el horario con la encargada.

## Verificación realizada

- XML analizado y referencias locales de recursos revisadas.
- Código Java comprobado contra APIs Android/AndroidX/Firebase/Maps, sin errores ni advertencias. Es una comprobación separada del compilador de recursos Android; algunas bibliotecas base de comprobación tienen versiones anteriores compatibles.
- 12 pruebas unitarias aprobadas de horarios, cruces, fechas estrictas, importes, validación e información de citas.
- No se pudo generar el APK con Gradle en el entorno de entrega: la descarga de su distribución quedó bloqueada por la red. No se ejecutó un emulador ni se probaron transacciones contra Firebase Emulator.

## Prueba final en tu equipo

Ejecuta `gradlew.bat testDebugUnitTest assembleDebug` desde Windows. Después comprueba catálogo, reserva, revisión, resultado y Mis citas. Rota el dispositivo durante el formulario; prueba fechas pasadas y horas ocupadas; intenta reservar el mismo tramo desde dos dispositivos; verifica cancelación y estados de error sin conexión. Confirma que ningún importe aparece abonado sin registro de pago real. Comprueba mapa, enlaces, WhatsApp, accesibilidad y modo oscuro en el teléfono.

La carpeta no incluye cachés, APK generados ni `local.properties`. Incluye código, recursos, Gradle Wrapper, configuración Firebase y pruebas.
