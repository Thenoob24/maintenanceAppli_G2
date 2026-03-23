# Script PowerShell pour lancer le jeu JavaDash depuis la racine du projet
$PSScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $PSScriptRoot

# Se déplace dans le dossier du jeu
Set-Location "projet\JavaDash"

# Lance l'exécutable Java avec le classpath adapté à Windows
java -cp ".;..\.." Main

# Revient à la racine
Set-Location "..\.."
