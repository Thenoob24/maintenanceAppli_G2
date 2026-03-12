class Main{
    public static void main(String[] args){
        Jeu javaDash = new Jeu();
        int finDuJeu = 0;

        javaDash.CreationJeu();

        while(finDuJeu != 1){
            javaDash.NewGame(finDuJeu);
        }
    }
}