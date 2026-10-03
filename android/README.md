# Nexora Messenger Android

Aplicación Android nativa de Nexora Messenger construida con Kotlin y Jetpack Compose.

## Funciones actuales

- Login con Firebase Phone Auth.
- Pantalla OTP.
- Perfil inicial con nombre y avatar.
- Avatar subido al VPS por `multipart/form-data`.
- Registro de FCM token contra el VPS.
- FCM service real para refresh de token y notificaciones locales.
- Room local para perfil, chats y mensajes.
- Retrofit/OkHttp para consumir el relay VPS.
- Interceptor que adjunta `Authorization: Bearer <Firebase ID Token>`.
- Pantalla de chats.
- Pantalla `ChatDetail`.
- Cifrado AES-GCM local antes de enviar mensajes.

## Configuración Firebase

Coloca tu archivo real en:

```text
android/app/google-services.json
```

El repo mantiene solo:

```text
android/app/google-services.example.json
```

No subas el archivo real si contiene datos de producción.

## Compilar

```bash
cd android
gradle :app:assembleDebug -PNEXORA_RELAY_BASE_URL=https://api.tu-dominio.com
```

Para emulador Android contra API local:

```bash
gradle :app:assembleDebug -PNEXORA_RELAY_BASE_URL=http://10.0.2.2:8080
```

## Estructura principal

```text
app/src/main/java/com/nexora/app/
 ├─ data/crypto       Claves locales y cifrado AES-GCM
 ├─ data/local        Room entities, DAO y DB
 ├─ data/messaging    FirebaseMessagingService
 ├─ data/remote       Retrofit API + DTOs + auth interceptor
 ├─ data/repository   Auth/Profile/Chat repositories
 ├─ ui/navigation     Root flow
 ├─ ui/screens        Auth, Profile, Chats, ChatDetail
 ├─ ui/theme          Tema Compose
 └─ ui/viewmodel      ViewModels y factories
```

## Nota de seguridad

La app cifra el payload localmente antes de llamar al relay. Esta fase protege al servidor contra texto plano. La siguiente fase debe implementar E2EE completo por sesión/dispositivo con intercambio de claves y rotación.
