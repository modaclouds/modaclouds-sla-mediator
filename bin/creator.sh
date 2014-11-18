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
# Assume sla-core is running at localhost in a clear state.
#
# The following ENV vars may be previously assigned:
# SLA_URL (default: http://localhost:8080/sla-service)
# CREDENTIALS (default: user:password)
#
#
if [ "$0" != "bin/creator.sh" ]; then
        echo "Must be executed from project root"
        exit 1
fi

if [ $# -lt 2 ]; then
        echo "Usage: $0 <directory> [--prefix <prefix>] [--rules <filename>] [--constraints <filename>]"
        exit 1
fi

CREDENTIALS=${CREDENTIALS:-user:password}
SLA_URL=${SLA_URL:-http://localhost:8080/sla-service}

DIR=$1
shift

#
# Generate and store agreement
#
AGREEMENT_ID=$(java -cp target/sla-mediator-jar-with-dependencies.jar eu.modaclouds.sla.mediator.Creator\
    -u "$CREDENTIALS"          \
    --provider ATOS            \
    --consumer random-consumer \
    -d $DIR                    \
    --url ${SLA_URL}           \
    $*)

if [ "$?" != "0" ]; then 
    >&2 echo -e "\n\nError creating agreement"
    exit 1
fi
echo AGREEMENT_ID=\"$AGREEMENT_ID\"

