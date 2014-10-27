#!/usr/bin/env bash
#
# Copyright 2014 Atos
# Contact: Atos <roman.sosa@atos.net>
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#

#
# Assume MP and sla-core are running at localhost in a clear state.
#
# The following ENV vars may be previously assigned:
# SLA_URL (default: http://localhost:8080/sla-service)
# MP_URL  (default: http://localhost:8170/v1)
# DDA_URL (default: http://localhost:8175)
#
#
if [ "$0" != "bin/test-integration.sh" ]; then
        echo "Must be executed from project root"
        exit 1
fi

CREDENTIALS=user:password
SLA_URL=${SLA_URL:-http://localhost:8080/sla-service}
MP_URL=${MP_URL:-http://localhost:8170/v1}
DDA_URL=${DDA_URL:-http://localhost:8175}

#
# Setup MP.
#
curl -v -X POST -H"Content-type: application/json"  \
    -d@'src/test/resources/mic/deployment.json'     \
    $MP_URL/model/resources

curl -v -X POST -H"Content-type: application/xml" -H"Accept: text/plain" \
    -d@'src/test/resources/mic/rules.xml'                                \
    $MP_URL/monitoring-rules

#
# Generate and store agreement
#
AGREEMENT_ID=$(java -cp target/sla-mediator-jar-with-dependencies.jar eu.modaclouds.sla.mediator.Creator\
    -u "$CREDENTIALS"          \
    -d src/test/resources/mic  \
    --prefix mic               \
    --provider ATOS            \
    --consumer random-consumer \
    --url ${SLA_URL})

if [ "$?" != "0" ]; then 
    >&2 echo -e "\n\nError creating agreement"
    exit 1
fi
[ "$DEBUG" != "" ] && echo AGREEMENT_ID=\"$AGREEMENT_ID\"

#
# Start agreement enforcement
#
java -cp target/sla-mediator-jar-with-dependencies.jar eu.modaclouds.sla.mediator.Starter\
    -u "$CREDENTIALS"             \
    --metrics "${MP_URL}/metrics" \
    --sla "$SLA_URL"              \
    --id "$AGREEMENT_ID"

if [ "$?" != "0" ]; then 
    >&2 echo -e "\n\nError starting enforcement"
    exit 1
fi

sleep 5

#
# POST some metrics to ResponseTime stream. sla-core should receive and process
#
curl -v -X POST -H"Content-type: application/json" \
    -d@'src/test/resources/mic/data01.json'        \
    $DDA_URL/streams/http%3A%2F%2Fwww.modaclouds.eu%2Fstreams%2Fresponsetime

sleep 3

curl -v -X POST -H"Content-type: application/json" \
    -d@'src/test/resources/mic/data02.json'        \
    $DDA_URL/streams/http%3A%2F%2Fwww.modaclouds.eu%2Fstreams%2Fresponsetime

