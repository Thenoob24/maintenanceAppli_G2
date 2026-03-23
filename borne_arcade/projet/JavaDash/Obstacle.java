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
        Dessin dessin;
        int x;
        int y;
        int vy;
        int minY;
        int maxY;
        boolean isSpike;
        boolean isMirror;

        public Obstacle(Dessin d, int startX, int startY, int vitesseY, int amplitudeY, boolean isSpike) {
            this.dessin = d;
            this.x = startX;
            this.y = startY;
            this.vy = vitesseY;
            this.minY = startY - amplitudeY;
            this.maxY = startY + amplitudeY;
            this.isSpike = isSpike;
        }
                // APRÈS (7 paramètres avec isMirror)
        public Obstacle(Dessin d, int startX, int startY, int vitesseY, int amplitudeY, boolean isSpike, boolean isMirror) {
            this.dessin = d;
            this.x = startX;
            this.y = startY;
            this.vy = vitesseY;
            this.minY = startY - amplitudeY;
            this.maxY = startY + amplitudeY;
            this.isSpike = isSpike;
            this.isMirror = isMirror; // ← ligne ajoutée
        }

        public void update() {
            dessin.translater(-10, 0); // Vitesse horizontale
            x -= 10;
            if (vy != 0) {
                y += vy;
                dessin.translater(0, vy);
                if (y <= minY || y >= maxY) {
                    vy = -vy; // Rebond
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
        if (!isSpike) {
            int ox = this.x, oy = this.y;
            return pX < ox + 100 && pX + pW > ox && pY < oy + 100 && pY + pH > oy;
        }
        // Spike sol : pointe vers le haut  → triangle : bas-gauche, bas-droite, sommet-milieu
        // Spike plafond : pointe vers le bas → idem mais vy inversé, on détecte via y
        int tx = this.x;
        int ty = this.y;
        boolean pointingUp = (vy >= 0); // spike sol pointe vers le haut

        int ax, ay, bx, by, cx, cy;
        if (pointingUp) {
            ax = tx;       ay = ty + 100; // bas-gauche
            bx = tx + 100; by = ty + 100; // bas-droite
            cx = tx + 50;  cy = ty;       // pointe haute
        } else {
            ax = tx;       ay = ty;        // haut-gauche
            bx = tx + 100; by = ty;        // haut-droite
            cx = tx + 50;  cy = ty + 100;  // pointe basse
        }

        // On teste les 4 coins du rectangle joueur contre le triangle
        return pointInTriangle(pX,      pY,      ax, ay, bx, by, cx, cy)
            || pointInTriangle(pX + pW, pY,      ax, ay, bx, by, cx, cy)
            || pointInTriangle(pX,      pY + pH, ax, ay, bx, by, cx, cy)
            || pointInTriangle(pX + pW, pY + pH, ax, ay, bx, by, cx, cy);
    }

    }
