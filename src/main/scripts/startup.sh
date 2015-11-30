#! /bin/bash

# We're going to be opinionated about logging frameworks
LOG_DELEGATE="-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory"
KEY_PASS_CONFIG="-Djiiify.key.pass=${jiiify.key.pass}"
JIIIFY_TEMP_DIR="-Djiiify.temp.dir=${jiiify.temp.dir}"
WATCH_FOLDER_DIR="-Djiiify.watch.folder=${jiiify.watch.folder}"
JIIIFY_PORT="-Djiiify.port=${jiiify.port}"
DROPWIZARD_METRICS="-Dvertx.metrics.options.enabled=true -Dvertx.metrics.options.registryName=jiiify.metrics"
JMX_METRICS="-Dcom.sun.management.jmxremote -Dvertx.options.jmxEnabled=true"
AUTHBIND=""
JIIIFY_CONFIG=""

# If we have authbind and it's configured to run our port, let's use it
if hash authbind 2>/dev/null; then
  if [ -e "/etc/authbind/byport/${jiiify.port}" ] ; then
    AUTHBIND="authbind"
  fi
fi

if [ -e "${jiiify.json.config.path}" ]; then
  JIIIFY_CONFIG="-conf ${jiiify.json.config.path}"
fi

$AUTHBIND java -Xmx${jiiify.memory} $LOG_DELEGATE $KEY_PASS_CONFIG $JIIIFY_TEMP_DIR $WATCH_FOLDER_DIR \
  $JIIIFY_PORT $DROPWIZARD_METRICS $1 -jar target/jiiify-${project.version}-exec.jar $JIIIFY_CONFIG
