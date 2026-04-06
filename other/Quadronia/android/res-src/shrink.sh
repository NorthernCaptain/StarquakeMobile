#! /bin/sh
name=$(basename $1)
dir=$(dirname $1)
dir=$(basename $dir)

dest=$2/$dir/$name

mkdir -p $2/$dir 2>/dev/null
if [ -e $dest ]; then
  echo -n "SKIPPED:$1 ->"
elif [[ $name =~ ".9.png" ]]; then
  echo -n "copy   :$1 ->"
  cp -a $1 $dest
else
  echo -n "RESIZE :$1 ->"
  convert $1 -filter Lanczos -resize $3 PNG32:$dest.1
  convert $dest.1 -sharpen 0x1.4 PNG32:$dest
  rm -f $dest.1
fi

echo "$dest"

