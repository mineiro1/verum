import { Server, Socket } from "socket.io";

export function setupWebSocket(io: Server) {
  io.on("connection", (socket: Socket) => {
    const userId = socket.handshake.query.userId as string;
    console.log(`[Socket] Usuário conectado: ${userId || socket.id}`);

    if (userId) {
      socket.join(`user:${userId}`);
    }

    // Join Chat Room
    socket.on("chat:join", (chatId: string) => {
      socket.join(`chat:${chatId}`);
      console.log(`[Socket] ${socket.id} entrou na sala chat:${chatId}`);
    });

    socket.on("chat:leave", (chatId: string) => {
      socket.leave(`chat:${chatId}`);
    });

    // Realtime Message Broadcast
    socket.on("message:send", (payload: {
      chatId: string;
      message: any;
    }) => {
      socket.to(`chat:${payload.chatId}`).emit("message:received", payload.message);
    });

    // Read Receipts
    socket.on("message:read", (payload: {
      chatId: string;
      messageId: string;
      userId: string;
    }) => {
      socket.to(`chat:${payload.chatId}`).emit("message:status_updated", {
        messageId: payload.messageId,
        status: "READ"
      });
    });

    // Typing Indicators
    socket.on("typing:start", (payload: { chatId: string; userName: string }) => {
      socket.to(`chat:${payload.chatId}`).emit("typing:started", {
        chatId: payload.chatId,
        userName: payload.userName
      });
    });

    socket.on("typing:stop", (payload: { chatId: string }) => {
      socket.to(`chat:${payload.chatId}`).emit("typing:stopped", {
        chatId: payload.chatId
      });
    });

    // Reactions
    socket.on("reaction:add", (payload: {
      chatId: string;
      messageId: string;
      emoji: string;
      userId: string;
    }) => {
      io.to(`chat:${payload.chatId}`).emit("reaction:updated", payload);
    });

    // Polls
    socket.on("poll:vote", (payload: {
      chatId: string;
      messageId: string;
      optionId: string;
      userId: string;
    }) => {
      io.to(`chat:${payload.chatId}`).emit("poll:vote_updated", payload);
    });

    // ==========================================
    // WebRTC VOIP CALL SIGNALING ENGINE
    // ==========================================

    // Offer (Iniciador envia SDP Offer para o destino)
    socket.on("webrtc:offer", (data: {
      targetUserId: string;
      callerId: string;
      callerName: string;
      sdp: any;
      callType: "AUDIO" | "VIDEO";
    }) => {
      socket.to(`user:${data.targetUserId}`).emit("webrtc:incoming_call", {
        callerId: data.callerId,
        callerName: data.callerName,
        sdp: data.sdp,
        callType: data.callType
      });
    });

    // Answer (Destino aceita e envia SDP Answer de volta)
    socket.on("webrtc:answer", (data: {
      targetUserId: string;
      sdp: any;
    }) => {
      socket.to(`user:${data.targetUserId}`).emit("webrtc:call_answered", {
        sdp: data.sdp
      });
    });

    // ICE Candidate Exchange
    socket.on("webrtc:ice_candidate", (data: {
      targetUserId: string;
      candidate: any;
    }) => {
      socket.to(`user:${data.targetUserId}`).emit("webrtc:ice_candidate", {
        candidate: data.candidate
      });
    });

    // End Call
    socket.on("webrtc:end_call", (data: {
      targetUserId: string;
    }) => {
      socket.to(`user:${data.targetUserId}`).emit("webrtc:call_ended");
    });

    socket.on("disconnect", () => {
      console.log(`[Socket] Desconectado: ${socket.id}`);
    });
  });
}
