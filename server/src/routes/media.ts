import { Router, Request, Response } from "express";

export const mediaRouter = Router();

// Upload media file simulation (AWS S3 / Cloudinary)
mediaRouter.post("/upload", (req: Request, res: Response) => {
  const { type, isHd } = req.body;

  // Emulates cloud bucket storage upload returning public CDN URL
  const mockCdnUrls: Record<string, string> = {
    IMAGE: isHd
      ? "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1600"
      : "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
    AUDIO: "https://example.com/audio/voice_note_14920.m4a",
    DOCUMENT: "https://example.com/docs/arquivo_whatsapp.pdf"
  };

  const cdnUrl = mockCdnUrls[type] || mockCdnUrls.IMAGE;

  return res.json({
    success: true,
    url: cdnUrl,
    fileSize: isHd ? "4.2 MB" : "850 KB",
    type: type || "IMAGE",
    isHd: !!isHd
  });
});
