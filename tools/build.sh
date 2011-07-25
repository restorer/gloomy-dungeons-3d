#!/bin/bash

if [ "$1" = "" ] ; then
	echo "Usage: build.sh <debug|release>"
	exit
fi

SELF=`dirname "$0"`
BASE="$SELF/.."

[ -e "$SELF/GloomyDungeons.apk" ] && rm "$SELF/GloomyDungeons.apk"
[ -e "$BASE/build" ] && rm -r "$BASE/build"
mkdir "$BASE/build"

ruby "$SELF/jpp.rb" && \
ruby "$SELF/convert-levels.rb" && \
pushd "$BASE/build" && \
ndk-build && \
ant $1 && \
popd && \
cp "$BASE/build/bin/GloomyDungeons-$1.apk" "$SELF/GloomyDungeons.apk"
