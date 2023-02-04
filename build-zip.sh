#!/bin/bash
# Creates a modular zip in the build directory.
#
# To test with local UniMix, set ALL_BUILD_EXTRA_ARG=-PmixinSourceIsLocal

./gradlew clean

./gradlew module-mixin:build -PmixinSource=spongepowered
./gradlew module-mixin:build -PmixinSource=fabric
./gradlew module-mixin:build -PmixinSource=gasmix
./gradlew module-mixin:build -PmixinSource=gtnh

./gradlew module-all:build $ALL_BUILD_EXTRA_ARG

mkdir build -p

zip -j build/unimixins-modular-INSERT_VERSION.zip $(find -name "_unimixins-*.jar" | grep "build/libs" | grep -v sources | grep -v "module-all" | xargs)
