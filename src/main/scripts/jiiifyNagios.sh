#! /bin/bash

#
# A simple bash script that checks the various statuses of a Jiiify image server.
#
# Usage:
#   ./jiiifyNagios my.domain.edu [basic]
#
if [ $# -ne 2 ]; then
  echo "Usage: ./jiiifyNagios my.domain.edu [basic]"
else
  if [ ${1:0:8} != 'https://' ]; then
    URL='https://'${1%/}
  else
    URL=${1%/}
  fi

  STATUS=`curl -k -s "${URL}/status/${2}"`
  echo "$STATUS" | cut -d '_' -f 2

  if [[ ${STATUS:0:2} == 'OK' ]]; then
    exit 0
  elif [[ ${STATUS:0:7} == 'WARNING' ]]; then
    exit 1
  elif [[ ${STATUS:0:8} == 'CRITICAL' ]]; then
    exit 2
  else
    exit 3
  fi
fi
