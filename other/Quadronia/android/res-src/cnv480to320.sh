#! /bin/sh
cd gfx480
find . -name "*.png" -exec ../shrink.sh {} ../gfx320 %66.6 \;
cd ..
/bin/ls gfx320 | xargs -i cp gfx480/armory-atlas/pack.json gfx320/{}/
echo $Done
