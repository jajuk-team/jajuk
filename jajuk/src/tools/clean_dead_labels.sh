#!/bin/bash
# Copyright 2011, jajuk team
# This script is a tool available for jajuk developers to manage langpack files

# Launch this script from its directory
for label in `./detect_dead_labels.sh`;
do
 echo cleaning up : $label
 for file in ../main/java/org/jajuk/i18n/*.properties
 do 
  echo Entering file : $file
  sed -i "/^$label/D" $file
 done
done

