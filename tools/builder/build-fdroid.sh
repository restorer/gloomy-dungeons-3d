#!/bin/sh

if [ "$1" = "" ] ; then
	echo "Usage: build-fdroid.sh <preset-name>"
	exit
fi

SELF=`dirname "$0"`
SELF="`pwd`/$SELF"
TEMP="$SELF/../../temp"
BASE="$SELF/../.."

if [ ! -e "$SELF/presets/$1" ] ; then
	echo "Preset \"$1\" not found"
	exit
fi

BPARAMS=""

while read LINE ; do
	if [ "$LINE" != "" ] ; then
		FSYM=$( echo "$LINE" | awk '{ string=substr($0, 1, 1); print string; }' )

		if [ "$FSYM" != "#" ] ; then
			if [ "$BPARAMS" = "" ] ; then
				BPARAMS="$LINE"
			else
				BPARAMS="$BPARAMS $LINE"
			fi
		fi
	fi
done < "$SELF/presets/$1"

if [ -e "$BASE/.build" ] ; then
	rm -r "$BASE/.build"/* 2> /dev/null
else
	mkdir "$BASE/.build"
fi

NDKBUILDTOOL="ndk-build"

if [ "$NDK" != "" ] ; then
	NDKBUILDTOOL="$NDK/ndk-build"
fi

ruby "$SELF/jpp.rb" $BPARAMS && \
ruby "$SELF/convert-levels.rb" && \
CDIR="`pwd`" && \
cd "$BASE/.build" && \
$NDKBUILDTOOL && \
ruby "$SELF/jpp.rb" --fixlibs $BPARAMS && \
cd "$CDIR"
