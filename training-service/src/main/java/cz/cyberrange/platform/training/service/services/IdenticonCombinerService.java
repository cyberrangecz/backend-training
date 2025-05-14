package cz.cyberrange.platform.training.service.services;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

@Service
public class IdenticonCombinerService {

  public byte[] combineIdenticons(List<byte[]> identiconList) {
    int count = identiconList.size();
    int gridSize = (int) Math.ceil(Math.sqrt(count));

    int canvasSize = 100;
    int cellSize = canvasSize / gridSize;

    BufferedImage result = new BufferedImage(canvasSize, canvasSize, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = result.createGraphics();
    g2d.setBackground(new Color(0, 0, 0, 0));
    g2d.clearRect(0, 0, canvasSize, canvasSize);

    for (int i = 0; i < count; i++) {
      BufferedImage icon = decodeToImage(identiconList.get(i));
      int iconWidth = icon.getWidth();
      int iconHeight = icon.getHeight();

      float scale = Math.min((float) cellSize / iconWidth, (float) cellSize / iconHeight);
      int scaledWidth = Math.round(iconWidth * scale);
      int scaledHeight = Math.round(iconHeight * scale);

      Image scaledIcon = icon.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

      int row = i / gridSize;
      int col = i % gridSize;
      int x = col * cellSize + (cellSize - scaledWidth) / 2;
      int y = row * cellSize + (cellSize - scaledHeight) / 2;

      g2d.drawImage(scaledIcon, x, y, null);
    }

    g2d.dispose();

    return toByteArray(result);
  }

  private BufferedImage decodeToImage(byte[] data) {
    try {
      return ImageIO.read(new ByteArrayInputStream(data));
    } catch (Exception e) {
      throw new RuntimeException("Failed to decode image", e);
    }
  }

  private byte[] toByteArray(BufferedImage image) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ImageIO.write(image, "png", baos);
      return baos.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException("Failed to encode image", e);
    }
  }
}
