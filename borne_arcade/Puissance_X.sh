#!/bin/bash
xdotool mousemove 1280 1024
cd projet/Puissance_X
java -cp .:../..:/home/$USER/git/MG2D -Dsun.java2d.pmoffscreen=false Main

# -Dsun.java2d.pmoffscreen=false : Améliore les performances sur les système Unix utilisant X11 (donc Raspbian est concerné).
# -Dsun.java2d.opengl=true : Utilise OpenGL (peut améliorer les performances).
