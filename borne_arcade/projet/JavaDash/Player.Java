import MG2D.*;
import MG2D.geometrie.*;

class Player {  
    
    /* ------- Attributs ------- */
    private Texture tex;
    private int velocity;
    private double gravity;

    /* ------- Constructeurs ------- */
    public Player() {
        tex = new Texture("./img/player/img1.png");
        velocity = 0;
        gravity = 0;
    }

    public Player(Player a) {
        tex = new Texture(a.getTex());
        velocity = a.getVelocity();
        gravity = a.getGravity();
    }

    public Player(Texture tex, int velocity, double gravity) {
        this.tex = new Texture(tex);  
        this.velocity = velocity;
        this.gravity = gravity;
    }

    /* ------- Getters & Setters ------- */
    public Texture getTex() {
        return tex;
    }

    public int getVelocity() {
        return velocity;
    }

    public double getGravity() { 
        return gravity;
    }

    public void setTex(Texture tex) {  
        this.tex = tex;
    }

    public void setVelocity(int velocity) { 
        this.velocity = velocity;
    }

    public void setGravity(double gravity) {  
        this.gravity = gravity;
    }

    /* ------- Méthodes ------- */
    public void bougerJoueur(ClavierBorneArcade cla, int yMin, int yMax, int yDecalage, int largeurJoueur, int hauteurJoueur) {
        if (cla.getJoyJ1HautEnfoncee() && tex.getA().getY() < (yMax - hauteurJoueur - 5) + 152) {
            tex.translater(0, +yDecalage);
        }
    }

    public String toString() {
        return "Texture : " + tex + "\n Vélocité : " + velocity + "\n Gravité : " + gravity;

    }

    public boolean equals(Player j) { 
        return (tex.equals(j.getTex())) && (velocity == j.getVelocity()) && (gravity == j.getGravity());

    }
}