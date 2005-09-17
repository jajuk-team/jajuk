#!/bin/sh
#Jajuk launching script for unix, assumes javaw program is in the PATH env. variable

#Go to installation directory we get with own shell path
cd `dirname $0`/bin

#Launch jajuk
java -client -Xms25M -jar jajuk.jar -notaskbar DEBUG_FLAG_REPLACED_BY_ANT