import MG2D.FenetrePleinEcran;

class Main {
    public static void main(String[] args) {
        FenetrePleinEcran fen = new FenetrePleinEcran("JavaDash");
        
        while (true) {
            Menu menu = new Menu(fen);
            int choix = menu.afficherMenu();

            if (choix == 1) {
                menu.effacerMenu();
                
                Jeu javaDash = new Jeu();
                int finDuJeu = 0;

                javaDash.CreationJeu(fen);

                while (finDuJeu == 0) {
                    finDuJeu = javaDash.NewGame(finDuJeu);
                }
                
                javaDash.effacerJeu();
            } else {
                break;
            }
        }
        
        System.exit(0);
    }
}