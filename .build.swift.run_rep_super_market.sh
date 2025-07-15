#!/bin/bash

GREEN='\033[92m'
BLUE='\033[94m'
RESET='\033[0m'

output_pathname=.build/swift/out
input_pathname=Swift/RepresentableHypermarket.swift
output_filename=RepresentableHypermarket
valification_filename=~/Documents/.repSupermarketSavedAliceData.json

rm -rf $output_pathname
mkdir -p $output_pathname
/usr/bin/swiftc $input_pathname -o $output_pathname/$output_filename
cd $output_pathname

start_time=$(date +%s)
echo -e "${BLUE}[Executon] $(date '+%Y-%m-%d %H:%M:%S') Proccessing application $PWD/${target}${RESET}"

run_times=2
if [ -f $valification_filename ]; then
    run_times=1
fi

for ((i=1; i<=$run_times; i++))
do
    ./$output_filename
done

end_time=$(date +%s)
cost_time=$[ $end_time-$start_time ]
echo -e "${GREEN}[Executon] $(date '+%Y-%m-%d %H:%M:%S') Process finished in ${cost_time}ms${RESET}"