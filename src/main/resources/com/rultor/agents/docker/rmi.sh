#!/bin/bash
set -e

images=$(docker images | grep -E "[0-9]+ months ago" | tr -s ' ' | cut -d ' ' -f 3)
for i in ${images}; do
  docker rmi "${i}" || echo "failed to remove image ${i}"
done
