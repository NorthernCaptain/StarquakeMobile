#! /bin/sh
cd gfx480
find . -name "*.png" -exec ../enlarge.sh {} ../gfx720 %150 \;
cd ..
/bin/ls gfx720 | xargs -i cp gfx480/armory-atlas/pack.json gfx720/{}/
echo $Done
