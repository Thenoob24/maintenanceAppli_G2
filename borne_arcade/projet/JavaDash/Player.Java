import MG2D.*;
import MG2D.geometrie.*;

class Player(){
    
    /*------- Attribut ------- */
    private Texture tex;
    private int Velocity;
    private double gravity;

    /*------- Constructeur ------- */ 
    public Player(){ //Défault
        tex = new Texture("./img/player/img1.png");
        Velocity = 0;
        gravity = 0;
    }

    public Player(Player a){ //copie
    tex = new Texture (a.getTex());
    Velocity = a.getVelocity();
    gravity = a.getGravity();
    }

    public Player(Texture tex, int Velocity, double gravity){ //choix
        tex = new Texture(tex);
        this.Velocity = Velocity; 
        this.gravity = gravity;
    }

    /*------- Get && Set ------- */
    public Texture getTex(){
        return tex;
    }

    public int getVelocity(){
        return Velocity;
    }

    public int getGravity(){
        return gravity;
    }

    public void setTex(){
        this.tex = tex;
    }

    public void setVelocity(){
        this.Velocity = Velocity;
    }

    public void setGravity(){
        this.gravity = gravity;
    }


    /*------- Méthode ------- */

    public String toString(){
        return "Texture : "+tex+"\n Vélocité : "+Velocity+ "\n Gravité : "+gravity;
    }
}