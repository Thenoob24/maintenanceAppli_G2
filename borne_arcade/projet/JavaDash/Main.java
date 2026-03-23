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

                // 2 = victoire, 3 = défaite
                // Dans les deux cas on retourne au menu,
                // mais tu peux ajouter un écran intermédiaire ici si besoin
                if (finDuJeu == 2) {
                    // Victoire : l'écran a déjà été affiché 3s dans Jeu.java
                    // On revient simplement au menu
                } else if (finDuJeu == 3) {
                    // Défaite : pareil, retour menu
                    // Tu peux afficher un Game Over ici si tu veux
                }

            } else {
                break;
            }
        }

        System.exit(0);
    }
}