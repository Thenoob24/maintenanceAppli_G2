# Script PowerShell pour lancer le jeu JavaDash
# Se place dans le répertoire du script si besoin
$PSScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $PSScriptRoot

# Compilation de tous les fichiers Java du jeu
Write-Host "Compilation des fichiers Java..." -ForegroundColor Cyan
javac -cp ".;..\.." *.java
if ($LASTEXITCODE -ne 0) {
    Write-Host "Erreur de compilation !" -ForegroundColor Red
    Read-Host "Appuyez sur Entree pour quitter"
    exit 1
}
Write-Host "Compilation reussie !" -ForegroundColor Green

# Lance l'exécutable Java avec le classpath adapté à Windows
java -cp ".;..\.." Main
