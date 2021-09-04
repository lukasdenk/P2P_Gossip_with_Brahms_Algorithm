To build and run the jar in IntelIJ do the following:
1. Go to "File"
2. "Project Structure"
3. "Artifacts"
4. "+"
5. "JAR"
6. "From module with dependencies..."
7. As main class chose "main.MainKt"
8. Press "OK"
9. Press "OK"
10. Go to "Build"
11. "Build Artifacts..."
12. "Gossip:Jar" > "Build"
13. Copy **Gossip.jar** and **service.ini** into your deployment folder
    1. For Windows PowerShell
       1. Run `.\deployment\windows\update_jar_and_ini.ps1` from root project folder.
    2. For Windows CMD
       1. Run `.\deployment\windows\update_jar_and_ini.cmd` from root project folder.
14. Run the jar file
    1. For Windows PowerShell
       1. Run `.\deployment\windows\run.ps1` from root project folder.
    2. For Windows CMD
       1. Run `.\deployment\windows\run.cmd` from root project folder.