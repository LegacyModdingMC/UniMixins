# Run this from the root dir of this repo to update the buildscript in another
# repo, or install it in a new repo.
# The recommended workflow is to have a clean working and staging area in the
# target repo, so you easily correct the changes afterwards.
#
# Example usage: ./updater/install.sh ~/SomeMod

rsync -av . $1 --exclude-from='updater/excludes.txt' --exclude-from='updater/persist.txt'
rsync -av . $1 --files-from='updater/persist.txt' --ignore-existing
rsync -av .github.disabled/* $1/.github

(cd $1; git update-index --chmod=+x gradlew)
(cd $1; git update-index --chmod=+x publish/gradlew)
