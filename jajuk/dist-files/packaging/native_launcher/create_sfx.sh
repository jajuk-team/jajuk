#!/bin/sh
/usr/bin/7z a jajuk.7z -mx=9 -t7z -aoa *.jar *.manifest setup.exe launcher.ini
cat 7zS.sfx config7z.ini jajuk.7z > $1
