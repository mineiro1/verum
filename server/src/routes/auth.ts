import { Router, Request, Response } from "express";
import jwt from "jsonwebtoken";

export const authRouter = Router();
const JWT_SECRET = process.env.JWT_SECRET || "whatsapp_secret_super_key_2026";

// Step 1: Send SMS OTP
authRouter.post("/send-otp", (req: Request, res: Response) => {
  const { phone } = req.body;
  if (!phone) {
    return res.status(400).json({ error: "Número de telefone obrigatório." });
  }

  // Simulated SMS dispatch
  console.log(`[SMS OTP] Código 619-482 enviado para ${phone}`);
  return res.json({
    success: true,
    message: "Código de verificação de 6 dígitos enviado por SMS.",
    devOtpCode: "619482"
  });
});

// Step 2: Verify OTP & Login
authRouter.post("/verify-otp", (req: Request, res: Response) => {
  const { phone, code, username, name } = req.body;

  if (code !== "619482") {
    return res.status(400).json({ error: "Código de verificação incorreto ou expirado." });
  }

  const token = jwt.sign({ phone, username: username || "user.whatsapp" }, JWT_SECRET, {
    expiresIn: "30d"
  });

  return res.json({
    success: true,
    token,
    user: {
      id: "usr_" + Math.random().toString(36).substring(2, 9),
      phone,
      name: name || "Carlos Silva",
      username: username || "carlos.silva",
      statusText: "Olá! Estou usando o WhatsApp."
    }
  });
});
