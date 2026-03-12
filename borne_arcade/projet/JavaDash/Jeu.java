import MG2D.*;
import MG2D.Fenetre;
import MG2D.geometrie.*;

class Jeu{
    private Fenetre fen;
    private Texture fond1;

    public void CreationJeu(){
        fen = new FenetrePleinEcran ("JavaDash");
        fond1 = new Texture("./img/background/Day/Background.png", new Point(0, 0), 1280, 1024);
        fen.ajouter(fond1);    
        fen.setVisible(true);
        fen.rafraichir();
    }

    public int NewGame(int game){
        return game = 1 ;
    }

}