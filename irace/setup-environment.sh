#!/bin/bash
set -euo pipefail

if ! (grep -qi "ID_LIKE=.*debian" /etc/os-release || grep -qi "^ID=debian" /etc/os-release); then
    echo "This script is intended to run on a Debian-based distro."
    exit 1
fi

python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt

sudo apt update
sudo apt install r-base -y

Rscript -e 'if (!require("irace")) install.packages("irace", repos="https://cloud.r-project.org/")'
