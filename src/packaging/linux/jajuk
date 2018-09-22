#!/bin/bash
#JVM arguments used by Jajuk. It's important  to notice that some of this tuning is useful only when using Java player (when mplayer is not available)
# -cp : classpath to libs. Note: Incompatible with -jar option.
# -Xms : Initial Heap (total JVM reserved memory) size. We set it to a pretty large value because it requires resources to expand heap size.
# -Xmx: Maximal Heap size. We set a large size because Out Of Memory errors make the application crash. In some rare cases, very large collection (>200Gb) users could increase this setting (see Performance section in the manual) [Change this setting only if you have very large collection, do not reduce it]
# -Xverify:none : quicker startup, no bytecode check
# -Djava.library.path : directory containing the .so files.
JAVA_OPTIONS="-cp "../lib/*:jajuk.jar" -Xms30M -Xmx2G -Xverify:none -Djava.library.path=../lib"

# Go in this script directory to use relative paths
DIR="$(dirname "$(readlink -f "$0")")"
cd $DIR

#Jajuk options
JAJUK_OPTIONS="-test"

##let's start Jajuk displaying the command:
set -o xtrace
java $JAVA_OPTIONS org.jajuk.Main $JAJUK_OPTIONS &

# Kill this bash script but let java process running
disown
exit 0
