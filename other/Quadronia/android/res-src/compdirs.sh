#! /bin/sh
dir1="$1"
dir2="$2"

filen=/tmp/$$

pushd $dir1
find . -name "*.png" | sort >$filen.1
popd
pushd $dir2
find . -name "*.png" | sort >$filen.2
popd

echo "Legend:"
echo "> missed in $dir1"
echo "< missed in $dir2"
echo "--------------------------"
diff $filen.1 $filen.2

rm -f $filen.*

