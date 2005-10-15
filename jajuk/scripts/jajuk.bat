REM Jajuk launching script for Windows, assumes javaw program is in the PATH env. variable
cd bin
java -client -Xms25M -XX:+UseParallelGC -jar jajuk.jar  -notaskbar TEST_FLAG_REPLACED_BY_ANT
