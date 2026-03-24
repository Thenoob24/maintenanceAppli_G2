import MG2D.*;
import MG2D.geometrie.*;

class Player {  
    private RotatingTexture tex;
    
    private double velocity;
    private double gravity;
    private boolean isJumping;

    /**
     * Constructeur par défaut. Initialise le joueur avec sa texture rotative
     * et ses paramètres physiques de base.
     */
    public Player() {
        this.tex = new RotatingTexture("./img/player/layer1.png", new Point(200, 100), 100, 100);
        this.velocity = 0;
        this.gravity = 1.0; 
        this.isJumping = false;
    }

    /**
     * Constructeur par copie.
     * @param a Le joueur à copier.
     */
    public Player(Player a) {
        this.tex = new RotatingTexture((RotatingTexture)a.getTex());
        this.velocity = a.getVelocity();
        this.gravity = a.getGravity();
        this.isJumping = a.isJumping();
    }
    
    // Hitbox principale (et texture visible)
    /**
     * Retourne la texture du joueur utilisée pour l'affichage et les collisions.
     * @return La RotatingTexture du joueur.
     */
    public Texture getTex() { return tex; }
    
    public double getVelocity() { return velocity; }
    public double getGravity() { return gravity; }
    public boolean isJumping() { return isJumping; }

    public void setVelocity(double velocity) { this.velocity = velocity; }
    public void setGravity(double gravity) { this.gravity = gravity; }
    public void setJumping(boolean jumping) { 
        this.isJumping = jumping; 
        if (!jumping) {
            double a = tex.getAngle();
            double nearest = Math.round(a / 90.0) * 90.0;
            tex.setAngle(nearest);
        }
    }

    /**
     * Gère l'entrée clavier pour déclencher un saut simple.
     * @param cla Le gestionnaire du clavier.
     * @param fen La fenêtre du jeu.
     */
    public void bougerJoueur(ClavierBorneArcade cla, Fenetre fen) {
        bougerJoueur(cla, fen, false);
    }

    /**
     * Gère l'entrée clavier pour déclencher un saut, en tenant compte de l'inversion de gravité.
     * @param cla Le gestionnaire du clavier.
     * @param fen La fenêtre du jeu.
     * @param gravityInverted Vrai si la gravité est actuellement inversée.
     */
    public void bougerJoueur(ClavierBorneArcade cla, Fenetre fen, boolean gravityInverted) {
        if (cla.getJoyJ1HautEnfoncee() && !isJumping) {
            velocity = gravityInverted ? -20.0 : 20.0;
            isJumping = true;
        }
    }

    /**
     * Déclenche un saut spécial (plus puissant) suite à l'activation d'une orbe.
     * @param gravityInverted Vrai si la gravité est inversée.
     */
    public void jumpOrb(boolean gravityInverted) {
        velocity = gravityInverted ? -25.0 : 25.0; // Boost un peu plus fort que le saut normal
        isJumping = true;
    }

    /**
     * Met à jour la physique du joueur (gravité et collision sol).
     * @param groundY Position Y du sol.
     */
    public void updatePhysics(int groundY) {
        updatePhysics(groundY, -1, false);
    }

    /**
     * Met à jour la physique complète du joueur.
     * @param groundY Position Y du sol.
     * @param ceilingY Position Y du plafond.
     * @param gravityInverted État de l'inversion de gravité.
     */
    public void updatePhysics(int groundY, int ceilingY, boolean gravityInverted) {
        if (gravityInverted) {
            velocity += gravity; // gravité inversée : accélère vers le haut
        } else {
            velocity -= gravity;
        }
        int dy = (int) velocity;
        
        tex.translater(0, dy);
        
        if (isJumping) {
            double rotDir = gravityInverted ? -4.5 : 4.5;
            tex.setAngle(tex.getAngle() + rotDir);
        }

        if (!gravityInverted) {
            // Gravité normale : sol en bas
            if (tex.getA().getY() <= groundY) {
                int correctif = groundY - tex.getA().getY();
                tex.translater(0, correctif);
                velocity = 0;
                setJumping(false);
            }
        } else {
            // Gravité inversée : plafond en haut
            if (tex.getA().getY() >= ceilingY) {
                int correctif = ceilingY - tex.getA().getY();
                tex.translater(0, correctif);
                velocity = 0;
                setJumping(false);
            }
        }
    }

    public String toString() {
        return "Player avec rotation continue. Velocite : " + velocity + " Gravite : " + gravity;
    }
}