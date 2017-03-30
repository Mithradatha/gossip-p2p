#!/bin/bash

if [ -n "$1" ]; then
	java -cp src/:libraries/sqllite-jdbc-3.14.2.1/*  Server -p "$1"
	exit 0
else
	java -cp src/:libraries/sqllite-jdbc-3.14.2.1/*  Server
	exit 0
fi
	
