@echo off
cd /d "%~dp0"
title Sen Thioono Diexna - Assistant Numerique des Demarches Administratives
cls
echo =====================================================================
echo  SEN THIOONO DIEXNA - REPUBLIQUE DU SENEGAL
echo  Demarrage de l'application desktop JavaFX...
echo =====================================================================
echo.
call mvnw.cmd javafx:run
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERREUR] Le lancement a rencontre un probleme.
    pause
)
