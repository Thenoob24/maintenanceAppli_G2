# JavaDash - Jeu Rythmique MG2D

JavaDash est un jeu de plateforme rythmique inspiré de Geometry Dash, développé en Java à l'aide de la bibliothèque graphique **MG2D**. Le jeu synchronise ses éléments de gameplay (obstacles, effets visuels) avec l'amplitude de la musique.

## 🚀 Caractéristiques principales

- **Gameplay Rythmique** : Les obstacles (pics, plateformes) apparaissent en fonction du rythme de la musique.
- **Effets Dynamiques** :
    - **Pulsation des Orbes** : Les orbes jaunes pulsent visuellement avec l'amplitude sonore.
    - **Inversion de Gravité** : La gravité s'inverse lors des moments forts de la musique, changeant le décor et le comportement du joueur.
    - **Feedback Visuel** : Secousses de l'écran (Screen Shake) et néons pulsants synchronisés.
- **Documentation Complète** : L'intégralité du code source est documentée selon les standards Javadoc.
- **Système de Physique** : Gestion des sauts, de la gravité et des collisions avec différents types d'hitboxes (rectangulaires et triangulaires).

## 🛠️ Installation et Exécution

### Prérequis
- Java Development Kit (JDK) 8 ou supérieur.
- Bibliothèque MG2D (incluse dans le projet).

### Lancer le jeu
Rendez-vous sur la borne arcade et appuyez sur le bouton "Start" pour lancer le jeu.

## 📂 Structure du Projet

- **Main.java** : Point d'entrée du programme, gère la transition entre les menus et le jeu.
- **Menu.java** : Gestion de l'écran d'accueil et des entrées utilisateur pour démarrer.
- **Jeu.java** : Cœur du moteur de jeu (boucle de rendu, traitement audio, spawn d'obstacles).
- **Player.java** : Logique de mouvement et physique du joueur.
- **Obstacle.java** : Définition des différents dangers (pics fixés/mobiles, plateformes, orbes).
- **RotatingTexture.java** : Extension de la classe Texture de MG2D permettant la rotation continue (utilisée pour le joueur).
- **Dossier `img/`** : Contient toutes les ressources graphiques (joueur, fonds, tuiles, ennemis).
- **Dossier `sound/`** : Contient la musique du jeu.

## 🎮 Commandes (Borne Arcade)
- **JOystick J1 HAUT** : Sauter / Activer une orbe.
- **Bouton J1 A / B** : Sauter (alternatif).

---
*Ce projet a été réalisé dans le cadre du BUT3 Informatique - Maintenance Applicative (G2).*
