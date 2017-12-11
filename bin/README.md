# Combine scripts and jar
A Java program can be executed even with "`java -jar program.jar`", but there is a way to make it easier.

Bash(*nix)
```bash
cat http.src.sh sws.jar > http.sh
chmod +x http.sh
```

Batch(Windows)
```bat
copy /b http.src.bat+sws.jar http.bat
```
