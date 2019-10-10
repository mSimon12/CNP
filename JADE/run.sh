#! /bin/bash

nWorkers=10
nClients=1
nCNPs=3

if [ $# -eq 3 ]; then
    nWorkers=$1
    nClients=$2
    nCNPs=$3
fi

java -cp ".:./jade-4.3.jar" StartJade $nWorkers $nClients $nCNPs