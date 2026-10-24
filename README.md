# Ada, Color y Trazo

Aplicación educativa Android en Kotlin y Jetpack Compose. Esta versión conserva las actividades y la estética pastel del proyecto original, y corrige recursos de personajes, interacción, animaciones, guardado y ciclo de vida.

## Maquetas aprobadas integradas

Se incorporaron las ocho imágenes finales de `prototipos-aprobados-Ada-Color-y-Trazo.zip`. Al terminar la bienvenida, la app muestra el menú principal aprobado a pantalla completa. Los cubos abren **Jugar con Ada**, que ofrece Armar el cuerpo y Memorice; Vestir según el clima ya no se repite en esa categoría. El icono de ropa es el único acceso al juego del clima. El gato abre Reconocer animales, el auto abre Reconocer transportes, la manzana abre **Crear** y el dibujo abre Dibujo libre. **Armar el cuerpo** usa el mismo personaje ilustrado en la maqueta aprobada: se quitó del fondo el brazo suelto y se aislaron sus piezas originales (rostro vacío, cabello, orejas, ojos, nariz, boca, torso, brazos y piernas con zapatillas) para que mantengan su apariencia y proporciones al arrastrarlas. Memorice y Dibujo libre conservan sus maquetas de entrada y permiten pasar a las actividades al tocar la zona de juego. La pantalla de Jugar con Ada mantiene la maqueta aprobada con la tarjeta de clima retirada. Las imágenes de referencia originales permanecen en `app/src/main/res/drawable-nodpi/approved_*.png`.

Las referencias son verticales (9:16), por lo que la app mantiene la orientación retrato y oculta las barras del sistema en estas pantallas para preservar su composición. La imagen aprobada completa siempre se mantiene visible; en pantallas con otra proporción, el espacio sobrante se rellena con una versión desenfocada de esa misma imagen. En **Armar el cuerpo**, se elimina la barra blanca superior: la flecha de regreso forma parte de la ilustración aprobada y su zona táctil es invisible.

## Personajes didácticos

Los nombres propios se mantienen tanto en la interfaz como en la voz. El identificador del dibujo es independiente del nombre mostrado: Tobías usa el perro; Confeti, el gato; Wormi, el gusano; Cuco, el pollito; Flica, el caballo; Gingi, el pato; y Spunky, el cerdo. Son correspondencias adoptadas para reutilizar las ilustraciones existentes; se pueden ajustar en `ui/AnimalsFlow.kt` sin cambiar los nombres de los personajes.

## Correcciones incluidas

El confeti se sincroniza con cuadros de animación y ya no ejecuta un bucle ocupado en el hilo principal. Las animaciones decorativas del menú leen su estado durante dibujo o en capas gráficas, evitando recalcular el layout completo en cada cuadro.

El dibujo libre actualiza trazos mediante estado observable. El coloreado carga imágenes y rellena regiones fuera del hilo principal. La rasterización, compresión y escritura de PNG también se ejecutan fuera del hilo de interfaz.

Las dos pantallas usan un guardado común, con mensajes de resultado y limpieza de archivos o entradas incompletas. En Android 7–9 se solicita permiso de escritura cuando el usuario guarda; en Android 10 o posterior se usa `MediaStore` con publicación mediante `IS_PENDING`.

Se conserva la pantalla activa, el personaje/dibujo seleccionado y las sesiones de edición al rotar. Memorice cancela trabajos de partidas anteriores y normaliza un par incorrecto pendiente al restaurar. El botón Atrás vuelve al menú y la música se reactiva al regresar del segundo plano.

El menú y las paletas admiten desplazamiento. Las piezas nuevas del cuerpo parten dentro de una bandeja calculada según el tamaño de la ventana y su arrastre tiene límites. El armado continúa siendo libre, sin evaluar si las piezas quedaron en posiciones correctas.

Las ilustraciones originales de coloreado se mantienen en `res/drawable-nodpi`; se añadieron además 33 ilustraciones fijas de apariencia 3D para animales, frutas, verduras y transportes. Los selectores y reconocedores usan esas nuevas ilustraciones, y Memorice las prefiere cuando están disponibles. El editor sigue usando los dibujos lineales originales para poder rellenar regiones.

## Bienvenida de Ada

Al iniciar, se conserva la bienvenida animada de Ada en la banca; al terminar, continúa al menú principal. El ícono redondo del launcher se mantiene independiente de esa escena.

La bienvenida usa el video existente y mantiene la alternativa prevista por `SplashCute` cuando no se puede reproducir. El poster también se reutiliza para presentar a Ada dentro del menú.

## Rediseño visual inspirado en la referencia

El TikTok compartido de un rompecabezas infantil inspira la interacción táctil, los elementos grandes y el tono de jardín; no se copia su arte ni sus personajes. `ui/PlayfulGameUi.kt` incorpora cielo y colinas, encabezados compactos, paneles redondeados con relieve, objetivos con ilustración flotante, tarjetas grandes y estados de acierto/error.

El lenguaje visual se extendió a los tres reconocedores (animales, frutas/verduras y transportes), los selectores de dibujos, Colores, Memorice, armado del cuerpo, el juego del clima y los editores de dibujo/coloreado. Se quitó la barra superior redundante donde las pantallas ya ofrecen navegación propia; el menú ahora deja más espacio a sus actividades y destaca que funcionan sin Wi-Fi.

En los reconocedores, la respuesta correcta permanece visible y se bloquean nuevos toques hasta avanzar a una consigna distinta; el panel y la voz ya no se cruzan con el objetivo siguiente. En Frutas y verduras, cada opción usa un color vivo asociado al alimento. Los botones para volver comparten ahora una casita ilustrada en 3D, en vez de dibujos vectoriales distintos entre pantallas. Colores también conserva el acierto antes de cambiar de ronda. Se mantuvieron las reglas y recursos de juego existentes.

Los nombres visuales pueden conservar su emoji, pero la voz y las descripciones accesibles usan solo el nombre. Así TextToSpeech no vuelve a decir «moto» al leer el emoji de moto; el mismo ajuste se aplica a animales, frutas, verduras y pares de Memorice.

Memorice usa la foto completa de Ada en la banca con sus animales como reverso y no imprime consignas sobre las cartas. Al girar aparece el dibujo; la voz nombra los pares encontrados. La partida comienza con 3 parejas (6 cartas) y, al completar cada tablero, sube a 6 parejas (12 cartas) y luego a 9 parejas (18 cartas). El mazo mezcla colores, frutas, verduras, transportes y animales; las cartas de colores muestran una muestra y su nombre. El pie presenta las etapas y el reinicio con tarjetas visuales. Al completar un tablero aparece una celebración de fuegos artificiales con destellos. Los demás logros que antes mostraban confeti (coloreado y juegos de reconocer) también usan ahora la misma animación de fuegos artificiales.

En **Armar el cuerpo**, las doce piezas visibles —cara vacía, cabello, orejas, ojos, nariz, boca, torso, brazos y piernas— son ilustraciones nuevas originales con volumen 3D y transparencia, ya no las piezas planas anteriores. Se presentan sueltas, sin placas ni sombras rectangulares; la cara no incluye ojos, nariz ni boca para que el niño los complete. Se conserva el jardín aprobado y la zona táctil de arrastre.

En **Vestir a Ada**, los botones de Sol y Lluvia usan ilustraciones 3D infantiles. Tocar o arrastrar una prenda la coloca sobre Ada; chaqueta y botas cambian a ilustraciones completas de Ada ya vestida —en lugar de superponerse al personaje base—; al elegir ambas aparece la variante con el atuendo completo. El paraguas se mantiene como accesorio que Ada lleva. La chaqueta reemplaza a la polera y solo se acepta una prenda por ranura. La guía hablada se conserva. El editor de coloreado tiene muestras de pintura brillantes y tres herramientas con ilustraciones 3D —pincel, bote de pintura y goma—; la goma restaura la ilustración original y conserva los contornos. Se retiraron los botones de deshacer, rehacer, reinicio y grosor para simplificar la pantalla. Los encabezados Fredoka de las actividades usan ahora varios colores; las instrucciones mantienen colores oscuros para facilitar la lectura.

**Límite visual:** los personajes de juego siguen siendo imágenes WebP estáticas con apariencia 3D. El pequeño movimiento añadido es una animación de interfaz; no equivale a modelos 3D interactivos. La bienvenida conserva su video 3D prerenderizado. Para dar el siguiente paso harían falta modelos/rigs 3D reales y una decisión sobre su integración en Android.

Los cambios actuales se conservan en la copia local y no se publicaron en GitHub. El paquete fuente actualizado se entrega en `Ada-Color-y-Trazo-actualizado.zip`, junto con un APK debug separado para pruebas. La compilación debug, las pruebas unitarias y lint se ejecutaron correctamente con JDK y Android SDK 34. No se validó en un teléfono o emulador, por lo que aún conviene probar interacción, adaptación de pantalla y guardado en un dispositivo real.

## Compilar y probar

Se requiere un JDK completo compatible con AGP 8.5.2 —se recomienda JDK 17—, el SDK Android 34 y Build Tools 34.0.0. Android Studio puede instalar estos componentes y crear `local.properties`; ese archivo es local y no se distribuye en el repositorio.

Abre **esta carpeta**, `AdaColoryTrazo`, como proyecto en Android Studio y sincroniza Gradle. Desde terminal, con el JDK y SDK configurados:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

En Windows se utiliza `gradlew.bat` en lugar de `./gradlew`.

El APK de prueba se genera en `app/build/outputs/apk/debug/app-debug.apk`. Es una compilación debug para probar, no una publicación en Google Play ni una versión firmada con una clave de producción.

## Límites y pruebas en dispositivo

El éxito de compilación y las pruebas unitarias no demuestran la fluidez en un celular concreto. Hay que probar arrastre, coloreado, guardado, permisos, rotación, retorno del segundo plano y tamaños de pantalla en dispositivos representativos.

La sesión editable de dibujo o coloreado se mantiene durante una recreación de configuración, pero no se persiste automáticamente en disco ante la muerte del proceso. Para conservar el resultado fuera de la sesión se debe guardar el PNG. El editor conserva el dibujo activo; al elegir otro dibujo se inicia una sesión de coloreado nueva para mantener el consumo de memoria acotado.

La referencia de Instagram se usó como orientación visual general; no se certifica una equivalencia 1:1 ni una tasa de cuadros concreta. La mejora de animaciones y tarjetas no sustituye la creación de modelos 3D reales.
