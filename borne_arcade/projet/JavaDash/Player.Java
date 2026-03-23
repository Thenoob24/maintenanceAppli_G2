import MG2D.*;
import MG2D.geometrie.*;

class Player {  
    private RotatingTexture tex;
    
    private double velocity;
    private double gravity;
    private boolean isJumping;

    public Player() {
        this.tex = new RotatingTexture("./img/player/layer1.png", new Point(200, 100), 100, 100);
        this.velocity = 0;
        this.gravity = 1.0; 
        this.isJumping = false;
    }

    public Player(Player a) {
        this.tex = new RotatingTexture((RotatingTexture)a.getTex());
        this.velocity = a.getVelocity();
        this.gravity = a.getGravity();
        this.isJumping = a.isJumping();
    }
    
    // Hitbox principale (et texture visible)
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

    public void bougerJoueur(ClavierBorneArcade cla, Fenetre fen) {
        if (cla.getJoyJ1HautEnfoncee() && !isJumping) {
            velocity = 20.0;
            isJumping = true;
            // On laisse l'angle filer pour qu'il tourne progressivement
        }
    }

    public void updatePhysics(int groundY) {
        velocity -= gravity;
        int dy = (int) velocity;
        
        tex.translater(0, dy);
        
        if (isJumping) {
            // Tourne progressivement en vol (environ 4.5 degres par frame pour 180 degres total)
            tex.setAngle(tex.getAngle() + 4.5);
        }

        if (tex.getA().getY() <= groundY) {
            int correctif = groundY - tex.getA().getY();
            tex.translater(0, correctif);
            velocity = 0;
            setJumping(false);
        }
    }

    public String toString() {
        return "Player avec rotation continue. Velocite : " + velocity + " Gravite : " + gravity;
    }
}