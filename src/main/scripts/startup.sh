#! /bin/bash

# We're going to be opinionated about logging frameworks
LOG_DELEGATE="-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory"
KEY_PASS_CONFIG="-Djiiify.key.pass=${jiiify.key.pass}"
JIIIFY_TEMP_DIR="-Djiiify.temp.dir=${jiiify.temp.dir}"
WATCH_FOLDER_DIR="-Djiiify.watch.folder=${jiiify.watch.folder}"

java $LOG_DELEGATE $KEY_PASS_CONFIG $JIIIFY_TEMP_DIR $WATCH_FOLDER_DIR $1 \
  -jar target/jiiify-${project.version}-exec.jar
