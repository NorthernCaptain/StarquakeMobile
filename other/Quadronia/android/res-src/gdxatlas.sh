#! /bin/sh
inputDir=$1
outputDir=../assets/data/$1
packFileName=$2
mkdir -p $outputDir 2>/dev/null
java -cp /home/leo/home/android/libgdx-1.5.0/gdx.jar:/home/leo/home/android/libgdx-1.5.0/extensions/gdx-tools/gdx-tools.jar com.badlogic.gdx.tools.texturepacker.TexturePacker "$inputDir" "$outputDir" "$packFileName"
