@echo off
cd /d "%~dp0"
echo Creation du raccourci sur le Bureau Windows...

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$wsh = New-Object -ComObject WScript.Shell; " ^
  "$desktop = [Environment]::GetFolderPath('Desktop'); " ^
  "$shortcut = $wsh.CreateShortcut(\"$desktop\Sen Thioono Diexna.lnk\"); " ^
  "$shortcut.TargetPath = 'wscript.exe'; " ^
  "$shortcut.Arguments = '\"%~dp0run_silent.vbs\"'; " ^
  "$shortcut.WorkingDirectory = '%~dp0'; " ^
  "$shortcut.IconLocation = '%~dp0app_icon.ico,0'; " ^
  "$shortcut.Description = 'Sen Thioono Diexna - Assistant Numerique des Demarches Administratives'; " ^
  "$shortcut.Save()"

echo =====================================================================
echo  Raccourci 'Sen Thioono Diexna' cree avec succes sur le Bureau !
echo  Icone : %~dp0app_icon.ico
echo =====================================================================
pause
