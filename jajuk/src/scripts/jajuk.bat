REM Jajuk launching script for Windows, assumes javaw program is in the PATH env. variable

REM JVM arguments used by Jajuk. It's important  to notice that some of this tuning is useful only when using Java player (when mplayer is not available)
REM * -client : Use the client JVM, optimized for single user usage [Don't change this setting]
REM * -Xms: Initial Heap (total JVM reserved memory) size. We set it to a pretty large value because it requires resources to expand heap size and it causes a blanck when using java player. [it can be reduced to 25M by some users if required]
REM * -Xmx: Maximal Heap size. We set a large size because Out Of Memory errors make the application crash. In some rare cases, very large collection (>200Gb) users could increase this setting (see Performance section in the manual). We don't set it to a lower value to avoid Out of Memory issues when performing memory consuming tasks (like generating thumbs). We don't set an higher value because some operations (still generating thumbs) could quickly reach any value and setting this value force the JVM to clear the memory when near to this value (Garbage collecting) [Change this setting only if you have very large collection, do not reduce it]
REM * -XX:MinHeapFreeRatio -XX:MaxHeapFreeRatio: fine running parameters that optimizes JVM to garbage collecting as rarely as possible (because a gc 'end of the world' causes an audio blanck). These values have been set by experience [keep these parameters as it]

cd bin
java -client -Xms20M -Xmx512M -XX:MinHeapFreeRatio=5 -XX:MaxHeapFreeRatio=10 -jar jajuk.jar TEST_FLAG_REPLACED_BY_ANT
