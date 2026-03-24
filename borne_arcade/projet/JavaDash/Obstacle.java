import MG2D.*;
import MG2D.Fenetre;
import MG2D.geometrie.Dessin;
import MG2D.geometrie.Cercle;
import MG2D.geometrie.Point;
import MG2D.geometrie.Texture;
import MG2D.geometrie.Couleur;
import MG2D.audio.player.JavaSoundAudioDevice;
import MG2D.audio.player.advanced.AdvancedPlayer;
import MG2D.audio.decoder.JavaLayerException;
import java.io.FileInputStream;
import java.util.ArrayList;

// Gestion des obstacles
class Obstacle {
    Dessin dessin;
    Cercle onde;
    int x;
    int y;
    int vy;
    int minY;
    int maxY;
    boolean isSpike;
    boolean isMirror;
    boolean isPopUp = false;
    boolean isOrb = false;
    boolean orbUsed = false;

    public Obstacle(Texture d, int startX, int startY, int vitesseY, int amplitudeY, boolean isSpike) {
        this.dessin = d;
        this.x = startX;
        this.y = startY;
        this.vy = vitesseY;
        this.minY = startY - amplitudeY;
        this.maxY = startY + amplitudeY;
        this.isSpike = isSpike;
    }

    public Obstacle(Texture d, int startX, int startY, int vitesseY, int amplitudeY, boolean isSpike, boolean isMirror) {
        this.dessin = d;
        this.x = startX;
        this.y = startY;
        this.vy = vitesseY;
        this.minY = startY - amplitudeY;
        this.maxY = startY + amplitudeY;
        this.isSpike = isSpike;
        this.isMirror = isMirror;
    }

    public Obstacle(Texture d, int startX, int startY, int vitesseY, int targetY, boolean isSpike, boolean isMirror, boolean isPopUp) {
        this.dessin = d;
        this.x = startX;
        this.y = startY;
        this.vy = vitesseY;
        this.isSpike = isSpike;
        this.isMirror = isMirror;
        this.isPopUp = isPopUp;
        if (vitesseY > 0) {
            this.maxY = targetY;
            this.minY = startY;
        } else {
            this.minY = targetY;
            this.maxY = startY;
        }
    }

    public Obstacle(Texture d, int startX, int startY, boolean isOrb) {
        this.dessin = d;
        this.x = startX;
        this.y = startY;
        this.isOrb = isOrb;
        this.vy = 0;
    }

    public Obstacle(Cercle c, int startX, int startY, boolean isOrb) {
        this.dessin = c;
        this.x = startX;
        this.y = startY;
        this.isOrb = isOrb;
        this.vy = 0;
        if (isOrb) {
            this.onde = new Cercle(Couleur.JAUNE, c.getO(), c.getRayon(), false);
        }
    }

    public void update() {
        update(0);
    }

    public void update(float intensity) {
        dessin.translater(-10, 0); // Vitesse horizontale
        if (onde != null) {
            onde.translater(-10, 0);
            int baseRadius = ((Cercle) dessin).getRayon();
            onde.setRayon(baseRadius + (int) (intensity * 60));
        }
        x -= 10;
        if (vy != 0) {
            if (isPopUp) {
                if (vy > 0 && y < maxY) {
                    y += vy;
                    dessin.translater(0, vy);
                    if (onde != null)
                        onde.translater(0, vy);
                    if (y >= maxY) {
                        dessin.translater(0, maxY - y);
                        if (onde != null)
                            onde.translater(0, maxY - y);
                        y = maxY;
                        vy = 0; // Stop
                    }
                } else if (vy < 0 && y > minY) {
                    y += vy;
                    dessin.translater(0, vy);
                    if (onde != null)
                        onde.translater(0, vy);
                    if (y <= minY) {
                        dessin.translater(0, minY - y);
                        if (onde != null)
                            onde.translater(0, minY - y);
                        y = minY;
                        vy = 0; // Stop
                    }
                }
            } else {
                y += vy;
                dessin.translater(0, vy);
                if (onde != null)
                    onde.translater(0, vy);
                if (y <= minY || y >= maxY) {
                    vy = -vy; // Rebond
                }
            }
        }
    }

    private boolean pointInTriangle(int px, int py, int ax, int ay, int bx, int by, int cx, int cy) {
        int d1 = (px - bx) * (ay - by) - (ax - bx) * (py - by);
        int d2 = (px - cx) * (by - cy) - (bx - cx) * (py - cy);
        int d3 = (px - ax) * (cy - ay) - (cx - ax) * (py - ay);
        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);
        return !(hasNeg && hasPos);
    }

    public boolean collidesWithPlayer(int pX, int pY, int pW, int pH) {
        int ow, oh;
        if (dessin instanceof Texture) {
            ow = ((Texture) dessin).getLargeur();
            oh = ((Texture) dessin).getHauteur();
        } else if (dessin instanceof Cercle) {
            ow = ((Cercle) dessin).getRayon() * 2;
            oh = ((Cercle) dessin).getRayon() * 2;
        } else {
            // Fallback for generic Dessin if methods are available
            ow = 80;
            oh = 80;
        }

        if (!isSpike) {
            int ox = this.x, oy = this.y;
            if (dessin instanceof Cercle) {
                // For circle, x,y is center in MG2D (based on Pong.java)
                // but we store startX, startY in Obstacle.
                // Let's adjust ox, oy to be bottom-left for collision
                ox = this.x - ow / 2;
                oy = this.y - oh / 2;
            }
            return pX < ox + ow && pX + pW > ox && pY < oy + oh && pY + pH > oy;
        }

        int tx = this.x;
        int ty = this.y;
        boolean pointingUp = (vy >= 0);

        int ax, ay, bx, by, cx, cy;
        if (pointingUp) {
            ax = tx; ay = ty;
            bx = tx + ow; by = ty;
            cx = tx + ow / 2; cy = ty + oh;
        } else {
            ax = tx; ay = ty + oh;
            bx = tx + ow; by = ty + oh;
            cx = tx + ow / 2; cy = ty;
        }

        return pointInTriangle(pX, pY, ax, ay, bx, by, cx, cy)
                || pointInTriangle(pX + pW, pY, ax, ay, bx, by, cx, cy)
                || pointInTriangle(pX, pY + pH, ax, ay, bx, by, cx, cy)
                || pointInTriangle(pX + pW, pY + pH, ax, ay, bx, by, cx, cy);
    }
}
