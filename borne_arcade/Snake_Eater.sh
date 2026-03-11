#!/bin/bash
xdotool mousemove 1280 1024
cd projet/Snake_Eater
touch highscore
java -cp .:../..:/home/pi/git/MG2D Snake_Eater
