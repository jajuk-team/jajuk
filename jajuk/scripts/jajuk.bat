REM Jajuk launching script for Windows, assumes javaw program is in the PATH env. variable
cd bin
java -client -Xms25M -Xmx512M -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=20 -Dapple.laf.useScreenMenuBar=true -jar jajuk.jar  -notaskbar TEST_FLAG_REPLACED_BY_ANT
