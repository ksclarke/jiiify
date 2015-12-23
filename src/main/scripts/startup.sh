#! /bin/bash

# We're going to be opinionated about logging frameworks
LOG_DELEGATE="-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory"
KEY_PASS_CONFIG="-Djiiify.key.pass=${jiiify.key.pass}"
JIIIFY_TEMP_DIR="-Djiiify.temp.dir=${jiiify.temp.dir}"
WATCH_FOLDER_DIR="-Djiiify.watch.folder=${jiiify.watch.folder}"
JIIIFY_PORT="-Djiiify.port=${jiiify.port}"
DROPWIZARD_METRICS="-Dvertx.metrics.options.enabled=true -Dvertx.metrics.options.registryName=jiiify.metrics"
JMX_METRICS="-Dcom.sun.management.jmxremote -Dvertx.metrics.options.jmxEnabled=true"
# For tools like Eclipse's Debugging
JDWP_AGENTLIB="-agentlib:jdwp=transport=dt_socket,address=9003,server=y,suspend=n"
# For tools like VisualVM or JConsole (Note: only for use on dev's localhost since there is no configured security)
JMXREMOTE="-Dcom.sun.management.jmxremote.port=9001 -Dcom.sun.management.jmxremote.authenticate=false"
JMXREMOTE="$JMXREMOTE -Dcom.sun.management.jmxremote.ssl=false"
TOOLING="$DROPWIZARD_METRICS $JMX_METRICS"
AUTHBIND=""
JIIIFY_CONFIG=""
JKS_CONFIG=""

# If we have authbind and it's configured to run our port, let's use it
if hash authbind 2>/dev/null; then
  if [ -e "/etc/authbind/byport/${jiiify.port}" ] ; then
    AUTHBIND="authbind"
  fi
fi

if [ -e "${jiiify.json.config.path}" ]; then
  JIIIFY_CONFIG="-conf ${jiiify.json.config.path}"
fi

if [ -e "${jiiify.jks}" ]; then
  JKS_CONFIG="-Djiiify.jks=${jiiify.jks}"
fi

if [[ "${dev.tools}" == *"JDWP_AGENTLIB"* ]]; then
  echo "[DEBUG] Using JDWP_AGENTLIB for JDWP connections (port 9003)"
  TOOLING="$TOOLING $JDWP_AGENTLIB"
fi

if [[ "${dev.tools}" == *"JMX_REMOTE"* ]]; then
  echo "[DEBUG] Using JMX_REMOTE for JMX connections (port 9001)"
  TOOLING="$TOOLING $JMX_REMOTE"
fi

$AUTHBIND java -Xmx${jiiify.memory} $LOG_DELEGATE $KEY_PASS_CONFIG $JIIIFY_TEMP_DIR $WATCH_FOLDER_DIR \
  $JKS_CONFIG $JIIIFY_PORT $TOOLING $1 -jar target/jiiify-${project.version}-exec.jar $JIIIFY_CONFIG
