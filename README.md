## Repositorio

<https://github.com/jhonrodas1/LabsCM20262-Gr10>

## Equipo

| Integrante | GitHub |
|---|---|
| Jhon Deivy Rodas Morales | [@jhonrodas1](https://github.com/jhonrodas1) |
| Nelson Alcides Puerta García | [@Neltrin22](https://github.com/Neltrin22) |

# Rally Finance — Lab 2

Aplicación Android desarrollada en **Kotlin** y **Jetpack Compose** para visualizar información financiera y consultar la tasa de cambio entre dólares estadounidenses (USD) y pesos colombianos (COP).

Proyecto correspondiente al **Laboratorio 2 — Servicios** del curso Computación Móvil, Grupo 10, semestre 2026-2.

## Características

- Consulta automática y manual de la tasa USD/COP.
- Sincronización inicial al iniciar la aplicación.
- Sincronización periódica cada 15 minutos cuando existe conexión a Internet.
- Conversión de balances y facturas a pesos colombianos.
- Navegación por las secciones **Resumen**, **Cuentas** y **Facturas**.
- Interfaz construida con Jetpack Compose y Material 3.
- Representación gráfica de la distribución de cuentas y facturas.
- Reintento automático de las solicitudes fallidas mediante WorkManager.

## Capturas de pantalla

> Próximamente: se pueden añadir aquí capturas de las pantallas principales.

## Tecnologías

| Categoría | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.2.10 |
| Interfaz | Jetpack Compose, Material 3 |
| Navegación | Navigation Compose |
| Servicios HTTP | Retrofit 2.11.0 y Gson |
| Procesos en segundo plano | WorkManager 2.10.0 |
| Estado reactivo | Kotlin Coroutines y StateFlow |
| Construcción | Gradle 9.6.0 y Android Gradle Plugin 9.4.1 |

## Arquitectura

El proyecto está organizado por responsabilidades dentro del módulo `Lab2-Services`:

```text
Lab2-Services/src/main/java/co/edu/udea/compumovil/gr10_20262/lab2/
├── data/
│   ├── ExchangeApiService.kt
│   ├── RallyModels.kt
│   └── RallyRepository.kt
├── ui/
│   ├── RallyApp.kt
│   ├── RallyScreens.kt
│   └── theme/
├── worker/
│   └── RallySyncWorker.kt
└── MainActivity.kt
```

- **data:** modelos, repositorio local y cliente HTTP.
- **ui:** navegación, pantallas y tema visual.
- **worker:** ejecución de la sincronización de forma diferida.
- **MainActivity:** configuración de la aplicación y programación de tareas.

La tasa de cambio se obtiene desde:

`https://open.er-api.com/v6/latest/USD`

No se requiere una clave API.

## Requisitos

- Android Studio con soporte para Android SDK 37.
- JDK compatible con Android Studio.
- Android SDK 24 como versión mínima o superior.
- Conexión a Internet para consultar la tasa de cambio.
- Dispositivo físico o emulador con Android 7.0 (API 24) o superior.

## Instalación y ejecución

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/jhonrodas1/LabsCM20262-Gr10.git
   cd LabsCM20262-Gr10
   ```

2. Abrir la carpeta del proyecto en Android Studio.

3. Esperar a que Gradle sincronice las dependencias.

4. Seleccionar el módulo o configuración `Lab2-Services`.

5. Ejecutar la aplicación en un emulador o dispositivo.

También puede construirse desde una terminal de Windows:

```bat
gradlew.bat assembleDebug
```

El APK generado quedará disponible en:

```text
Lab2-Services/build/outputs/apk/debug/
```

## Sincronización

La aplicación configura dos tipos de trabajo con WorkManager:

- **Una vez:** se ejecuta al abrir la aplicación y al pulsar **Sync Now**.
- **Periódico:** se ejecuta cada 15 minutos con una restricción de conectividad.

Si la solicitud falla o no existe conexión, WorkManager solicita un reintento. Si no se encuentra la tasa COP en la respuesta, la aplicación conserva un valor predeterminado de 4200 COP por USD.

## Pruebas

Ejecutar las pruebas unitarias:

```bat
gradlew.bat test
```

Ejecutar las pruebas instrumentadas con un dispositivo o emulador conectado:

```bat
gradlew.bat connectedAndroidTest
```




