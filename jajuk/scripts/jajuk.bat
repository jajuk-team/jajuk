REM Jajuk launching script for Windows, assumes javaw program is in the PATH env. variable
cd bin
java -client -Xms25M  -Djava.library.path=..\native -jar jajuk.jar  -notaskbar
