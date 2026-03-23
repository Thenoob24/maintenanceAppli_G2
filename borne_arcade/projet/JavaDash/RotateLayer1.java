import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class RotateLayer1 {
    public static void main(String[] args) {
        try {
            BufferedImage img = ImageIO.read(new File("./img/player/layer1.png"));
            int w = img.getWidth();
            int h = img.getHeight();
            
            for (int i = 1; i <= 3; i++) {
                int angle = i * 90;
                double rads = Math.toRadians(angle);
                double sin = Math.abs(Math.sin(rads));
                double cos = Math.abs(Math.cos(rads));
                int neww = (int) Math.floor(w * cos + h * sin);
                int newh = (int) Math.floor(h * cos + w * sin);
                
                BufferedImage rot = new BufferedImage(neww, newh, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = rot.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                AffineTransform at = new AffineTransform();
                at.translate((neww - w) / 2.0, (newh - h) / 2.0);
                at.rotate(rads, w / 2.0, h / 2.0);
                g2d.setTransform(at);
                g2d.drawImage(img, 0, 0, null);
                g2d.dispose();
                
                ImageIO.write(rot, "png", new File("./img/player/layer1_" + angle + ".png"));
                System.out.println("Created layer1_" + angle + ".png");
            }
            System.out.println("Done!");
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }
}
