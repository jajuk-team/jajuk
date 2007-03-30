#!/bin/sh
#Jajuk launching script for unix, assumes javaw program is in the PATH env. variable

#Go to installation directory we get with own shell path
cd `dirname $0`/bin

#Launch jajuk
java -client -Xms35M -Xmx512M -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=30 -Xincgc -XX:NewRatio=12 -jar jajuk.jar -notaskbar TEST_FLAG_REPLACED_BY_ANT