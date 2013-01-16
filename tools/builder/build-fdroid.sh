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

[ -e "$BASE/.build" ] && rm -r "$BASE/.build"
mkdir "$BASE/.build"

ruby "$SELF/jpp.rb" $BPARAMS && \
ruby "$SELF/convert-levels.rb" && \
CDIR="`pwd`" && \
cd "$BASE/.build" && \
$$NDK$$/ndk-build && \
ruby "$SELF/jpp.rb" --fixlibs $BPARAMS && \
cd "$CDIR"
