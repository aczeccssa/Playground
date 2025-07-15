#!/bin/bash
# Node Typescript Compiler with Runner V1.0
# Auth: Lester E
# Date: 2024-12-26

# Define colors:
RED='\033[91m'
GREEN='\033[92m'
YELLOW='\033[93m'
BLUE='\033[94m'
PURPLE='\033[95m'
RESET='\033[0m'

# Modules:
function compile_source() {
    # Block variable assignments...
    local source=$1
    local output=$2
    local es=$3

    # Logics...
    if [ -e "$source" ]; then
        echo -e "Compile typeScript source file ${PURPLE}${source} ==> ${output}${RESET}"

        # Execute the compilation command and capture its exit status
        tsc_compile_res=$(npx tsc --target "$es" "$source" --outFile "$output")
        echo -e "$tsc_compile_res"
        local compile_status=$?

        if [ $compile_status -eq 0 ]; then
            echo -e "${GREEN}Compile successfully ==> ${PURPLE}${output}${RESET}"
            return 0
        else
            echo -e "${RED}Compile failed ==> ${PURPLE}$compile_status${RESET}"
            return 1
        fi
    else
        echo -e "${RED}Source file does not exist ==> ${PURPLE}${source}${RESET}"
        return 1
    fi
}

function run_target() {
    # Block variable assignments...
    local target=$1

    # Logics...
    if [ -e "$target" ]; then
        echo -e "${PURPLE}Ready run output file ==> ${PURPLE}${target}${RESET}"
        node "${target}"
    else
        echo -e "${RED}Output file dose not exist ==> ${PURPLE}${target}${RESET}"
        exit 3
    fi
}

function single_run_javascript_job() {
    target=$1

    if [ -z "$target" ] || [ "$target" = "0" ]; then
        echo -e "${RED}Error: No js run file provided${RESET}"
        echo -e "${RED}    $0 -c <run_target>${RESET}"
        exit 4
    fi
    echo -e "${BLUE}Part 1: Run target file ${target}${RESET}"
    run_target "$target"
    exit 0
}

function compile_run_typescript_job() {
    input_source=$1
    output_target=$2
    es_module=$3

    if [ -z "$output_target" ] || [ "$output_target" = "0" ]; then
        output_target=$(echo "$input_source" | sed 's/\.ts$/.js/')
    fi
    if [ -z "$es_module" ] || [ "$es_module" = "0" ]; then
        es_module="es6"
    fi

    echo -e "${BLUE}Part 1: Validate finished and compile source file${RESET}"
    if ! compile_source $input_source $output_target $es_module; then
        exit 2
    fi
    echo -e "${BLUE}Part 2: Compilation finished and run compiled output file${RESET}"
    run_target "$output_target"
    exit 0
}

function help_info() {
    echo -e "${PURPLE}$0${RESET} <${GREEN}input_source${RESET}> [${YELLOW}optional::output_target${RESET}] [${YELLOW}optional::es_version${RESET}]"
    echo -e "${GREEN}    Example: index.ts ./output/index.js es6${RESET}"
    exit 0train
}

# Main Application
if [ $# -eq 0 ]; then
    echo -e "${RED}Error: No input source file provided${RESET}"
    echo -e "${RED}    $0 <input_source> [output_target] [es_version]${RESET}"
    exit 1
fi

input_source=$1
output_target=$2
es_module=$3

# Application help information
if [ $1 = "-h" ]; then
    help_info
fi

if [ $1 = "-r" ]; then
    single_run_javascript_job "$output_target"
fi

compile_run_typescript_job "$input_source" "$output_target" "$es_module"