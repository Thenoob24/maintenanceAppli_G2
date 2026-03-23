import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class FlipAndSave {
    public static void main(String[] args) {
        String[] files = {
            "./img/Tiles/Tile_02.png",
            "./img/ennemis/spike.png",
            "./img/Tiles/Tile_05.png"
        };
        for (String file : files) {
            try {
                File f = new File(file);
                if (!f.exists()) {
                    System.out.println("Skipped: " + file);
                    continue;
                }
                BufferedImage image = ImageIO.read(f);
                
                // Rotate 180 degrees instead of vertical flip
                AffineTransform tx = AffineTransform.getRotateInstance(Math.PI, image.getWidth(null) / 2.0, image.getHeight(null) / 2.0);
                AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                image = op.filter(image, null);
                
                String newName = file.replace(".png", "_flip.png");
                ImageIO.write(image, "PNG", new File(newName));
                System.out.println("Created: " + newName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
