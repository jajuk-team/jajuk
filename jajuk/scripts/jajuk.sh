#!/bin/sh
#Jajuk launching script for unix, assumes javaw program is in the PATH env. variable

#Go to installation directory we get with own shell path
cd `dirname $0`/bin

#Set right JAVA_HOME variable according to JRE runtime
export JAVA_HOME=`java -cp jajuk.jar org.jajuk.SystemAnalyser`
echo JAVA_HOME=$JAVA_HOME

#Launch jajuk
java -client -Xms25M -Xss5M -Xmx40M -Djava.library.path=../native -jar jajuk.jar -notaskbar