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
    private MyAudioDevice audioDevice;
    private AdvancedPlayer player;

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

        public Obstacle(Dessin d, int startX) {
            this.dessin = d;
            this.x = startX;
        }
    }

    private ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    private int cooldown = 0;

    public void CreationJeu() {
        fen = new FenetrePleinEcran("JavaDash");
        fond1 = new Texture("./img/background/Day/Background.png", new Point(0, 0), 1280, 1024);
        fond2 = new Texture("./img/background/Day/Background.png", new Point(1280, 0), 1280, 1024);
        fond3 = new Texture("./img/background/Day/Background.png", new Point(2560, 0), 1280, 1024);
        fen.ajouter(fond1);
        fen.ajouter(fond2);
        fen.ajouter(fond3);

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

        // Logique d'apparition en fonction du rythme (amplitude interceptée)
        if (cooldown > 0)
            cooldown--;
        float amp = (audioDevice != null) ? audioDevice.currentAmplitude : 0;

        // Un pic d'amplitude > 6000 est en général un marqueur de grosse basse sur ce
        // type de MP3 (16-bit = 32767 max)
        if (amp > 6000 && cooldown == 0) {
            Dessin d;
            if (Math.random() > 0.5) {
                d = new Triangle(MG2D.geometrie.Couleur.ROUGE, new Point(1280, 100), new Point(1330, 100),
                        new Point(1305, 180), true);
            } else {
                d = new Rectangle(MG2D.geometrie.Couleur.BLEU, new Point(1280, 150), 100, 30, true);
            }
            obstacles.add(new Obstacle(d, 1280));
            fen.ajouter(d);
            cooldown = 25; // Env 0.4 seconde avant de pouvoir respawn un obstacle
        }

        // Mise à jour des obstacles
        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Obstacle obs = obstacles.get(i);
            obs.dessin.translater(-10, 0); // Vitesse double par rapport au fond
            obs.x -= 10;

            if (obs.x < -200) {
                fen.supprimer(obs.dessin);
                obstacles.remove(i);
            }
        }

        fen.rafraichir();
        return game = 1;
    }
}