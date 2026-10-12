# WHATSAPP CLONE FULL-STACK (PRODUCTION ARCHITECTURE)

Este projeto contém a réplica completa e de alta fidelidade do **WhatsApp**, englobando:
1. **Frontend Nativo Android** em Kotlin com **Jetpack Compose**, Material Design 3 e suporte a Dark/Light Mode.
2. **Backend em Node.js com WebSockets (Socket.IO)**, sinalização **WebRTC VoIP**, banco de dados relacional com **Prisma ORM (PostgreSQL)** e integração com **Meta AI**.

---

## 🚀 1. INICIALIZAÇÃO DO BACKEND (NODE.JS & WEBSOCKETS)

### Pré-requisitos:
- Node.js 18+ instalado
- PostgreSQL ou container Docker ativo

### Passos de Execução:
1. Abra o terminal na pasta `server`:
   ```bash
   cd server
   ```

2. Instale as dependências:
   ```bash
   npm install
   ```

3. Configure o arquivo `.env`:
   ```env
   PORT=4000
   DATABASE_URL="postgresql://postgres:postgres@localhost:5432/whatsapp?schema=public"
   JWT_SECRET="whatsapp_production_secret_key_2026"
   ```

4. Gere o cliente Prisma e execute as migrações:
   ```bash
   npm run prisma:generate
   npm run prisma:migrate
   ```

5. Inicie o servidor em modo de desenvolvimento com hot-reload:
   ```bash
   npm run dev
   ```
   O servidor estará ativo em `http://localhost:4000` pronto para receber conexões HTTP e WebSocket.

---

## 📱 2. SINCRONIZAÇÃO E EXECUÇÃO NO ANDROID STUDIO

### No Visual Studio Code / Terminal:
1. Para abrir o projeto no VS Code:
   ```bash
   code .
   ```

### No Android Studio:
1. Abra o **Android Studio**.
2. Selecione **Open** e escolha a pasta raiz deste repositório.
3. Aguarde o **Gradle Sync** baixar as dependências (`libs.versions.toml`).
4. Selecione um dispositivo físico ou emulador Android com API 33 ou 34.
5. Clique no botão **Run (Shift + F10)** para instalar o APK.

---

## 📦 3. GERAÇÃO DE APK E AAB PARA PRODUÇÃO

Para gerar o arquivo **APK** de depuração diretamente pelo terminal Gradle:
```bash
gradle assembleDebug
```
O APK gerado estará em:
`app/build/outputs/apk/debug/app-debug.apk`

Para gerar o **App Bundle (AAB)** assinado para publicação na Google Play Store:
```bash
gradle bundleRelease
```
O AAB gerado estará em:
`app/build/outputs/bundle/release/app-release.aab`

---

## 🛡️ 4. MATRIZ DE RECURSOS IMPLEMENTADOS
- **Conversas:** Filtros (Tudo, Não lidas, Favoritos, Grupos), fixação de até 3 conversas no topo, conversas bloqueadas com biometria.
- **Mensagens:** Formatação Markdown (*negrito*, _itálico_, ~tachado~, `mono`), mensagens editadas com tag "(editada)", mensagens temporárias, visualização única (View Once "1"), reações com emojis e swipe-to-reply.
- **Áudio:** Gravação com lock hands-free, waveform dinâmico, velocidade (1x, 1.5x, 2x) e transcrição automática Speech-to-Text.
- **Mídia:** Envio de fotos/vídeos em Normal e HD, documentos (PDF/DOCX), localização em tempo real e compartilhamento de contatos vCard.
- **Enquetes:** Votação interativa com barras proporcionais animadas e contagem em tempo real.
- **Atualizações:** Status com expiração de 24h + Canais verificados com feed público, reações e seguidores.
- **Comunidades:** Agrupamento de grupos de tópicos, canal de avisos gerais e chat de voz suspenso.
- **Chamadas:** Chamadas de áudio e vídeo com WebRTC, mute de microfone, câmera on/off, compartilhamento de tela e alto-falante.
- **Meta AI:** Assistente oficial com geração de texto e suporte a prompts `/imagine` para criação de imagens.
- **Múltiplas Contas:** Alternador rápido de conta e número telefônico com perfil e nomes de usuário únicos (@username).
