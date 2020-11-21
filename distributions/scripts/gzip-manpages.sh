#!/bin/sh

rm -rf ./*.gz
find . -maxdepth 1 ! -name 'sourcehawk*' -type f -delete
gzip ./*.1