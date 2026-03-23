class Main {
    public static void main(String[] args) {
        Jeu javaDash = new Jeu();
        int finDuJeu = 0;
        javaDash.CreationJeu();
        while (finDuJeu != 3) {
            finDuJeu = javaDash.NewGame(finDuJeu);
        }
        System.out.println("Game Over !");
    }
}