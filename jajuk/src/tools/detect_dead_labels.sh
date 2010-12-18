#!/bin/bash
# Copyright 2010, jajuk team
# This script is a tool available for jajuk developers to manage langpack files

# Launch this script from its directory
# CAUTION : some labels could be used indirectly or used by files out of java directory, check all the repository
cd ../main/java

# Find useless labels
while read line;do echo -e "$line\n"; done < org/jajuk/i18n/jajuk.properties | grep -E ".*=.*" | grep -v "?xml" | grep -v '#' | awk -F'=' '{print $1}' > /tmp/list
while read line; do  count=`grep -rH "\"$line" * | grep -v svn | wc -l`; if [[ $count -eq 0 ]]; then echo $line; fi; done < /tmp/list | grep -v "TipOfTheDay" | grep -v "Language_desc" | grep -v "Error." | grep -v "Property" | grep -v "ReportAction" | grep -v "Wizard_" | grep -v "Notificator."

# Note that this errors MAY be useless, check it and that useless errors may have been omitted
#Find useless errors
while read line;do echo -e "$line\n"; done < org/jajuk/i18n/jajuk.properties | grep -E ".*=.*" | grep -v '#' | grep -v "?xml" |awk -F'=' '{print $1}' | grep "Error." > /tmp/errors
while read line; do  code=`echo $line|awk -F '.' '{print $2}'`; trimed_code=`echo $code | sed 's/0*//'`; count=`grep -rHE "showErrorMessage\($code|\"$line\"|showDetailedErrorMessage\($code|new JajukException\($code|Log.error\($code|\(${trimed_code}" org ext | grep -v svn | wc -l`; if [[ $count -eq 0 ]]; then echo $line; fi; done < /tmp/errors

