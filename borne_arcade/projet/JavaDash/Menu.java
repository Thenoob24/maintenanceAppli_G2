import MG2D.*;
import MG2D.geometrie.*;
import java.awt.Font;

public class Menu {
    private Fenetre fen;
    private ClavierBorneArcade clavier;
    
    private Texture fond;
    private Texture[] sol;
    private RotatingTexture joueurTex;
    
    private Texte titre;
    private Texte sousTitre;

    public Menu(Fenetre f) {
        this.fen = f;
        clavier = new ClavierBorneArcade();
        fen.addKeyListener(clavier);
        fen.getP().addKeyListener(clavier);
        
        int w = fen.getWidth();
        int h = fen.getHeight();
        
        fond = new Texture("./img/background/Day/Background.png", new Point(0,0), w, h);
        fen.ajouter(fond);
        
        int nbTuiles = (w / 100) + 2;
        sol = new Texture[nbTuiles];
        for (int i = 0; i < nbTuiles; i++) {
            sol[i] = new Texture("./img/Tiles/Tile_05.png", new Point(i * 100, 0), 100, 100);
            fen.ajouter(sol[i]);
        }
        
        joueurTex = new RotatingTexture("./img/player/layer1.png", new Point((w/2) - 50, 100), 100, 100);
        fen.ajouter(joueurTex);
        
        Font policeTitre = new Font("Arial", Font.BOLD, 128);
        Font policeSousTitre = new Font("Arial", Font.ITALIC, 48);
        
        titre = new Texte(Couleur.BLANC, "JAVA DASH", policeTitre, new Point((w/2) - 350, (h/2) + 200));
        sousTitre = new Texte(Couleur.BLANC, "Appuyez sur HAUT pour jouer", policeSousTitre, new Point((w/2) - 350, (h/2) - 100));
        
        fen.ajouter(titre);
        fen.ajouter(sousTitre);
    }
    
    public int afficherMenu() {
        int etat = 0;
        int frameCount = 0;
        double baseVel = 14.0;
        double vel = baseVel;
        
        while (etat == 0 && fen.isVisible()) {
            frameCount++;
            
            if (frameCount % 60 == 0) {
                fen.supprimer(sousTitre);
            } else if (frameCount % 60 == 30) {
                fen.ajouter(sousTitre);
            }
            
            vel -= 0.5;
            int dy = (int) vel;
            joueurTex.translater(0, dy);
            
            if (joueurTex.getA().getY() <= 100) {
                int correctif = 100 - joueurTex.getA().getY();
                joueurTex.translater(0, correctif);
                vel = baseVel;
                joueurTex.setAngle(Math.round(joueurTex.getAngle() / 90.0) * 90.0);
            } else {
                joueurTex.setAngle(joueurTex.getAngle() + 4.0);
            }
            
            if (clavier.getJoyJ1HautEnfoncee() || clavier.getBoutonJ1AEnfoncee() || clavier.getBoutonJ1BEnfoncee()) {
                etat = 1;
            }
            
            fen.rafraichir();
            
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {}
        }
        
        return etat;
    }
    
    public void effacerMenu() {
        fen.supprimer(fond);
        for(Texture t : sol) fen.supprimer(t);
        fen.supprimer(joueurTex);
        fen.supprimer(titre);
        fen.supprimer(sousTitre);
        fen.removeKeyListener(clavier);
        fen.getP().removeKeyListener(clavier);
    }
}
