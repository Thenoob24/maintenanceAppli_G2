#!/bin/bash
xdotool mousemove 1280 1024
cd projet/Kowasu_Renga
touch highscore
java -cp .:../..:/home/pi/git/MG2D Kowasu_Renga
