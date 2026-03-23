import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import MG2D.geometrie.Point;
import MG2D.geometrie.Texture;

public class RotatingTexture extends Texture {
    private double angleDegrees = 0;

    public RotatingTexture(String chemin, Point a, int larg, int haut) {
        super(chemin, a, larg, haut);
    }
    
    public RotatingTexture(RotatingTexture rt) {
        super(rt);
        this.angleDegrees = rt.angleDegrees;
    }

    public void setAngle(double angle) {
        this.angleDegrees = angle;
    }

    public double getAngle() {
        return this.angleDegrees;
    }

    @Override
    public void afficher(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        
        int x = this.getA().getX();
        int y = (int) g.getClipBounds().getHeight() - this.getA().getY() - getHauteur();
        int w = getLargeur();
        int h = getHauteur();
        
        AffineTransform at = new AffineTransform();
        // Pivot au centre de l'image
        at.translate(x + w / 2.0, y + h / 2.0);
        at.rotate(Math.toRadians(angleDegrees));
        at.translate(-x - w / 2.0, -y - h / 2.0);
        
        g2d.transform(at);
        
        // On dessine l'hote
        if (getTransparent()) {
            g2d.drawImage(getImg(), x, y, w, h, null);
        } else {
            g2d.drawImage(getImg(), x, y, w, h, getCouleur(), null);
        }
        
        g2d.dispose();
    }
}
