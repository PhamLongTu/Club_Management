package com.clubmanagement.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public final class ImageUtil {

    private ImageUtil() {}

    public static String buildInitials(String fullName) {
        if (fullName == null) return "?";
        String trimmed = fullName.trim();
        if (trimmed.isEmpty()) return "?";
        String[] parts = trimmed.split("\\s+");
        String first = parts[0];
        String last = parts.length > 1 ? parts[parts.length - 1] : "";
        StringBuilder initials = new StringBuilder();
        if (!first.isEmpty()) initials.append(first.substring(0, 1));
        if (!last.isEmpty()) initials.append(last.substring(0, 1));
        String value = initials.toString().toUpperCase();
        return value.isEmpty() ? "?" : value;
    }

    public static ImageIcon loadCircleAvatar(String avatarUrl, int size,
                                             String initials, Color bg, Color fg) {
        BufferedImage base = loadImage(avatarUrl);
        BufferedImage canvas = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        Color safeBg = bg != null ? bg : new Color(226, 232, 240);
        Color safeFg = fg != null ? fg : new Color(30, 41, 59);
        g.setColor(safeBg);
        g.fillOval(0, 0, size, size);

        if (base != null) {
            BufferedImage square = scaleAndCrop(base, size);
            g.setClip(new Ellipse2D.Double(0, 0, size, size));
            g.drawImage(square, 0, 0, null);
            g.setClip(null);
        } else {
            drawInitials(g, initials, size, safeFg);
        }
        g.dispose();
        return new ImageIcon(canvas);
    }

    public static ImageIcon loadSquareAvatar(String avatarUrl, int size,
                                             String initials, Color bg, Color fg) {
        BufferedImage base = loadImage(avatarUrl);
        BufferedImage square = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = square.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        Color safeBg = bg != null ? bg : new Color(226, 232, 240);
        Color safeFg = fg != null ? fg : new Color(30, 41, 59);
        g.setColor(safeBg);
        g.fillRect(0, 0, size, size);

        if (base != null) {
            BufferedImage cropped = scaleAndCrop(base, size);
            g.drawImage(cropped, 0, 0, null);
        } else {
            drawInitials(g, initials, size, safeFg);
        }
        g.dispose();
        return new ImageIcon(square);
    }

    private static BufferedImage loadImage(String avatarUrl) {
        if (avatarUrl == null) return null;
        String trimmed = avatarUrl.trim();
        if (trimmed.isEmpty()) return null;
        File file = new File(trimmed);
        if (!file.exists() || !file.isFile()) return null;
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            return null;
        }
    }

    private static BufferedImage scaleAndCrop(BufferedImage src, int size) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();
        if (srcW <= 0 || srcH <= 0) return null;

        double scale = Math.max((double) size / srcW, (double) size / srcH);
        int newW = (int) Math.round(srcW * scale);
        int newH = (int) Math.round(srcH * scale);
        int x = (size - newW) / 2;
        int y = (size - newH) / 2;

        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, x, y, newW, newH, null);
        g.dispose();
        return out;
    }

    private static void drawInitials(Graphics2D g, String initials, int size, Color fg) {
        String value = (initials == null || initials.isBlank()) ? "?" : initials;
        int fontSize = Math.max(12, (int) (size * 0.4));
        g.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        g.setColor(fg);
        FontMetrics fm = g.getFontMetrics();
        int x = (size - fm.stringWidth(value)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(value, x, y);
    }
}
