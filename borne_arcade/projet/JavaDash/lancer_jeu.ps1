# Script PowerShell pour lancer le jeu JavaDash
# Se place dans le répertoire du script si besoin
$PSScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $PSScriptRoot

# Lance l'exécutable Java avec le classpath adapté à Windows
# Remplace les ":" du bash par des ";" sous Windows
java -cp ".;..\.." Main
