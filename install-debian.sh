#!/usr/bin/env bash

apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 379CE192D401AB61
echo "deb https://dl.bintray.com/optum/deb buster main" | sudo tee -a /etc/apt/sources.list
sudo apt install sourcehawk