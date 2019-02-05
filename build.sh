#!/usr/bin/env bash

set -eu

HERE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd -P )"

. "$HERE/version.sh"

# TODO: ADD THE ANOMALY DETECTION!!
root_dirs=( ${HERE}/apps/bigdl ${HERE}/apps/nwintrusion ${HERE}/apps/flink ${HERE}/apps/kstream ${HERE}/apps/killrweather ${HERE}/apps/akka-kafka-streams-model-server)

# The only allowed arguments are the optional version string (no flag) and the
# flag to just print the Docker image names.
# Note that because VERSION is exported in version.sh, its value will be propagated
# to the subsequent build.sh script invocations.
print_docker_image_names=false
while [[ $# -gt 0 ]]
do
  case $1 in
    -h|--h*)
      echo "usage: $0 [-h|--help] [--print-docker-image] [VERSION]"
      exit 0
      ;;
    --print-docker-image*)
      print_docker_image_names=true
      ;;
    -*)
      echo "ERROR: $0: Unrecognized argument $1"
      exit 1
      ;;
    *)
     VERSION=$1
     ;;
  esac
  shift
done

if $print_docker_image_names
then
  for d in ${root_dirs[@]}
  do
    ( cd "$d/source/core"; sbt -no-colors "set version in ThisBuild := \"$VERSION\"" "show docker::imageNames" ) |  grep -e 'lightbend/[^)]*' --only-matching
  done
  exit $?
fi

echo "$0: Using version $VERSION"

for d in ${root_dirs[@]}
do
  "$d/build.sh"
done
