#!/bin/bash

if [[ "$1" = "" || "$2" = "" ]] ; then
	echo "Usage: build.sh <preset-name> <build-type>"
	exit
fi

SELF=`dirname "$0"`
SELF=`realpath "$SELF"`
TOOLS=`realpath "$SELF/.."`
BASE=`realpath "$SELF/../.."`

if [ ! -e "$SELF/presets/$1" ] ; then
	echo "Preset \"$1\" not found"
	exit
fi

SUFFIX="$1-$2"
BTYPE="$2"
BPARAMS=""

while read LINE ; do
	if [ "$LINE" != "" ] ; then
		if [ "${LINE:0:1}" != "#" ] ; then
			if [ "$BPARAMS" = "" ] ; then
				BPARAMS="$LINE"
			else
				BPARAMS="$BPARAMS $LINE"
			fi
		fi
	fi
done < "$SELF/presets/$1"

[ -e "$TOOLS/GloomyDungeons-$SUFFIX.apk" ] && rm "$TOOLS/GloomyDungeons-$SUFFIX.apk"
[ -e "$BASE/.build" ] && rm -r "$BASE/.build"
mkdir "$BASE/.build"

ruby "$SELF/jpp.rb" $BPARAMS && \
ruby "$SELF/convert-levels.rb" && \
pushd "$BASE/.build" && \
ndk-build && \
ruby "$SELF/jpp.rb" --fixlibs $BPARAMS && \
ant "$BTYPE" && \
popd && \
cp "$BASE/.build/bin/GloomyDungeons-$BTYPE.apk" "$TOOLS/GloomyDungeons-$SUFFIX.apk"
