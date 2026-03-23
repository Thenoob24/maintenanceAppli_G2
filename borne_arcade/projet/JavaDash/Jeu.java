import MG2D.*;
import MG2D.Fenetre;
import MG2D.geometrie.*;
import MG2D.audio.player.JavaSoundAudioDevice;
import MG2D.audio.player.advanced.AdvancedPlayer;
import MG2D.audio.decoder.JavaLayerException;
import java.io.FileInputStream;
import java.util.ArrayList;

class Jeu {
    private Fenetre fen;
    private Texture fond1;
    private Texture fond2;
    private Texture fond3;
    private Texture sol1;
    private Texture sol2;
    private Texture sol3;
    private Texture plafond1;
    private Texture plafond2;
    private Texture plafond3;
    private MyAudioDevice audioDevice;
    private AdvancedPlayer player;
    private ClavierBorneArcade clavier;
    private Player joueur;

    // Classe interne pour surcharger le lecteur et intercepter l'amplitude (sans
    // modifier MG2D !)
    class MyAudioDevice extends JavaSoundAudioDevice {
        public volatile float currentAmplitude = 0;

        @Override
        protected void writeImpl(short[] samples, int offs, int len) throws JavaLayerException {
            long sum = 0;
            for (int i = 0; i < len; i++) {
                sum += (long) samples[offs + i] * (long) samples[offs + i];
            }
            if (len > 0) {
                currentAmplitude = (float) Math.sqrt(sum / len);
            } else {
                currentAmplitude = 0;
            }
            super.writeImpl(samples, offs, len);
        }
    }

    // Gestion des obstacles
    class Obstacle {
        Dessin dessin;
        int x;
        int y;
        int vy;
        int minY;
        int maxY;
        boolean isSpike;

        public Obstacle(Dessin d, int startX, int startY, int vitesseY, int amplitudeY, boolean isSpike) {
            this.dessin = d;
            this.x = startX;
            this.y = startY;
            this.vy = vitesseY;
            this.minY = startY - amplitudeY;
            this.maxY = startY + amplitudeY;
            this.isSpike = isSpike;
        }

        public void update() {
            dessin.translater(-10, 0); // Vitesse horizontale
            x -= 10;
            if (vy != 0) {
                y += vy;
                dessin.translater(0, vy);
                if (y <= minY || y >= maxY) {
                    vy = -vy; // Rebond
                }
            }
        }
    }

    private ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    private int cooldown = 0;

    private int lastPlatformY = 100;
    private int consecutivePlatforms = 0;
    
    private Rectangle flashScreen;
    private int flashTime = 0;

    public void CreationJeu() {
        fen = new FenetrePleinEcran("JavaDash");
        fond1 = new Texture("./img/background/Day/Background.png", new Point(0, 0), 1280, 1024);
        fond2 = new Texture("./img/background/Day/Background.png", new Point(1280, 0), 1280, 1024);
        fond3 = new Texture("./img/background/Day/Background.png", new Point(2560, 0), 1280, 1024);

        // Ground textures (sol)
        sol1 = new Texture("./img/Tiles/Tile_02.png", new Point(0, 0), 1280, 100);
        sol2 = new Texture("./img/Tiles/Tile_02.png", new Point(1280, 0), 1280, 100);
        sol3 = new Texture("./img/Tiles/Tile_02.png", new Point(2560, 0), 1280, 100);
        
        // Ceiling textures (plafond)
        plafond1 = new Texture("./img/Tiles/Tile_02_flip.png", new Point(0, 924), 1280, 100);
        plafond2 = new Texture("./img/Tiles/Tile_02_flip.png", new Point(1280, 924), 1280, 100);
        plafond3 = new Texture("./img/Tiles/Tile_02_flip.png", new Point(2560, 924), 1280, 100);

        // Strobing light effect (Effet Lumineux)
        flashScreen = new Rectangle(MG2D.geometrie.Couleur.BLANC, new Point(0, 0), 1280, 1024, true);

        fen.ajouter(fond1);
        fen.ajouter(fond2);
        fen.ajouter(fond3);
        fen.ajouter(sol1);
        fen.ajouter(sol2);
        fen.ajouter(sol3);
        fen.ajouter(plafond1);
        fen.ajouter(plafond2);
        fen.ajouter(plafond3);

        clavier = new ClavierBorneArcade();
        fen.addKeyListener(clavier);
        fen.getP().addKeyListener(clavier);

        joueur = new Player();
        fen.ajouter(joueur.getTex());

        // Charger la musique via notre lecteur custom
        try {
            audioDevice = new MyAudioDevice();
            FileInputStream fis = new FileInputStream("./sound/PressStart.mp3");
            player = new AdvancedPlayer(fis, audioDevice);

            // Lancer la musique dans un thread séparé pour ne pas bloquer le jeu
            new Thread(new Runnable() {
                public void run() {
                    try {
                        player.play();
                    } catch (Exception e) {
                    }
                }
            }).start();
        } catch (Exception e) {
            System.out.println("Musique non trouvée, le jeu continue sans.");
        }

        fen.setVisible(true);
        fen.rafraichir();
    }

    public int NewGame(int game) {
        try {
            Thread.sleep(16); // ~60 FPS
        } catch (InterruptedException e) {
        }

        // Déplacement du fond
        fond1.translater(-5, 0);
        fond2.translater(-5, 0);
        fond3.translater(-5, 0);

        if (fond1.getA().getX() <= -1280) {
            fond1.translater(3840, 0);
        }
        if (fond2.getA().getX() <= -1280) {
            fond2.translater(3840, 0);
        }
        if (fond3.getA().getX() <= -1280) {
            fond3.translater(3840, 0);
        }

        // Déplacement du sol (vitesse double, comme les obstacles)
        sol1.translater(-10, 0);
        sol2.translater(-10, 0);
        sol3.translater(-10, 0);

        if (sol1.getA().getX() <= -1280)
            sol1.translater(3840, 0);
        if (sol2.getA().getX() <= -1280)
            sol2.translater(3840, 0);
        if (sol3.getA().getX() <= -1280)
            sol3.translater(3840, 0);

        // Déplacement du plafond 
        plafond1.translater(-10, 0);
        plafond2.translater(-10, 0);
        plafond3.translater(-10, 0);
        
        if (plafond1.getA().getX() <= -1280) plafond1.translater(3840, 0);
        if (plafond2.getA().getX() <= -1280) plafond2.translater(3840, 0);
        if (plafond3.getA().getX() <= -1280) plafond3.translater(3840, 0);

        // Logique d'apparition en fonction du rythme (amplitude interceptée)
        if (cooldown > 0)
            cooldown--;
        float amp = (audioDevice != null) ? audioDevice.currentAmplitude : 0;

        // Un pic d'amplitude > 6000 est en général un marqueur de grosse basse
        if (amp > 6000 && cooldown == 0) {
            if (Math.random() > 0.5) {
                // Spike logic
                consecutivePlatforms = 0; // Réinitialise l'escalier

                int nbSpikes = 1;
                if (amp > 12000)
                    nbSpikes = 3;
                else if (amp > 9000)
                    nbSpikes = 2;

                for (int s = 0; s < nbSpikes; s++) {
                    int startY = 100;
                    int vitY = 0;
                    int ampY = 0;
                    if (amp > 15000 && Math.random() > 0.4) {
                        startY = 150 + (int) (Math.random() * 200);
                        vitY = 5;
                        ampY = 100;
                    }
                    Texture spike = new Texture("./img/ennemis/spike.png", new Point(1280 + s * 100, startY), 100, 100);
                    obstacles.add(new Obstacle(spike, 1280 + s * 100, startY, vitY, ampY, true));
                    fen.ajouter(spike);
                    
                    // Miroir plafond
                    int mirrorY = 1024 - startY - 100;
                    Texture spikeMir = new Texture("./img/ennemis/spike_flip.png", new Point(1280 + s * 100, mirrorY), 100, 100);
                    obstacles.add(new Obstacle(spikeMir, 1280 + s * 100, mirrorY, -vitY, ampY, true));
                    fen.ajouter(spikeMir);
                }

                // Le cooldown dépend du nombre de spikes + un espacement pour que le saut soit possible
                cooldown = nbSpikes * 10 + 20;
            } else {
                // Platform logic (en cube de 100x100, formant un escalier)
                consecutivePlatforms++;
                if (consecutivePlatforms == 1) {
                    lastPlatformY = 100; // Première hauteur au niveau du sol (comme les spikes)
                } else {
                    lastPlatformY += 100; // Monte d'un bloc de 100
                    if (consecutivePlatforms > 2) { // Limite maximale de 2 cubes d'escalier
                        lastPlatformY = 100;
                        consecutivePlatforms = 1;
                    }
                }

                int vitY = 0;
                int ampY = 0;
                // Effet : plateforme mobile si l'amplitude est massive (et qu'elle est isolée)
                if (amp > 16000 && consecutivePlatforms == 1) {
                    vitY = 3;
                    ampY = 150;
                    lastPlatformY = 300; // Force une hauteur de départ plus élevée pour l'amplitude de mouvement
                }
                
                // Normal
                Texture plat = new Texture("./img/Tiles/Tile_05.png", new Point(1280, lastPlatformY), 100, 100);
                obstacles.add(new Obstacle(plat, 1280, lastPlatformY, vitY, ampY, false));
                fen.ajouter(plat);
                
                // Miroir plafond
                int mirrorY = 1024 - lastPlatformY - 100;
                Texture platMir = new Texture("./img/Tiles/Tile_05_flip.png", new Point(1280, mirrorY), 100, 100);
                obstacles.add(new Obstacle(platMir, 1280, mirrorY, -vitY, ampY, false));
                fen.ajouter(platMir);

                // 10 frames à -10 pixels/frame = 100 pixels. Les cubes collés
                cooldown = 10;
            }
        }

        // Effets Lumineux ! Un flash d'écran si l'amplitude est hardcore
        if (amp > 18000 && flashTime <= 0) {
            flashTime = 2; // 2 frames de flash blanc éblouissant
            fen.ajouter(flashScreen);
        }
        if (flashTime > 0) {
            flashTime--;
            if (flashTime == 0) {
                fen.supprimer(flashScreen);
            }
        }

        // Mise à jour des obstacles
        boolean collisionTop = false;

        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Obstacle obs = obstacles.get(i);
            obs.update();

            if (obs.x < -200) {
                fen.supprimer(obs.dessin);
                obstacles.remove(i);
            } else if (joueur.getTex().intersection(obs.dessin)) {
                if (obs.isSpike) {
                    System.out.println("BOOM! Spike hit.");
                    return 3;
                } else {
                    int pY = joueur.getTex().getA().getY();
                    int obsTop = ((Texture)obs.dessin).getA().getY() + 100; // haut de la plateforme
                    
                    // Si le joueur tombe et son centre est au dessus de la plateforme (tolérance)
                    if (joueur.getVelocity() < 0 && (pY + 25) >= obsTop) {
                        joueur.getTex().translater(0, obsTop - pY);
                        joueur.setVelocity(0);
                        joueur.setJumping(false);
                        collisionTop = true;
                    } else if (pY < obsTop - 30) {
                        // Impact sur le mur vertical de la plateforme
                        System.out.println("CRASH! Platform Wall hit.");
                        return 3; 
                    }
                }
            }
        }
        
        // Physique du joueur
        joueur.bougerJoueur(clavier);
        joueur.updatePhysics(100);

        fen.rafraichir();
        return game = 1;
    }
}