#! /bin/bash

#
# This is a startup script for devs. It's not intended for production. See the supervisor config for that.
#
# And, a troubleshooting tip... this file is filtered by the Maven resources plugin. If your editor updates this
# file in the "target" directory after you edit the one in "src/main/scripts" without filtering it, you'll get a
# "bad substitution" error when you run the script.
#

# We're going to be opinionated about logging frameworks
LOG_DELEGATE="-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory"
KEY_PASS_CONFIG="-Djiiify.key.pass=${jiiify.key.pass}"
WATCH_FOLDER_DIR="-Djiiify.watch.folder=${jiiify.watch.folder}"
JIIIFY_PORT="-Djiiify.port=${jiiify.port}"
JIIIFY_HOST="-Djiiify.host=${jiiify.host}"
DROPWIZARD_METRICS="-Dvertx.metrics.options.enabled=true -Dvertx.metrics.options.registryName=jiiify.metrics"
JMX_METRICS="-Dcom.sun.management.jmxremote -Dvertx.metrics.options.jmxEnabled=true"
# For tools like Eclipse's Debugging
JDWP_AGENTLIB="-agentlib:jdwp=transport=dt_socket,address=9003,server=y,suspend=n"
# For tools like VisualVM or JConsole (Note: only for use on dev's localhost since there is no configured security)
JMX_REMOTE="-Dcom.sun.management.jmxremote.port=9001 -Dcom.sun.management.jmxremote.authenticate=false"
JMX_REMOTE="$JMX_REMOTE -Dcom.sun.management.jmxremote.ssl=false"
# If you want to temporarily run remotely, after building on the remote machine, change these two variables
JMX_REMOTE="$JMX_REMOTE -Djava.rmi.server.hostname=localhost -Dcom.sun.management.jmxremote.local.only=true"
TOOLING="$DROPWIZARD_METRICS $JMX_METRICS"
AUTHBIND=""
JIIIFY_CONFIG=""
JKS_CONFIG=""
JCEKS_CONFIG=""
XMX_CONFIG="${jiiify.memory}"
JIIIFY_CORES="-Djiiify.cores=${jiiify.cores}"
HEAP_DUMP_CONFIG="-XX:+HeapDumpOnOutOfMemoryError"
AUTH_CONFIG="-Djiiify.ignore.auth=${jiiify.ignore.auth}"

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

if [ -e "${jiiify.jceks}" ]; then
  JCEKS_CONFIG="-Djiiify.jceks=${jiiify.jceks}"
fi

if [[ "${dev.tools}" == *"JDWP_AGENTLIB"* ]]; then
  echo "[DEBUG] Using JDWP_AGENTLIB for JDWP connections (port 9003)"
  TOOLING="$TOOLING $JDWP_AGENTLIB"
fi

if [[ "${dev.tools}" == *"JMX_REMOTE"* ]]; then
  echo "[DEBUG] Using JMX_REMOTE for JMX connections (port 9001)"
  TOOLING="$TOOLING $JMX_REMOTE"
fi

if [[ ! -z "$XMX_CONFIG" ]]; then
  XMX_CONFIG="-Xmx${jiiify.memory} -Xms${jiiify.memory}"
else
  XMX_CONFIG="-Xmx${system.free.memory} -Xms${system.free.memory}"
fi

# Start up a Solr instance automatically if we have Docker installed
if hash docker 2>/dev/null; then
  CONTAINER_ID=$(docker ps -q --filter "name=jiiify_solr")

  # First check whether our Jiiify Solr container is active
  if [ -z "${CONTAINER_ID}" ]; then
    CONTAINER_ID=$(docker ps -a -q --filter "name=jiiify_solr")
    PING="http://localhost:8983/solr/jiiify/admin/ping"

    # If container has never been created, create it and its Solr core
    if [ -z "$CONTAINER_ID" ]; then
      CONTAINER_ID=$(docker run --name jiiify_solr -d -p 8983:8983 -t solr:alpine)

      for INDEX in $(seq 1 20); do
    	# Create the solr core
        docker exec -it --user=solr jiiify_solr bin/solr create_core -c jiiify >/dev/null 2>&1
        RESPONSE_CODE=$(docker exec -it --user=solr jiiify_solr wget --server-response $PING 2>&1 | awk '/^  HTTP/{print $2}')

        if [ "$RESPONSE_CODE" == "200" ]; then
          echo "Solr connection established"
          break
        elif [ $INDEX == 10 ]; then
          echo "[ERROR] startup.sh | Failed to start Solr server"
          exit 1
        else
          echo "Waiting on Solr..."
          sleep 6
        fi
      done

      echo "[INFO] startup.sh | Jiiify Solr [${CONTAINER_ID:0:12}] created and started..."
    else
      CONTAINER_ID=$(docker start ${CONTAINER_ID})

      for INDEX in $(seq 1 20); do
        RESPONSE_CODE=$(docker exec -it --user=solr jiiify_solr wget --server-response $PING 2>&1 | awk '/^  HTTP/{print $2}')

        if [ "$RESPONSE_CODE" == "200" ]; then
          echo "Solr connection established"
          break
        elif [ $INDEX == 10 ]; then
          echo "[ERROR] startup.sh | Failed to start Solr server"
          exit 1
        else
          echo "Waiting on Solr..."
          sleep 6
        fi
      done

      echo "[INFO] startup.sh | Existing Jiiify Solr [${CONTAINER_ID}] restarted..."
    fi
  else
    echo "[INFO] startup.sh | Jiiify Solr [${CONTAINER_ID}] is already running..."
  fi
else
  echo "[WARNING] The administrative interface's browse and search won't work without Solr"
fi

$AUTHBIND java $HEAP_DUMP_CONFIG $XMX_CONFIG $LOG_DELEGATE $KEY_PASS_CONFIG $WATCH_FOLDER_DIR \
  $JKS_CONFIG $JCEKS_CONFIG $AUTH_CONFIG $JIIIFY_PORT $JIIIFY_HOST $TOOLING $JIIIFY_CORES $1 \
  -jar target/build-artifact/jiiify-${project.version}.jar $JIIIFY_CONFIG
