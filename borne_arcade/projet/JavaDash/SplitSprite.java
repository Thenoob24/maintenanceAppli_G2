import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Classe utilitaire pour découper une planche de sprites en couches individuelles.
 * Utilisée spécifiquement pour l'animation de la balle du joueur.
 */
public class SplitSprite {
    /**
     * Méthode principale qui lit la planche de sprites du joueur et sauvegarde chaque couche.
     * @param args Arguments de la ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        try {
            File f = new File("./img/player/player_ball_62-uhd.png");
            if (!f.exists()) {
                System.out.println("Image not found!");
                return;
            }
            BufferedImage img = ImageIO.read(f);
            
            int w = img.getWidth() / 4;
            int h = img.getHeight();

            for (int i = 0; i < 4; i++) {
                BufferedImage part = img.getSubimage(i * w, 0, w, h);
                ImageIO.write(part, "png", new File("./img/player/layer" + (i+1) + ".png"));
                System.out.println("Created layer" + (i+1) + ".png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
