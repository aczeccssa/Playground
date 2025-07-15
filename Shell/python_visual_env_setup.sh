#!/bin/bash
# Python Visual Environment creator
# Auth: Lester E
# Date: 2025-01-06

# Usage: python_visual_env_setup <separate_dir:default_current_folder>

# Confirm target dir
# Default target is current directory
target="./"

# Using first argument as target directory if exsit
if [ -e "$1" ]; then
    target=$1
fi

# Going to target directory
cd "$target"

# Everything is ready and going to create venv
echo "Ready to setup python visual Environment in $target"

# Create requirements file
touch requirements.txt

# Create venv
python3 -m venv venv

# Source reload venv binary
source venv/bin/activate

# Install requirements file
pip install -r requirements.txt

# Create .zshrc file for zsh shell
touch .zshrc

# Write auto load Environment script
echo "#!/bin/bash" >>.zshrc
echo "source ${PWD}/venv/bin/activate" >>.zshrc

# Finished
echo "Done."