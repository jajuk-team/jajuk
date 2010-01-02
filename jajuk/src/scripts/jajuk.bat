@echo off

::  Jajuk launching script for Windows, assumes javaw (x86) program is in the default location: env. variable %ProgramFiles(x86)% + Java + jre6
::  Contains fixes for #1503 (Problem under Seven + JRE 64 bits), see http://trac.jajuk.info/ticket/1503
::  If available, we start jajuk using a 32 bits JVM  

::  JVM arguments used by Jajuk : 
::  * -client : Use the client JVM, optimized for single user usage [Don't change this setting]
::  * -Xms: Initial Heap (total JVM reserved memory) size. We set it to a pretty large value because it requires resources to expand heap size and it causes a blanck when using java player. [it can be reduced to 25M by some users if required]
::  * -Xmx: Maximal Heap size. We set a large size because Out Of Memory errors make the application crash. In some rare cases, very large collection (>200Gb) users could increase this setting (see Performance section in the manual). We don't set it to a lower value to avoid Out of Memory issues when performing memory consuming tasks (like generating thumbs). We don't set an higher value because some operations (still generating thumbs) could quickly reach any value and setting this value force the JVM to clear the memory when near to this value (Garbage collecting) [Change this setting only if you have very large collection, do not reduce it]
::  * -XX:MinHeapFreeRatio -XX:MaxHeapFreeRatio: fine running parameters that optimizes JVM to garbage collecting as rarely as possible (because a gc 'end of the world' causes an audio blanck). These values have been set by experience [keep these parameters as it]

:: set default location for JRE6
SET JAVA_HOME_x86=%ProgramFiles(x86)%\Java\jre6

:: check if first parameter is given (java home)
if not "%1" == "" if not "%1" == "." (SET JAVA_HOME_x86=%1)

:: check if path exists and set PATH env. variable
if not exist "%JAVA_HOME_x86%" goto NOT_SET_PATH

SET PATH=%JAVA_HOME_x86%\bin;%PATH%
goto END_SET_PATH

:NOT_SET_PATH
@echo.
@echo WARNING: path '%JAVA_HOME_x86%' does not exist !!!
@echo.
goto SHOW_JRE_VER
:END_SET_PATH

:: print out java home x86 path
@echo.
@echo *** JAVA_HOME_x86 ***
@echo %JAVA_HOME_x86%
@echo.

:: print out java version
:SHOW_JRE_VER
@echo *** JRE version ***
java -version
@echo.

:: change to jajuk bin dir and start
@echo.
@echo *** STARTING JAJUK ***
PUSHD bin
java -client -Xms20M -Xmx512M -XX:MinHeapFreeRatio=5 -XX:MaxHeapFreeRatio=10 -jar jajuk.jar -notest
POPD
