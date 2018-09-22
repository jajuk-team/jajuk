REM Windows launcher for Jajuk 
JAVA_OPTIONS="-cp "lib\*;jajuk.jar" -Xms30M -Xmx2G -Xverify:none -Djava.library.path=lib\native"

#Jajuk options
JAJUK_OPTIONS="-test"

java %JAVA_OPTIONS% org.jajuk.Main %_OPTIONS%