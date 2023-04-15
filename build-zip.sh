#!/bin/bash
# Creates a modular zip in the build directory.
#
# To test with local UniMix, set ALL_BUILD_EXTRA_ARG=-Plocal

set -e

./gradlew clean
./gradlew build

mkdir build -p

OUT_ZIP=build/unimixins-modular-1.7.10-$(git describe --tags --dirty).zip
rm -f $OUT_ZIP
zip -j $OUT_ZIP $(find -name "-unimixins-*.jar" | grep "build/libs" | grep -v sources | grep -v "module-all" | xargs)
