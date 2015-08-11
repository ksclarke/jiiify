#! /bin/bash

# And we do a little clean up after the integration tests have been run
kill `cat jiiify-it.pid`
rm jiiify-it.pid
