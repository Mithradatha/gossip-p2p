#!/bin/bash

if type -p java; then
	javac src/*.java
	exit 0
else
	echo "Java not installed, please install Java."
	exit 1
fi
