#! /bin/bash

nWorkers=10
nClients=1
nCNPs=3

if [ $# -eq 3 ]; then
    nWorkers=$1
    nClients=$2
    nCNPs=$3
fi

echo "nCNPs($nCNPs)." > settings.asl
echo -en "MAS contracts {\n\tinfrastructure: Centralised\n\n\tagents:\n\t\tworker #$nWorkers;\n\t\tclient #$nClients;\n\t\tseverino;\n}" > contracts.mas2j