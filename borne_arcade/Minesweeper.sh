#!/bin/bash
xdotool mousemove 1280 1024
cd projet/Minesweeper
touch highscore
java -cp .:../..:/home/pi/git/MG2D Minesweeper
