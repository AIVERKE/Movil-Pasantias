# Movil-Pasantias

Aplicación Android nativa en **Java** para el sistema de gestión de pasantías. Consume el API REST del backend NestJS y replica el design system **Scholar Core** del frontend web del mismo proyecto.

**Repositorio:** [github.com/AIVERKE/Movil-Pasantias](https://github.com/AIVERKE/Movil-Pasantias)

## Requisitos previos

| Herramienta | Versión |
|-------------|---------|
| Android Studio | Hedgehog (2023.1.1) o superior |
| JDK | 17 |
| Android SDK | API 34 (compileSdk) |
| Emulador o dispositivo | API 24+ (Android 7.0+) |
| Backend NestJS | Puerto **3000** (PostgreSQL en ejecución) |

> Si la ruta del proyecto contiene caracteres especiales (por ejemplo `técnico`), ya está habilitado `android.overridePathCheck=true` en `gradle.properties` para compilar en Windows sin mover la carpeta.

## Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/AIVERKE/Movil-Pasantias.git
cd Movil-Pasantias
```

### 2. Configurar el SDK de Android

Copia el archivo de ejemplo y ajusta la ruta de tu SDK:

```bash
cp local.properties.example local.properties
```

Edita `local.properties` y define `sdk.dir` con la ruta de tu Android SDK. Ejemplo en Windows:

```properties
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
```

En Linux/macOS suele ser `~/Android/Sdk`. Android Studio también puede generar este archivo al abrir el proyecto.

### 3. Levantar el backend

La app **no funciona sin el backend**. Necesitas el proyecto NestJS del sistema de pasantías (mismo equipo u organización) con:

1. PostgreSQL corriendo con la base de datos configurada.
2. Backend en el puerto **3000**:

```bash
npm install
npm run start:dev
```

Verifica que responda en `http://localhost:3000/api`.

### 4. Abrir y ejecutar en Android Studio

1. **File → Open** y selecciona la carpeta `Movil-Pasantias`.
2. Espera a que Gradle sincronice (descarga dependencias la primera vez).
3. Crea o elige un emulador con **API 24 o superior**.
4. Pulsa **Run** (▶) o `Shift+F10`.

Desde terminal (con `local.properties` configurado):

```bash
./gradlew assembleDebug
./gradlew installDebug
```

## Configuración del API

Las URLs base se definen en `app/build.gradle` dentro de `buildTypes`:

| Entorno | `API_BASE_URL` | Cuándo usarlo |
|---------|----------------|---------------|
| Debug (emulador) | `http://10.0.2.2:3000/api/` | Por defecto; `10.0.2.2` es el localhost del PC visto desde el emulador |
| Debug (dispositivo físico) | `http://<IP-LAN-PC>:3000/api/` | Cambia la IP por la de tu PC en la red local (ej. `192.168.1.10`) |
| Release | `https://tu-servidor.com/api/` | Producción; actualiza antes de publicar |

Archivos estáticos (logos, fotos): `{MEDIA_BASE_URL}/uploads/...` (misma IP/puerto que el backend en debug).

El tráfico HTTP sin cifrar está permitido solo en builds **debug** (`network_security_config` + `usesCleartextTraffic`).

## Credenciales de prueba (seed del backend)

| Rol | Email | Contraseña |
|-----|-------|------------|
| Gerente | `andres.sosa@techbolivia.com` | `Gerente@2024!` |
| Jefe de pasantes | `marcos.delgado@techbolivia.com` | `Jefe@2024!` |
| Pasante (estudiante) | `ana.gutierrez@est.umsa.edu.bo` | `Estudiante@2024!` |

Tras iniciar sesión, la app redirige automáticamente según el rol del usuario.

## Funcionalidades por rol

### Gerente
- Dashboard con KPIs
- CRUD de pasantías
- Datos de empresa y logo
- Notificaciones

### Jefe de pasantes
- Dashboard e inscripciones (aprobar/rechazar)
- Pasantes activos
- Actividades, comentarios y mensajes
- Notificaciones

### Pasante (estudiante)
- Dashboard personal
- Catálogo de pasantías e inscripción
- Actividades, bitácoras y calendario
- Notificaciones

## Arquitectura

- **Patrón:** Activities/Fragments + Retrofit (`ApiClient`) + almacenamiento seguro de token (`TokenManager`)
- **Tema:** colores Scholar Core — `#3B82F6`, `#1A2233`, `#D16900`
- **Notificaciones:** WorkManager consulta el conteo de notificaciones cada 15 min (polling; sin FCM)

```
app/src/main/java/com/pasantias/movil/
├── data/api/          # Retrofit PasantiasApi
├── data/dto/          # Modelos JSON
├── data/local/        # TokenManager
├── ui/auth/           # Login
├── ui/gerente/        # Módulo gerente
├── ui/jefe/           # Módulo jefe
├── ui/estudiante/     # Módulo pasante
├── ui/common/         # Listas, notificaciones
└── worker/            # NotificationSyncWorker
```

## Solución de problemas

| Problema | Posible causa | Solución |
|----------|---------------|----------|
| Error de red al iniciar sesión | Backend apagado o URL incorrecta | Confirma que el backend escucha en `:3000` y revisa `API_BASE_URL` |
| No conecta desde el teléfono físico | Usas `10.0.2.2` (solo emulador) | Pon la IP LAN de tu PC en `app/build.gradle` → `debug` |
| Gradle no encuentra el SDK | Falta `local.properties` | Copia `local.properties.example` y define `sdk.dir` |
| Imágenes no cargan | `MEDIA_BASE_URL` distinta al servidor | Debe apuntar al mismo host que el API (sin `/api/`) |

## Licencia

Proyecto académico — Taller de Técnico Superior.
