import MG2D.*;
import MG2D.geometrie.*;

class Player {  
    private Texture tex;
    private double velocity;
    private double gravity;
    private boolean isJumping;

    public Player() {
        this.tex = new Texture("./img/player/img1.png", new Point(200, 100), 100, 100);
        this.velocity = 0;
        this.gravity = 1.0; 
        this.isJumping = false;
    }

    public Player(Player a) {
        this.tex = new Texture(a.getTex());
        this.velocity = a.getVelocity();
        this.gravity = a.getGravity();
        this.isJumping = a.isJumping();
    }

    public Player(Texture tex, double velocity, double gravity) {
        this.tex = new Texture(tex);  
        this.velocity = velocity;
        this.gravity = gravity;
        this.isJumping = false;
    }

    public Texture getTex() { return tex; }
    public double getVelocity() { return velocity; }
    public double getGravity() { return gravity; }
    public boolean isJumping() { return isJumping; }

    public void setTex(Texture tex) { this.tex = tex; }
    public void setVelocity(double velocity) { this.velocity = velocity; }
    public void setGravity(double gravity) { this.gravity = gravity; }
    public void setJumping(boolean jumping) { this.isJumping = jumping; }

    public void bougerJoueur(ClavierBorneArcade cla) {
        if (cla.getJoyJ1HautEnfoncee() && !isJumping) {
            velocity = 20.0;
            isJumping = true;
        }
    }

    public void updatePhysics(int groundY) {
        velocity -= gravity;
        int dy = (int) velocity;
        tex.translater(0, dy);

        if (tex.getA().getY() <= groundY) {
            tex.translater(0, groundY - tex.getA().getY());
            velocity = 0;
            isJumping = false;
        }
    }

    public String toString() {
        return "Texture : " + tex + "\n Vélocité : " + velocity + "\n Gravité : " + gravity;
    }

    public boolean equals(Player j) { 
        return (tex.equals(j.getTex())) && (velocity == j.getVelocity()) && (gravity == j.getGravity());
    }
}