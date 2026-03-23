import MG2D.*;
import MG2D.Fenetre;
import MG2D.geometrie.*;

class Jeu {
    private Fenetre fen;
    private Texture fond1;
    private Texture fond2;

    public void CreationJeu() {
        fen = new FenetrePleinEcran("JavaDash");
        fond1 = new Texture("./img/background/Day/Background.png", new Point(0, 0), 1280, 1024);
        fond2 = new Texture("./img/background/Day/Background.png", new Point(1280, 0), 1280, 1024);
        fen.ajouter(fond1);
        fen.ajouter(fond2);
        fen.setVisible(true);
        fen.rafraichir();

    }

    public int NewGame(int game) {
        try {
            Thread.sleep(16); // Pour limiter à environ 60 images par seconde
        } catch (InterruptedException e) {}

        // Déplacement des deux fonds vers la gauche
        fond1.translater(-5, 0);
        fond2.translater(-5, 0);

        // Replacement des fonds s'ils sortent de l'écran par la gauche
        if (fond1.getA().getX() <= -1280) {
            fond1.translater(2560, 0); // 1280 * 2
        }
        if (fond2.getA().getX() <= -1280) {
            fond2.translater(2560, 0);
        }

        // Rafraichir l'affichage avec la nouvelle position
        fen.rafraichir();

        return game = 1 ;
    }

}