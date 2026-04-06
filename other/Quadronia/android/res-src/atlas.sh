#! /bin/sh
find . -type d -name "$1-atlas" -exec ./gdxatlas.sh {} atlas \;
