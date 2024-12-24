#!/bin/bash
# This script zips the content of the src/ folder, ready to be returned to your teacher.
TOCOMPRESS=$(dirname $(readlink -f $0))
DIRNAME=$(basename $TOCOMPRESS)
projectname=$(cat $TOCOMPRESS/.project | grep "<name>" | head -1 | sed -e "s#.*>\\([^<]*\\)<.*#\\1#g"); echo $projectname
ZIPNAME=$projectname-$(whoami).project.zip
if [ "$1" == "full" ]; then ZIPNAME=final-$projectname.zip; fi
TODIR=$(dirname $TOCOMPRESS)
cd $TODIR
[ -f $ZIPNAME ] && rm -f $ZIPNAME
zip -r $ZIPNAME $DIRNAME/src $DIRNAME/README* $DIRNAME/.classpath  $DIRNAME/.project $DIRNAME/.settings
if [ "$1" == "full" ]; then zip -x "$DIRNAME/bin/*" -u -r $ZIPNAME $DIRNAME/*; fi
echo "Created: $TODIR/$ZIPNAME"

