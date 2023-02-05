#!/bin/bash
# Creates a modular zip in the build directory.
#
# To test with local UniMix, set ALL_BUILD_EXTRA_ARG=-Plocal

set -e

./gradlew clean

./gradlew module-mixin:build -PmixinSource=spongepowered
./gradlew module-mixin:build -PmixinSource=fabric
./gradlew module-mixin:build -PmixinSource=gasmix
./gradlew module-mixin:build -PmixinSource=gtnh

./gradlew module-all:build $ALL_BUILD_EXTRA_ARG

mkdir build -p

OUT_ZIP=build/unimixins-modular-1.7.10-$(git describe --tags --dirty).zip
rm -f $OUT_ZIP
zip -j $OUT_ZIP $(find -name "_unimixins-*.jar" | grep "build/libs" | grep -v sources | grep -v "module-all" | xargs)
