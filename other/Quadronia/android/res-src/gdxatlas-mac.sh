#! /bin/sh
inputDir=$1
outputDir=../assets/data/$1
packFileName=$2
mkdir -p $outputDir 2>/dev/null
java -cp /Users/leo/projects/runnable-texturepacker.jar com.badlogic.gdx.tools.texturepacker.TexturePacker "$inputDir" "$outputDir" "$packFileName"
