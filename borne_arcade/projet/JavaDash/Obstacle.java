import MG2D.*;
import MG2D.Fenetre;
import MG2D.geometrie.*;
import MG2D.audio.player.JavaSoundAudioDevice;
import MG2D.audio.player.advanced.AdvancedPlayer;
import MG2D.audio.decoder.JavaLayerException;
import java.io.FileInputStream;
import java.util.ArrayList;

// Gestion des obstacles
    class Obstacle {
        Texture dessin;
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
                // APRÈS (7 paramètres avec isMirror)
        public Obstacle(Texture d, int startX, int startY, int vitesseY, int amplitudeY, boolean isSpike, boolean isMirror) {
            this.dessin = d;
            this.x = startX;
            this.y = startY;
            this.vy = vitesseY;
            this.minY = startY - amplitudeY;
            this.maxY = startY + amplitudeY;
            this.isSpike = isSpike;
            this.isMirror = isMirror; // ← ligne ajoutée
        }

        // Constructeur pour obstacles "pop-up"
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

        // Constructeur pour orbes
        public Obstacle(Texture d, int startX, int startY, boolean isOrb) {
            this.dessin = d;
            this.x = startX;
            this.y = startY;
            this.isOrb = isOrb;
            this.vy = 0;
        }

        public void update() {
            dessin.translater(-10, 0); // Vitesse horizontale
            x -= 10;
            if (vy != 0) {
                if (isPopUp) {
                    if (vy > 0 && y < maxY) {
                        y += vy;
                        dessin.translater(0, vy);
                        if (y >= maxY) {
                            dessin.translater(0, maxY - y);
                            y = maxY;
                            vy = 0; // Stop
                        }
                    } else if (vy < 0 && y > minY) {
                        y += vy;
                        dessin.translater(0, vy);
                        if (y <= minY) {
                            dessin.translater(0, minY - y);
                            y = minY;
                            vy = 0; // Stop
                        }
                    }
                } else {
                    y += vy;
                    dessin.translater(0, vy);
                    if (y <= minY || y >= maxY) {
                        vy = -vy; // Rebond
                    }
                }
            }
        }

            // Teste si un point (px, py) est dans le triangle défini par 3 sommets
    private boolean pointInTriangle(int px, int py,
                                    int ax, int ay,
                                    int bx, int by,
                                    int cx, int cy) {
        int d1 = (px - bx) * (ay - by) - (ax - bx) * (py - by);
        int d2 = (px - cx) * (by - cy) - (bx - cx) * (py - cy);
        int d3 = (px - ax) * (cy - ay) - (cx - ax) * (py - ay);
        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);
        return !(hasNeg && hasPos);
    }

    // Retourne true si le joueur (rectangle pX,pY,pW,pH) touche cet obstacle
    public boolean collidesWithPlayer(int pX, int pY, int pW, int pH) {
        int ow = dessin.getLargeur();
        int oh = dessin.getHauteur();
        if (!isSpike) {
            int ox = this.x, oy = this.y;
            return pX < ox + ow && pX + pW > ox && pY < oy + oh && pY + pH > oy;
        }
        // Spike sol : pointe vers le haut  → triangle : bas-gauche, bas-droite, sommet-milieu
        // Spike plafond : pointe vers le bas → idem mais vy inversé, on détecte via y
        int tx = this.x;
        int ty = this.y;
        boolean pointingUp = (vy >= 0); // spike sol pointe vers le haut

        int ax, ay, bx, by, cx, cy;
        if (pointingUp) {
            ax = tx;       ay = ty;        // bas-gauche
            bx = tx + ow;  by = ty;        // bas-droite
            cx = tx + ow/2; cy = ty + oh;   // pointe haute (tip up)
        } else {
            ax = tx;       ay = ty + oh;   // haut-gauche
            bx = tx + ow;  by = ty + oh;   // haut-droite
            cx = tx + ow/2; cy = ty;        // pointe basse (tip down)
        }

        // On teste les 4 coins du rectangle joueur contre le triangle
        return pointInTriangle(pX,      pY,      ax, ay, bx, by, cx, cy)
            || pointInTriangle(pX + pW, pY,      ax, ay, bx, by, cx, cy)
            || pointInTriangle(pX,      pY + pH, ax, ay, bx, by, cx, cy)
            || pointInTriangle(pX + pW, pY + pH, ax, ay, bx, by, cx, cy);
    }

    }
