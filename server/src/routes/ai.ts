import { Router, Request, Response } from "express";

export const aiRouter = Router();

// Meta AI prompt generation and /imagine endpoint
aiRouter.post("/chat", async (req: Request, res: Response) => {
  const { prompt } = req.body;

  if (!prompt) {
    return res.status(400).json({ error: "O prompt é obrigatório." });
  }

  const isImagine = prompt.trim().toLowerCase().startsWith("/imagine");

  if (isImagine) {
    const query = prompt.replace(/\/imagine/i, "").trim();
    return res.json({
      type: "IMAGE",
      text: `Aqui está a imagem gerada para: "${query}"`,
      imageUrl: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800",
      caption: query
    });
  }

  return res.json({
    type: "TEXT",
    text: `Olá! Como Meta AI integrada ao WhatsApp, analisei sua solicitação: "${prompt}". Estou sempre pronta para responder perguntas, escrever textos, gerar código e criar imagens com /imagine.`
  });
});
