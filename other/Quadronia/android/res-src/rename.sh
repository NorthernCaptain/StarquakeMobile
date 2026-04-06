#! /bin/sh
newname=`echo $1 | sed -r 's/_([0123456789])/_t\1/'`
echo "$1 -> $newname"
mv -f $1 $newname
