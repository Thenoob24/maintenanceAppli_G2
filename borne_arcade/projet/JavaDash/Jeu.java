import MG2D.*;
import MG2D.Fenetre;
import MG2D.geometrie.*;
import MG2D.audio.player.JavaSoundAudioDevice;
import MG2D.audio.player.advanced.AdvancedPlayer;
import MG2D.audio.decoder.JavaLayerException;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Classe principale gérant la logique du jeu JavaDash.
 * Elle orchestre les décors défilants, les obstacles, la physique du joueur
 * et les effets synchronisés avec la musique.
 */
class Jeu {
    private Fenetre fen;
    private int W;
    private int H;

    private Texture fond1, fond2, fond3;
    private Texture sol1, sol2, sol3;
    private Texture plafond1, plafond2, plafond3;
    private MyAudioDevice audioDevice;
    private AdvancedPlayer player;
    private ClavierBorneArcade clavier;
    private Player joueur;

    // --- BARRE DE PROGRESSION ---
    private Rectangle barrefond;
    private Rectangle barreProgression;
    private long totalFrames = 0;
    private static final int BAR_H = 8;

    // --- ÉCRAN DE VICTOIRE ---
    private Rectangle fondVictoire;
    private Rectangle fondVictoireOverlay;
    private Texte texteVictoire;
    private Texte texteSousVictoire;
    private boolean victoireAffichee = false;
    private int victoireTimer = 0;
    private static final int VICTOIRE_DUREE = 180;
    private Texte texteAmplitude;

    // --- ÉCRAN DE DÉFAITE ---
    private Texte texteDefaite;
    private Texte texteSousDefaite;
    private boolean defaiteAffichee = false;
    private int defaiteTimer = 0;
    private static final int DEFAITE_DUREE = 180;

    /**
     * Classe interne pour capturer l'amplitude sonore en temps réel.
     * Étend JavaSoundAudioDevice pour analyser les échantillons audio joués.
     */
    class MyAudioDevice extends JavaSoundAudioDevice {
        public volatile float currentAmplitude = 0;
        public volatile long framesPlayed = 0;

        @Override
        protected void writeImpl(short[] samples, int offs, int len) throws JavaLayerException {
            long sum = 0;
            for (int i = 0; i < len; i++) {
                sum += (long) samples[offs + i] * (long) samples[offs + i];
            }
            currentAmplitude = (len > 0) ? (float) Math.sqrt(sum / len) : 0;
            framesPlayed += len;
            super.writeImpl(samples, offs, len);
        }
    }

    private ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    private int cooldown = 0;
    private int lastPlatformY = 100;
    private int consecutivePlatforms = 0;
    private int consecutiveSpikes = 0;
    private float meanAmplitude = 2000;

    // --- DÉTECTION DE BEAT ---
    private float[] ampHistory = new float[8];
    private int ampHistIdx = 0;
    private float localMean = 2000;
    private boolean wasOnBeat = false;
    private static final float BEAT_THRESHOLD = 1.6f;

    // --- EFFETS RYTHMÉS ---
    private int shakeTimer = 0;
    private int shakeOffsetX = 0;
    private int shakeOffsetY = 0;
    private Rectangle glowBot;
    private Rectangle glowTop;
    private static final int GLOW_H = 6;
    private float smoothAmplitude = 0;
    private float currentBarH = BAR_H;

    // --- PLATEFORMES SCALÉES PAR AMPLITUDE ---
    private static final int PLAT_W_MIN = 80;
    private static final int PLAT_W_MAX = 200;
    private static final int PLAT_H_BASE = 30;
    private static final int PLAT_H_MAX = 60;

    // --- INVERSION DE GRAVITÉ ---
    private boolean gravityInverted = false;
    private int gravityCooldown = 0;
    private static final int GRAVITY_COOLDOWN_DUREE = 180;
    private String currentBgPath = "./img/background/Day/Background.png";
    private String currentSolPath = "./img/Tiles/Tile_02.png";
    private String currentSolFlipPath = "./img/Tiles/Tile_02_flip.png";

    // --- SEUILS D'AMPLITUDE ---
    private static final float THRESHOLD_POPUP = 1.5f;
    private static final float THRESHOLD_SHAKE = 2.0f;
    private static final float THRESHOLD_GRAVITY = 5.0f;
    private static final float THRESHOLD_DOUBLE_SPIKE = 2.0f;
    private static final float THRESHOLD_MOVING_SPIKE = 10f;
    private static final float THRESHOLD_MOVING_PLATFORM = 10f;
    private static final float THRESHOLD_VISUAL_INTENSITY = 1.5f;

    // -------------------------------------------------------
    // Calcule la durée totale du MP3 en samples PCM
    // -------------------------------------------------------
    /**
     * Calcule la durée totale du fichier MP3 en nombre d'échantillons (frames).
     * @param path Chemin vers le fichier MP3.
     * @return Le nombre total de frames audio.
     */
    private long getMp3TotalFrames(String path) {
        try {
            FileInputStream fis = new FileInputStream(path);
            MG2D.audio.decoder.Bitstream bitstream = new MG2D.audio.decoder.Bitstream(fis);
            long frames = 0;
            while (true) {
                MG2D.audio.decoder.Header header = bitstream.readFrame();
                if (header == null)
                    break;
                frames += (long) (header.ms_per_frame() * 44.1f) * 2;
                bitstream.closeFrame();
            }
            bitstream.close();
            return frames;
        } catch (Exception e) {
            System.out.println("Impossible de lire la durée MP3 : " + e.getMessage());
            return 0;
        }
    }

    // -------------------------------------------------------
    // Dégradé vert → jaune → rouge selon ratio [0.0 - 1.0]
    // -------------------------------------------------------
    private MG2D.geometrie.Couleur couleurDegrade(float ratio) {
        int r, g, b;
        if (ratio < 0.5f) {
            float t = ratio * 2f;
            r = (int) (t * 255);
            g = 200;
            b = (int) (50 - t * 50);
        } else {
            float t = (ratio - 0.5f) * 2f;
            r = (int) (255 - t * 35);
            g = (int) (200 - t * 170);
            b = 0;
        }
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        return new MG2D.geometrie.Couleur(r, g, b);
    }

    // -------------------------------------------------------
    // Détecte si l'amplitude courante représente un "beat"
    // -------------------------------------------------------
    /**
     * Détecte si l'amplitude actuelle correspond à un "beat" musical.
     * @param amp L'amplitude sonore actuelle.
     * @return Vrai si un beat est détecté (front montant).
     */
    private boolean detectBeat(float amp) {
        ampHistory[ampHistIdx % ampHistory.length] = amp;
        ampHistIdx++;

        float sum = 0;
        for (float v : ampHistory)
            sum += v;
        localMean = sum / ampHistory.length;
        if (localMean < 3000)
            localMean = 3000;

        boolean onBeat = (amp > localMean * BEAT_THRESHOLD);
        boolean isBeatEdge = onBeat && !wasOnBeat;
        wasOnBeat = onBeat;
        return isBeatEdge;
    }

    // -------------------------------------------------------
    // CRÉATION DU JEU
    // -------------------------------------------------------
    /**
     * Initialise tous les éléments du jeu.
     * @param f La fenêtre MG2D à utiliser.
     */
    public void CreationJeu(Fenetre f) {
        fen = f;
        W = fen.getWidth();
        H = fen.getHeight();

        fond1 = new Texture("./img/background/Day/Background.png", new Point(0, 0), W, H);
        fond2 = new Texture("./img/background/Day/Background.png", new Point(W, 0), W, H);
        fond3 = new Texture("./img/background/Day/Background.png", new Point(W * 2, 0), W, H);

        sol1 = new Texture("./img/Tiles/Tile_02.png", new Point(0, 0), W, 100);
        sol2 = new Texture("./img/Tiles/Tile_02.png", new Point(W, 0), W, 100);
        sol3 = new Texture("./img/Tiles/Tile_02.png", new Point(W * 2, 0), W, 100);

        plafond1 = new Texture("./img/Tiles/Tile_02_flip.png", new Point(0, H - 100), W, 100);
        plafond2 = new Texture("./img/Tiles/Tile_02_flip.png", new Point(W, H - 100), W, 100);
        plafond3 = new Texture("./img/Tiles/Tile_02_flip.png", new Point(W * 2, H - 100), W, 100);

        glowBot = new Rectangle(new MG2D.geometrie.Couleur(0, 0, 0), new Point(0, 100), W, GLOW_H, true);
        glowTop = new Rectangle(new MG2D.geometrie.Couleur(0, 0, 0), new Point(0, H - 100 - GLOW_H), W, GLOW_H, true);

        barrefond = new Rectangle(new MG2D.geometrie.Couleur(40, 40, 40), new Point(0, H - BAR_H), W, BAR_H, true);
        barreProgression = new Rectangle(couleurDegrade(0f), new Point(0, H - BAR_H), 1, BAR_H, true);

        fen.ajouter(fond1);
        fen.ajouter(fond2);
        fen.ajouter(fond3);
        fen.ajouter(glowBot);
        fen.ajouter(glowTop);
        fen.ajouter(sol1);
        fen.ajouter(sol2);
        fen.ajouter(sol3);
        fen.ajouter(plafond1);
        fen.ajouter(plafond2);
        fen.ajouter(plafond3);
        fen.ajouter(barrefond);
        fen.ajouter(barreProgression);

        java.awt.Font fontAmp = new java.awt.Font("Arial", java.awt.Font.BOLD, 18);
        texteAmplitude = new Texte(new MG2D.geometrie.Couleur(255, 255, 255), "Amplitude : 0", fontAmp,
                new Point(W - 200, H - 50));
        fen.ajouter(texteAmplitude);

        clavier = new ClavierBorneArcade();
        fen.addKeyListener(clavier);
        fen.getP().addKeyListener(clavier);

        joueur = new Player();
        fen.ajouter(joueur.getTex());

        try {
            totalFrames = getMp3TotalFrames("./sound/PressStart.mp3");
            System.out.println("Durée totale estimée (samples) : " + totalFrames);

            audioDevice = new MyAudioDevice();
            FileInputStream fis = new FileInputStream("./sound/PressStart.mp3");
            player = new AdvancedPlayer(fis, audioDevice);
            new Thread(() -> {
                try {
                    player.play();
                } catch (Exception e) {
                }
            }).start();
        } catch (Exception e) {
            System.out.println("Musique non trouvée");
        }

        fen.setVisible(true);
        fen.rafraichir();
    }

    // -------------------------------------------------------
    // ÉCRAN DE VICTOIRE
    // -------------------------------------------------------
    private void afficherVictoire() {
        fondVictoire = new Rectangle(new MG2D.geometrie.Couleur(200, 160, 0), new Point(0, 0), W, H, true);
        fondVictoireOverlay = new Rectangle(new MG2D.geometrie.Couleur(0, 0, 0), new Point(W / 2 - 320, H / 2 - 100),
                640, 200, true);

        java.awt.Font fontTitre = new java.awt.Font("Arial", java.awt.Font.BOLD, 72);
        java.awt.Font fontSous = new java.awt.Font("Arial", java.awt.Font.PLAIN, 22);

        texteVictoire = new Texte(new MG2D.geometrie.Couleur(255, 220, 0), "YOU WIN !", fontTitre,
                new Point(W / 2 - 160, H / 2 + 20));
        texteSousVictoire = new Texte(new MG2D.geometrie.Couleur(255, 255, 255),
                "Félicitations, tu as survécu jusqu'au bout !", fontSous, new Point(W / 2 - 290, H / 2 - 55));

        fen.ajouter(fondVictoire);
        fen.ajouter(fondVictoireOverlay);
        fen.ajouter(texteVictoire);
        fen.ajouter(texteSousVictoire);
        fen.rafraichir();
    }

    // -------------------------------------------------------
    // ÉCRAN DE DÉFAITE
    // -------------------------------------------------------
    /**
     * Affiche l'écran de défaite avec le message "Vous avez perdu !".
     */
    private void afficherDefaite() {
        java.awt.Font fontTitre = new java.awt.Font("Arial", java.awt.Font.BOLD, 72);
        java.awt.Font fontSous  = new java.awt.Font("Arial", java.awt.Font.PLAIN, 22);

        texteDefaite = new Texte(new MG2D.geometrie.Couleur(255, 60, 60), "Vous avez perdu !", fontTitre,
                new Point(W / 2 - 240, H / 2 + 20));
        texteSousDefaite = new Texte(new MG2D.geometrie.Couleur(255, 255, 255),
                "Retentez votre chance...", fontSous,
                new Point(W / 2 - 150, H / 2 - 55));

        fen.ajouter(texteDefaite);
        fen.ajouter(texteSousDefaite);
        fen.rafraichir();
    }

    // -------------------------------------------------------
    // BOUCLE DE JEU
    // Codes de retour : 0 = en cours | 2 = victoire | 3 = défaite
    // -------------------------------------------------------
    /**
     * Boucle de mise à jour principale appelée à chaque frame.
     * @param game État actuel du jeu.
     * @return Nouvel état du jeu (0: en cours, 2: victoire, 3: défaite).
     */
    public int NewGame(int game) {
        try {
            Thread.sleep(16);
        } catch (InterruptedException e) {
        }

        // --- Victoire en attente ---
        if (victoireAffichee) {
            victoireTimer++;
            fen.rafraichir();
            if (victoireTimer >= VICTOIRE_DUREE)
                return 2;
            return 0;
        }

        // --- Défaite en attente ---
        if (defaiteAffichee) {
            defaiteTimer++;
            fen.rafraichir();
            if (defaiteTimer >= DEFAITE_DUREE)
                return 3;
            return 0;
        }

        // --- Défilement décors ---
        fond1.translater(-5, 0);
        fond2.translater(-5, 0);
        fond3.translater(-5, 0);
        if (fond1.getA().getX() <= -W)
            fond1.translater(W * 3, 0);
        if (fond2.getA().getX() <= -W)
            fond2.translater(W * 3, 0);
        if (fond3.getA().getX() <= -W)
            fond3.translater(W * 3, 0);

        sol1.translater(-10, 0);
        sol2.translater(-10, 0);
        sol3.translater(-10, 0);
        if (sol1.getA().getX() <= -W)
            sol1.translater(W * 3, 0);
        if (sol2.getA().getX() <= -W)
            sol2.translater(W * 3, 0);
        if (sol3.getA().getX() <= -W)
            sol3.translater(W * 3, 0);

        plafond1.translater(-10, 0);
        plafond2.translater(-10, 0);
        plafond3.translater(-10, 0);
        if (plafond1.getA().getX() <= -W)
            plafond1.translater(W * 3, 0);
        if (plafond2.getA().getX() <= -W)
            plafond2.translater(W * 3, 0);
        if (plafond3.getA().getX() <= -W)
            plafond3.translater(W * 3, 0);

        // --- BARRE DE PROGRESSION ---
        if (audioDevice != null && totalFrames > 0) {
            float ratio = Math.min(1f, (float) audioDevice.framesPlayed / (float) totalFrames);
            int barWidth = Math.max(1, (int) (ratio * W));
            int drawBarH = Math.max(BAR_H, (int) currentBarH);

            fen.supprimer(barrefond);
            barrefond = new Rectangle(new MG2D.geometrie.Couleur(40, 40, 40), new Point(0, H - drawBarH), W, drawBarH,
                    true);
            fen.ajouter(barrefond);

            fen.supprimer(barreProgression);
            barreProgression = new Rectangle(couleurDegrade(ratio), new Point(0, H - drawBarH), barWidth, drawBarH,
                    true);
            fen.ajouter(barreProgression);

            if (ratio >= 1.0f && !victoireAffichee) {
                victoireAffichee = true;
                victoireTimer = 0;
                afficherVictoire();
                return 0;
            }
        }

        // --- MAJ AFFICHAGE AMPLITUDE ---
        if (audioDevice != null) {
            fen.supprimer(texteAmplitude);
            java.awt.Font fontAmp = new java.awt.Font("Arial", java.awt.Font.BOLD, 18);
            texteAmplitude = new Texte(new MG2D.geometrie.Couleur(255, 255, 255),
                    "Amplitude : " + (int) audioDevice.currentAmplitude, fontAmp, new Point(W - 200, H - 50));
            fen.ajouter(texteAmplitude);
        }

        // --- AMPLITUDE COURANTE ---
        float amp = (audioDevice != null) ? audioDevice.currentAmplitude : 0;

        meanAmplitude = (meanAmplitude * 0.98f) + (amp * 0.02f);
        if (meanAmplitude < 3000)
            meanAmplitude = 3000;

        smoothAmplitude = smoothAmplitude * 0.85f + amp * 0.15f;
        float intensity = Math.min(1f, smoothAmplitude / (meanAmplitude * THRESHOLD_VISUAL_INTENSITY));

        // --- SPAWN D'OBSTACLES SUR BEAT DÉTECTÉ ---
        if (cooldown > 0)
            cooldown--;

        boolean beat = detectBeat(amp);

        if (beat && cooldown == 0) {
            double rand = Math.random();

            if (rand < 0.2) {
                // --- SPAWN PIÈGE 4 SPIKES + ORBE ---
                int gap = 100;
                for (int s = 0; s < 4; s++) {
                    Texture sp = new Texture("./img/ennemis/spike.png", new Point(W + s * gap, 100), 100, 100);
                    obstacles.add(new Obstacle(sp, W + s * gap, 100, 0, 0, true, false));
                    fen.ajouter(sp);
                }
                Cercle orbC = new Cercle(Couleur.JAUNE, new Point(W + 150, 250), 40, true);
                Obstacle orbObs = new Obstacle(orbC, W + 150, 250, true);
                obstacles.add(orbObs);
                fen.ajouter(orbC);
                if (orbObs.onde != null) fen.ajouter(orbObs.onde);
                cooldown = 60;
            } else if (rand > 0.6 && consecutiveSpikes < 2) {
                // --- SPIKES ---
                consecutivePlatforms = 0;
                consecutiveSpikes++;
                int samples = (amp > meanAmplitude * THRESHOLD_DOUBLE_SPIKE) ? 2 : 1;
                for (int s = 0; s < samples; s++) {
                    int startY = 100, vitY = 0, ampY = 0;
                    if (amp > meanAmplitude * THRESHOLD_MOVING_SPIKE && Math.random() > 0.6) {
                        startY = 150 + (int) (Math.random() * 150);
                        vitY = 1;
                        ampY = 80;
                    }
                    Texture spike = new Texture("./img/ennemis/spike.png", new Point(W + s * 130, startY), 100, 100);
                    obstacles.add(new Obstacle(spike, W + s * 130, startY, vitY, ampY, true, false));
                    fen.ajouter(spike);

                    int mirrorY = H - startY - 100;
                    Texture spikeMir = new Texture("./img/ennemis/spike_flip.png", new Point(W + s * 130, mirrorY), 100,
                            100);
                    obstacles.add(new Obstacle(spikeMir, W + s * 130, mirrorY, -vitY, ampY, true, true));
                    fen.ajouter(spikeMir);
                }
                cooldown = 35;
            } else {
                // --- PLATEFORMES ---
                int platW = 100, platH = 100;
                consecutivePlatforms++;
                consecutiveSpikes = 0;
                if (consecutivePlatforms == 1) {
                    lastPlatformY = 100;
                } else {
                    lastPlatformY += 80 + (int) (intensity * 60);
                    if (lastPlatformY > 300)
                        lastPlatformY = 100;
                }
                int vitY = 0, ampY = 0;
                if (amp > meanAmplitude * THRESHOLD_MOVING_PLATFORM) {
                    vitY = 2;
                    ampY = 100;
                    lastPlatformY = 200;
                }
                Texture plat = new Texture("./img/Tiles/Tile_11.png", new Point(W, lastPlatformY), platW, platH);
                obstacles.add(new Obstacle(plat, W, lastPlatformY, vitY, ampY, false, false));
                fen.ajouter(plat);
                int mirrorY = H - lastPlatformY - platH;
                Texture platMir = new Texture("./img/Tiles/Tile_11_flip.png", new Point(W, mirrorY), platW, platH);
                obstacles.add(new Obstacle(platMir, W, mirrorY, -vitY, ampY, false, true));
                fen.ajouter(platMir);
                cooldown = 22;
            }
        }

        // --- OBSTACLES POP-UP SUR FORTE AMPLITUDE ---
        if (amp > meanAmplitude * THRESHOLD_POPUP && cooldown == 0) {
            int spawnX = (int) (W * 0.75);
            Texture spikePop = new Texture("./img/ennemis/spike.png", new Point(spawnX, -100), 100, 100);
            obstacles.add(new Obstacle(spikePop, spawnX, -100, 12, 100, true, false, true));
            fen.ajouter(spikePop);
            cooldown = 25;
        }

        // --- EFFETS VISUELS RYTHMÉS ---
        if (audioDevice != null) {

            // 1) SCREEN SHAKE
            if (amp > meanAmplitude * THRESHOLD_SHAKE && shakeTimer <= 0) {
                shakeTimer = 4;
            }
            if (shakeTimer > 0) {
                shakeTimer--;
                int newShakeX = (int) (Math.random() * 10 - 5);
                int newShakeY = (int) (Math.random() * 6 - 3);
                int dx = newShakeX - shakeOffsetX;
                int dy = newShakeY - shakeOffsetY;
                fond1.translater(dx, dy);
                fond2.translater(dx, dy);
                fond3.translater(dx, dy);
                sol1.translater(dx, dy);
                sol2.translater(dx, dy);
                sol3.translater(dx, dy);
                plafond1.translater(dx, dy);
                plafond2.translater(dx, dy);
                plafond3.translater(dx, dy);
                shakeOffsetX = newShakeX;
                shakeOffsetY = newShakeY;
            } else if (shakeOffsetX != 0 || shakeOffsetY != 0) {
                fond1.translater(-shakeOffsetX, -shakeOffsetY);
                fond2.translater(-shakeOffsetX, -shakeOffsetY);
                fond3.translater(-shakeOffsetX, -shakeOffsetY);
                sol1.translater(-shakeOffsetX, -shakeOffsetY);
                sol2.translater(-shakeOffsetX, -shakeOffsetY);
                sol3.translater(-shakeOffsetX, -shakeOffsetY);
                plafond1.translater(-shakeOffsetX, -shakeOffsetY);
                plafond2.translater(-shakeOffsetX, -shakeOffsetY);
                plafond3.translater(-shakeOffsetX, -shakeOffsetY);
                shakeOffsetX = 0;
                shakeOffsetY = 0;
            }

            // 2) BANDES NÉON PULSANTES
            int oR = (int) (80 + intensity * 175);
            int oG = (int) (20 + (1f - intensity) * 30);
            int oB = (int) (180 * (1f - intensity));
            oR = Math.max(0, Math.min(255, oR));
            oG = Math.max(0, Math.min(255, oG));
            oB = Math.max(0, Math.min(255, oB));
            MG2D.geometrie.Couleur glowColor = new MG2D.geometrie.Couleur(oR, oG, oB);
            fen.supprimer(glowBot);
            fen.supprimer(glowTop);
            int glowH = GLOW_H + (int) (intensity * 10);
            glowBot = new Rectangle(glowColor, new Point(0, 100), W, glowH, true);
            glowTop = new Rectangle(glowColor, new Point(0, H - 100 - glowH), W, glowH, true);
            fen.ajouter(glowBot);
            fen.ajouter(glowTop);

            // 3) BARRE DE PROGRESSION QUI PULSE EN HAUTEUR
            float targetBarH = BAR_H + intensity * 14;
            currentBarH = currentBarH * 0.8f + targetBarH * 0.2f;

            // 4) INVERSION DE GRAVITÉ sur beat très fort
            if (gravityCooldown > 0)
                gravityCooldown--;
            if (amp > meanAmplitude * THRESHOLD_GRAVITY && gravityCooldown == 0) {
                gravityInverted = !gravityInverted;
                gravityCooldown = GRAVITY_COOLDOWN_DUREE;

                String newBg = gravityInverted ? "./img/background/Night/1.png" : "./img/background/Day/Background.png";
                currentBgPath = newBg;
                fen.supprimer(fond1);
                fen.supprimer(fond2);
                fen.supprimer(fond3);
                int f1x = fond1.getA().getX(), f2x = fond2.getA().getX(), f3x = fond3.getA().getX();
                fond1 = new Texture(newBg, new Point(f1x, 0), W, H);
                fond2 = new Texture(newBg, new Point(f2x, 0), W, H);
                fond3 = new Texture(newBg, new Point(f3x, 0), W, H);
                fen.ajouter(fond1);
                fen.ajouter(fond2);
                fen.ajouter(fond3);

                String newSol = gravityInverted ? "./img/Tiles/Tile_14.png" : "./img/Tiles/Tile_02.png";
                String newSolFlip = gravityInverted ? "./img/Tiles/Tile_14.png" : "./img/Tiles/Tile_02_flip.png";
                currentSolPath = newSol;
                currentSolFlipPath = newSolFlip;
                fen.supprimer(sol1);
                fen.supprimer(sol2);
                fen.supprimer(sol3);
                int s1x = sol1.getA().getX(), s2x = sol2.getA().getX(), s3x = sol3.getA().getX();
                sol1 = new Texture(newSol, new Point(s1x, 0), W, 100);
                sol2 = new Texture(newSol, new Point(s2x, 0), W, 100);
                sol3 = new Texture(newSol, new Point(s3x, 0), W, 100);
                fen.ajouter(sol1);
                fen.ajouter(sol2);
                fen.ajouter(sol3);

                fen.supprimer(plafond1);
                fen.supprimer(plafond2);
                fen.supprimer(plafond3);
                int p1x = plafond1.getA().getX(), p2x = plafond2.getA().getX(), p3x = plafond3.getA().getX();
                plafond1 = new Texture(newSolFlip, new Point(p1x, H - 100), W, 100);
                plafond2 = new Texture(newSolFlip, new Point(p2x, H - 100), W, 100);
                plafond3 = new Texture(newSolFlip, new Point(p3x, H - 100), W, 100);
                fen.ajouter(plafond1);
                fen.ajouter(plafond2);
                fen.ajouter(plafond3);
            }
        }

        // --- COLLISIONS ---
        int pX = joueur.getTex().getA().getX() + 10;
        int pY = joueur.getTex().getA().getY() + 10;
        int pW = 80, pH = 80;

        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Obstacle obs = obstacles.get(i);
            obs.update(intensity);
            if (obs.x < -200) {
                fen.supprimer(obs.dessin);
                if (obs.onde != null) fen.supprimer(obs.onde);
                obstacles.remove(i);
            } else if (obs.collidesWithPlayer(pX, pY, pW, pH)) {
                if (obs.isOrb) {
                    if (!obs.orbUsed && clavier.getJoyJ1HautEnfoncee()) {
                        joueur.jumpOrb(gravityInverted);
                        obs.orbUsed = true;
                    }
                } else if (obs.isSpike) {
                    // --- DÉFAITE ---
                    if (!defaiteAffichee) {
                        defaiteAffichee = true;
                        defaiteTimer = 0;
                        afficherDefaite();
                    }
                    return 0;
                } else {
                    int obsTop = obs.y + obs.dessin.getBoiteEnglobante().getHauteur();
                    if (joueur.getVelocity() < 0 && (pY + 25) >= obsTop) {
                        joueur.getTex().translater(0, obsTop - pY);
                        joueur.setVelocity(0);
                        joueur.setJumping(false);
                    } else if (pY < obsTop - 30) {
                        // --- DÉFAITE ---
                        if (!defaiteAffichee) {
                            defaiteAffichee = true;
                            defaiteTimer = 0;
                            afficherDefaite();
                        }
                        return 0;
                    }
                }
            }
        }

        joueur.bougerJoueur(clavier, fen, gravityInverted);
        joueur.updatePhysics(100, H - 200, gravityInverted);
        fen.rafraichir();
        return 0;
    }

    // -------------------------------------------------------
    // NETTOYAGE
    // -------------------------------------------------------
    /**
     * Supprime tous les éléments graphiques et ferme les ressources audio.
     */
    public void effacerJeu() {
        fen.supprimer(fond1);
        fen.supprimer(fond2);
        fen.supprimer(fond3);
        fen.supprimer(sol1);
        fen.supprimer(sol2);
        fen.supprimer(sol3);
        fen.supprimer(plafond1);
        fen.supprimer(plafond2);
        fen.supprimer(plafond3);
        fen.supprimer(joueur.getTex());
        fen.supprimer(barrefond);
        fen.supprimer(barreProgression);
        fen.supprimer(glowBot);
        fen.supprimer(glowTop);

        if (shakeOffsetX != 0 || shakeOffsetY != 0) {
            fond1.translater(-shakeOffsetX, -shakeOffsetY);
            fond2.translater(-shakeOffsetX, -shakeOffsetY);
            fond3.translater(-shakeOffsetX, -shakeOffsetY);
            sol1.translater(-shakeOffsetX, -shakeOffsetY);
            sol2.translater(-shakeOffsetX, -shakeOffsetY);
            sol3.translater(-shakeOffsetX, -shakeOffsetY);
            plafond1.translater(-shakeOffsetX, -shakeOffsetY);
            plafond2.translater(-shakeOffsetX, -shakeOffsetY);
            plafond3.translater(-shakeOffsetX, -shakeOffsetY);
        }

        if (victoireAffichee) {
            fen.supprimer(fondVictoire);
            fen.supprimer(fondVictoireOverlay);
            fen.supprimer(texteVictoire);
            fen.supprimer(texteSousVictoire);
        }

        if (defaiteAffichee) {
            fen.supprimer(texteDefaite);
            fen.supprimer(texteSousDefaite);
        }

        fen.removeKeyListener(clavier);
        fen.getP().removeKeyListener(clavier);
        for (Obstacle obs : obstacles) {
            fen.supprimer(obs.dessin);
            if (obs.onde != null) fen.supprimer(obs.onde);
        }
        obstacles.clear();
        if (player != null)
            player.close();
    }
}