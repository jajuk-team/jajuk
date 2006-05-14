REM Jajuk launching script for Windows, assumes javaw program is in the PATH env. variable
cd bin
java -client -Xms35M -Xmx512M -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=30 -Xincgc -XX:NewRatio=12 -Dapple.laf.useScreenMenuBar=true -jar jajuk.jar  -notaskbar TEST_FLAG_REPLACED_BY_ANT
