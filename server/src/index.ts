import express from "express";
import http from "http";
import { Server } from "socket.io";
import cors from "cors";
import dotenv from "dotenv";
import { setupWebSocket } from "./socket";
import { authRouter } from "./routes/auth";
import { mediaRouter } from "./routes/media";
import { aiRouter } from "./routes/ai";

dotenv.config();

const app = express();
const server = http.createServer(app);

const io = new Server(server, {
  cors: {
    origin: "*",
    methods: ["GET", "POST", "PUT", "DELETE"]
  }
});

app.use(cors());
app.use(express.json());

// API Routes
app.use("/api/auth", authRouter);
app.use("/api/media", mediaRouter);
app.use("/api/ai", aiRouter);

app.get("/health", (req, res) => {
  res.json({
    status: "online",
    service: "WhatsApp Realtime Backend Engine",
    version: "2026.1.0",
    timestamp: new Date().toISOString()
  });
});

// Setup Realtime WebSockets & WebRTC Signaling
setupWebSocket(io);

const PORT = process.env.PORT || 4000;
server.listen(PORT, () => {
  console.log(`[WhatsApp Backend] Servidor rodando com sucesso na porta ${PORT}`);
});
