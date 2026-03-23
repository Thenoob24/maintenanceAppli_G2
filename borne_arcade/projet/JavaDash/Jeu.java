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
    private int victoireTimer = 0; // délai avant de retourner le code 2
    private static final int VICTOIRE_DUREE = 180; // ~3 secondes à 60fps

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
    private float meanAmplitude = 5000;

    private Rectangle flashScreen;
    private int flashTime = 0;

    // --- EFFETS RYTHMÉS ---
    private int shakeTimer = 0;
    private int shakeOffsetX = 0;
    private int shakeOffsetY = 0;
    private Rectangle glowBot;
    private Rectangle glowTop;
    private static final int GLOW_H = 6;
    private float smoothAmplitude = 0;
    private float currentBarH = BAR_H;

    // -------------------------------------------------------
    // Calcule la durée totale du MP3 en samples PCM
    // -------------------------------------------------------
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
    // CRÉATION DU JEU
    // -------------------------------------------------------
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

        flashScreen = new Rectangle(MG2D.geometrie.Couleur.BLANC, new Point(0, 0), W, H, true);

        // Bandes néon pulsantes (sol et plafond)
        glowBot = new Rectangle(
                new MG2D.geometrie.Couleur(0, 0, 0),
                new Point(0, 100), W, GLOW_H, true);
        glowTop = new Rectangle(
                new MG2D.geometrie.Couleur(0, 0, 0),
                new Point(0, H - 100 - GLOW_H), W, GLOW_H, true);

        // Barre de progression (fond + barre colorée)
        barrefond = new Rectangle(
                new MG2D.geometrie.Couleur(40, 40, 40),
                new Point(0, H - BAR_H), W, BAR_H, true);
        barreProgression = new Rectangle(
                couleurDegrade(0f),
                new Point(0, H - BAR_H), 1, BAR_H, true);

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

    private void afficherVictoire() {
        fondVictoire = new Rectangle(
                new MG2D.geometrie.Couleur(200, 160, 0),
                new Point(0, 0), W, H, true);
        fondVictoireOverlay = new Rectangle(
                new MG2D.geometrie.Couleur(0, 0, 0),
                new Point(W / 2 - 320, H / 2 - 100), 640, 200, true);

        java.awt.Font fontTitre = new java.awt.Font("Arial", java.awt.Font.BOLD, 72);
        java.awt.Font fontSous = new java.awt.Font("Arial", java.awt.Font.PLAIN, 22);

        texteVictoire = new Texte(
                new MG2D.geometrie.Couleur(255, 220, 0),
                "YOU WIN !",
                fontTitre,
                new Point(W / 2 - 160, H / 2 + 20));
        texteSousVictoire = new Texte(
                new MG2D.geometrie.Couleur(255, 255, 255),
                "Félicitations, tu as survécu jusqu'au bout !",
                fontSous,
                new Point(W / 2 - 290, H / 2 - 55));

        fen.ajouter(fondVictoire);
        fen.ajouter(fondVictoireOverlay);
        fen.ajouter(texteVictoire);
        fen.ajouter(texteSousVictoire);
        fen.rafraichir();
    }

    // -------------------------------------------------------
    // BOUCLE DE JEU
    // Codes de retour : 0 = en cours | 2 = victoire | 3 = défaite
    // -------------------------------------------------------
    public int NewGame(int game) {
        try {
            Thread.sleep(16);
        } catch (InterruptedException e) {
        }

        // --- Si l'écran de victoire est affiché, on attend puis on renvoie 2 ---
        if (victoireAffichee) {
            victoireTimer++;
            fen.rafraichir();
            if (victoireTimer >= VICTOIRE_DUREE)
                return 2;
            return 0;
        }

        // Défilement décors
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
            barrefond = new Rectangle(
                    new MG2D.geometrie.Couleur(40, 40, 40),
                    new Point(0, H - drawBarH), W, drawBarH, true);
            fen.ajouter(barrefond);

            fen.supprimer(barreProgression);
            barreProgression = new Rectangle(
                    couleurDegrade(ratio),
                    new Point(0, H - drawBarH), barWidth, drawBarH, true);
            fen.ajouter(barreProgression);

            // FIN DE MUSIQUE → déclenche la victoire
            if (ratio >= 1.0f && !victoireAffichee) {
                victoireAffichee = true;
                victoireTimer = 0;
                afficherVictoire();
                return 0; // on laisse le timer s'écouler
            }
        }

        // --- SPAWN D'OBSTACLES RYTHMÉ SUR L'AMPLITUDE AUDIO ---
        if (cooldown > 0)
            cooldown--;
        float amp = (audioDevice != null) ? audioDevice.currentAmplitude : 0;

        meanAmplitude = (meanAmplitude * 0.98f) + (amp * 0.02f);
        if (meanAmplitude < 3000)
            meanAmplitude = 3000;

        if (amp > meanAmplitude * 1.3f && cooldown == 0) {
            boolean forcePlatform = (consecutiveSpikes >= 2);

            if (Math.random() > 0.4 && !forcePlatform) {
                // SPIKES
                consecutivePlatforms = 0;
                consecutiveSpikes++;
                int samples = (amp > meanAmplitude * 2.0f) ? 2 : 1;
                for (int s = 0; s < samples; s++) {
                    int startY = 100, vitY = 0, ampY = 0;
                    if (amp > meanAmplitude * 2.5f && Math.random() > 0.6) {
                        startY = 150 + (int) (Math.random() * 150);
                        vitY = 4;
                        ampY = 80;
                    }
                    Texture spike = new Texture("./img/ennemis/spike.png",
                            new Point(W + s * 120, startY), 100, 100);
                    obstacles.add(new Obstacle(spike, W + s * 120, startY, vitY, ampY, true, false));
                    fen.ajouter(spike);

                    int mirrorY = H - startY - 100;
                    Texture spikeMir = new Texture("./img/ennemis/spike_flip.png",
                            new Point(W + s * 120, mirrorY), 100, 100);
                    obstacles.add(new Obstacle(spikeMir, W + s * 120, mirrorY, -vitY, ampY, true, true));
                    fen.ajouter(spikeMir);
                }
                cooldown = 40;
            } else {
                // PLATEFORMES
                consecutivePlatforms++;
                consecutiveSpikes = 0;
                if (consecutivePlatforms == 1)
                    lastPlatformY = 100;
                else {
                    lastPlatformY += 100;
                    if (lastPlatformY > 300)
                        lastPlatformY = 100;
                }
                int vitY = 0, ampY = 0;
                if (amp > meanAmplitude * 2.5f) {
                    vitY = 2;
                    ampY = 100;
                    lastPlatformY = 200;
                }

                Texture plat = new Texture("./img/Tiles/Tile_05.png",
                        new Point(W, lastPlatformY), 110, 100);
                obstacles.add(new Obstacle(plat, W, lastPlatformY, vitY, ampY, false, false));
                fen.ajouter(plat);

                int mirrorY = H - lastPlatformY - 100;
                Texture platMir = new Texture("./img/Tiles/Tile_05_flip.png",
                        new Point(W, mirrorY), 110, 100);
                obstacles.add(new Obstacle(platMir, W, mirrorY, -vitY, ampY, false, true));
                fen.ajouter(platMir);
                cooldown = 25;
            }
        }

        // --- EFFETS RYTHMÉS (sans flash) ---
        if (audioDevice != null) {
            // Lissage de l'amplitude pour des transitions fluides
            smoothAmplitude = smoothAmplitude * 0.85f + amp * 0.15f;
            float intensity = Math.min(1f, smoothAmplitude / (meanAmplitude * 2.5f));

            // 1) SCREEN SHAKE sur les pics forts
            if (amp > meanAmplitude * 2.0f && shakeTimer <= 0) {
                shakeTimer = 4;
            }
            if (shakeTimer > 0) {
                shakeTimer--;
                int newShakeX = (int) (Math.random() * 10 - 5);
                int newShakeY = (int) (Math.random() * 6 - 3);
                int dx = newShakeX - shakeOffsetX;
                int dy = newShakeY - shakeOffsetY;
                fond1.translater(dx, dy); fond2.translater(dx, dy); fond3.translater(dx, dy);
                sol1.translater(dx, dy); sol2.translater(dx, dy); sol3.translater(dx, dy);
                plafond1.translater(dx, dy); plafond2.translater(dx, dy); plafond3.translater(dx, dy);
                shakeOffsetX = newShakeX;
                shakeOffsetY = newShakeY;
            } else if (shakeOffsetX != 0 || shakeOffsetY != 0) {
                // Remettre en place après le shake
                fond1.translater(-shakeOffsetX, -shakeOffsetY); fond2.translater(-shakeOffsetX, -shakeOffsetY); fond3.translater(-shakeOffsetX, -shakeOffsetY);
                sol1.translater(-shakeOffsetX, -shakeOffsetY); sol2.translater(-shakeOffsetX, -shakeOffsetY); sol3.translater(-shakeOffsetX, -shakeOffsetY);
                plafond1.translater(-shakeOffsetX, -shakeOffsetY); plafond2.translater(-shakeOffsetX, -shakeOffsetY); plafond3.translater(-shakeOffsetX, -shakeOffsetY);
                shakeOffsetX = 0;
                shakeOffsetY = 0;
            }

            // 2) BANDES NÉON PULSANTES (violet/bleu → rouge/orange)
            int oR = (int) (80 + intensity * 175);  // 80 → 255
            int oG = (int) (20 + (1f - intensity) * 30); // subtil
            int oB = (int) (180 * (1f - intensity));  // 180 → 0
            oR = Math.max(0, Math.min(255, oR));
            oG = Math.max(0, Math.min(255, oG));
            oB = Math.max(0, Math.min(255, oB));
            MG2D.geometrie.Couleur glowColor = new MG2D.geometrie.Couleur(oR, oG, oB);
            fen.supprimer(glowBot);
            fen.supprimer(glowTop);
            int glowH = GLOW_H + (int) (intensity * 10); // pulse de 6 à 16px
            glowBot = new Rectangle(glowColor, new Point(0, 100), W, glowH, true);
            glowTop = new Rectangle(glowColor, new Point(0, H - 100 - glowH), W, glowH, true);
            fen.ajouter(glowBot);
            fen.ajouter(glowTop);

            // 3) BARRE DE PROGRESSION QUI PULSE EN HAUTEUR
            float targetBarH = BAR_H + intensity * 14; // max ~22px
            currentBarH = currentBarH * 0.8f + targetBarH * 0.2f;
        }

        // --- COLLISIONS ---
        int pX = joueur.getTex().getA().getX() + 10;
        int pY = joueur.getTex().getA().getY() + 10;
        int pW = 80, pH = 80;

        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Obstacle obs = obstacles.get(i);
            obs.update();
            if (obs.x < -200) {
                fen.supprimer(obs.dessin);
                obstacles.remove(i);
            } else if (obs.collidesWithPlayer(pX, pY, pW, pH)) {
                if (obs.isSpike) {
                    return 3; // Mort
                } else {
                    int obsTop = obs.y + 100;
                    if (joueur.getVelocity() < 0 && (pY + 25) >= obsTop) {
                        joueur.getTex().translater(0, obsTop - pY);
                        joueur.setVelocity(0);
                        joueur.setJumping(false);
                    } else if (pY < obsTop - 30) {
                        return 3; // Mort
                    }
                }
            }
        }

        joueur.bougerJoueur(clavier, fen);
        joueur.updatePhysics(100);
        fen.rafraichir();
        return 0;
    }

    // -------------------------------------------------------
    // NETTOYAGE
    // -------------------------------------------------------
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
        if (flashTime > 0)
            fen.supprimer(flashScreen);
        fen.supprimer(glowBot);
        fen.supprimer(glowTop);
        // Reset shake si actif
        if (shakeOffsetX != 0 || shakeOffsetY != 0) {
            fond1.translater(-shakeOffsetX, -shakeOffsetY); fond2.translater(-shakeOffsetX, -shakeOffsetY); fond3.translater(-shakeOffsetX, -shakeOffsetY);
            sol1.translater(-shakeOffsetX, -shakeOffsetY); sol2.translater(-shakeOffsetX, -shakeOffsetY); sol3.translater(-shakeOffsetX, -shakeOffsetY);
            plafond1.translater(-shakeOffsetX, -shakeOffsetY); plafond2.translater(-shakeOffsetX, -shakeOffsetY); plafond3.translater(-shakeOffsetX, -shakeOffsetY);
        }
        if (victoireAffichee) {
            fen.supprimer(fondVictoire);
            fen.supprimer(fondVictoireOverlay);
            fen.supprimer(texteVictoire);
            fen.supprimer(texteSousVictoire);
        }
        fen.removeKeyListener(clavier);
        fen.getP().removeKeyListener(clavier);
        for (Obstacle obs : obstacles)
            fen.supprimer(obs.dessin);
        obstacles.clear();
        if (player != null)
            player.close();
    }
}