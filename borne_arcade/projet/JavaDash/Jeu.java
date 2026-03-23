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
    private int W;
    private int H;
    
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

    private ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    private int cooldown = 0;
    private int lastPlatformY = 100;
    private int consecutivePlatforms = 0;
    private int consecutiveSpikes = 0;
    private float meanAmplitude = 5000;
    
    private Rectangle flashScreen;
    private int flashTime = 0;

    public void CreationJeu(Fenetre f) {
        fen = f;
        W = fen.getWidth();
        H = fen.getHeight();
        
        fond1 = new Texture("./img/background/Day/Background.png", new Point(0, 0), W, H);
        fond2 = new Texture("./img/background/Day/Background.png", new Point(W, 0), W, H);
        fond3 = new Texture("./img/background/Day/Background.png", new Point(W*2, 0), W, H);

        sol1 = new Texture("./img/Tiles/Tile_02.png", new Point(0, 0), W, 100);
        sol2 = new Texture("./img/Tiles/Tile_02.png", new Point(W, 0), W, 100);
        sol3 = new Texture("./img/Tiles/Tile_02.png", new Point(W*2, 0), W, 100);
        
        plafond1 = new Texture("./img/Tiles/Tile_02_flip.png", new Point(0, H - 100), W, 100);
        plafond2 = new Texture("./img/Tiles/Tile_02_flip.png", new Point(W, H - 100), W, 100);
        plafond3 = new Texture("./img/Tiles/Tile_02_flip.png", new Point(W*2, H - 100), W, 100);

        flashScreen = new Rectangle(MG2D.geometrie.Couleur.BLANC, new Point(0, 0), W, H, true);

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

        try {
            audioDevice = new MyAudioDevice();
            FileInputStream fis = new FileInputStream("./sound/PressStart.mp3");
            player = new AdvancedPlayer(fis, audioDevice);
            new Thread(() -> {
                try { player.play(); } catch (Exception e) {}
            }).start();
        } catch (Exception e) {
            System.out.println("Musique non trouvee");
        }

        fen.setVisible(true);
        fen.rafraichir();
    }

    public int NewGame(int game) {
        try { Thread.sleep(16); } catch (InterruptedException e) {}

        // Defilement decors
        fond1.translater(-5, 0); fond2.translater(-5, 0); fond3.translater(-5, 0);
        if (fond1.getA().getX() <= -W) fond1.translater(W*3, 0);
        if (fond2.getA().getX() <= -W) fond2.translater(W*3, 0);
        if (fond3.getA().getX() <= -W) fond3.translater(W*3, 0);

        sol1.translater(-10, 0); sol2.translater(-10, 0); sol3.translater(-10, 0);
        if (sol1.getA().getX() <= -W) sol1.translater(W*3, 0);
        if (sol2.getA().getX() <= -W) sol2.translater(W*3, 0);
        if (sol3.getA().getX() <= -W) sol3.translater(W*3, 0);

        plafond1.translater(-10, 0); plafond2.translater(-10, 0); plafond3.translater(-10, 0);
        if (plafond1.getA().getX() <= -W) plafond1.translater(W*3, 0);
        if (plafond2.getA().getX() <= -W) plafond2.translater(W*3, 0);
        if (plafond3.getA().getX() <= -W) plafond3.translater(W*3, 0);

        // 4. SPAWN D'OBSTACLES RYTHMÉ SUR L'AMPLITUDE AUDIO (ADAPTATIF)
        if (cooldown > 0) cooldown--;
        float amp = (audioDevice != null) ? audioDevice.currentAmplitude : 0;
        
        // Mise à jour de la moyenne du son pour s'adapter à la musique
        meanAmplitude = (meanAmplitude * 0.98f) + (amp * 0.02f);
        if (meanAmplitude < 3000) meanAmplitude = 3000; // Seuil minimum

        // On déclenche sur un pic (amplitude > 130% de la moyenne)
        if (amp > meanAmplitude * 1.3f && cooldown == 0) {
            // Un peu de hasard mais on évite les séries trop longues de pics
            boolean forcePlatform = (consecutiveSpikes >= 2);
            
            if (Math.random() > 0.4 && !forcePlatform) {
                // --- SPIKES ---
                consecutivePlatforms = 0;
                consecutiveSpikes++;

                int samples = (amp > meanAmplitude * 2.0f) ? 2 : 1; // Max 2 spikes pour la difficulté
                for (int s = 0; s < samples; s++) {
                    int startY = 100, vitY = 0, ampY = 0;
                    if (amp > meanAmplitude * 2.5f && Math.random() > 0.6) {
                        startY = 150 + (int)(Math.random() * 150);
                        vitY = 4; ampY = 80;
                    }
                    Texture spike = new Texture("./img/ennemis/spike.png", new Point(W + s * 120, startY), 100, 100);
                    obstacles.add(new Obstacle(spike, W + s * 120, startY, vitY, ampY, true, false));
                    fen.ajouter(spike);
                    
                    int mirrorY = H - startY - 100;
                    Texture spikeMir = new Texture("./img/ennemis/spike_flip.png", new Point(W + s * 120, mirrorY), 100, 100);
                    obstacles.add(new Obstacle(spikeMir, W + s * 120, mirrorY, -vitY, ampY, true, true));
                    fen.ajouter(spikeMir);
                }
                cooldown = 40; // Plus de temps pour respirer
            } else {
                // --- PLATEFORMES ---
                consecutivePlatforms++;
                consecutiveSpikes = 0;

                if (consecutivePlatforms == 1) lastPlatformY = 100;
                else {
                    lastPlatformY += 100;
                    if (lastPlatformY > 300) lastPlatformY = 100;
                }
                
                int vitY = 0, ampY = 0;
                if (amp > meanAmplitude * 2.5f) { vitY = 2; ampY = 100; lastPlatformY = 200; }
                
                Texture plat = new Texture("./img/Tiles/Tile_05.png", new Point(W, lastPlatformY), 110, 100);
                obstacles.add(new Obstacle(plat, W, lastPlatformY, vitY, ampY, false, false));
                fen.ajouter(plat);
                
                int mirrorY = H - lastPlatformY - 100;
                Texture platMir = new Texture("./img/Tiles/Tile_05_flip.png", new Point(W, mirrorY), 110, 100);
                obstacles.add(new Obstacle(platMir, W, mirrorY, -vitY, ampY, false, true));
                fen.ajouter(platMir);
                cooldown = 25;
            }
        }

        if (amp > 18000 && flashTime <= 0) { flashTime = 2; fen.ajouter(flashScreen); }
        if (flashTime > 0) { flashTime--; if (flashTime == 0) fen.supprimer(flashScreen); }

        // Collisions
        int pX = joueur.getTex().getA().getX() + 10;
        int pY = joueur.getTex().getA().getY() + 10;
        int pW = 80, pH = 80;

        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Obstacle obs = obstacles.get(i);
            obs.update();
            if (obs.x < -200) { fen.supprimer(obs.dessin); obstacles.remove(i); }
            else if (obs.collidesWithPlayer(pX, pY, pW, pH)) {
                if (obs.isSpike) { return 3; }
                else {
                    int obsTop = obs.y + 100;
                    if (joueur.getVelocity() < 0 && (pY + 25) >= obsTop) {
                        joueur.getTex().translater(0, obsTop - pY);
                        joueur.setVelocity(0); joueur.setJumping(false);
                    } else if (pY < obsTop - 30) { return 3; }
                }
            }
        }
        
        joueur.bougerJoueur(clavier, fen);
        joueur.updatePhysics(100);
        fen.rafraichir();
        return 0; // On reste a 0 tant qu'on n'est pas mort (3)
    }

    public void effacerJeu() {
        fen.supprimer(fond1); fen.supprimer(fond2); fen.supprimer(fond3);
        fen.supprimer(sol1); fen.supprimer(sol2); fen.supprimer(sol3);
        fen.supprimer(plafond1); fen.supprimer(plafond2); fen.supprimer(plafond3);
        fen.supprimer(joueur.getTex());
        if (flashTime > 0) fen.supprimer(flashScreen);
        fen.removeKeyListener(clavier);
        fen.getP().removeKeyListener(clavier);
        for (Obstacle obs : obstacles) fen.supprimer(obs.dessin);
        obstacles.clear();
        if (player != null) player.close();
    }
}