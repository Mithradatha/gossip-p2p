#!/bin/bash

TCP="java -cp src TCPClient localhost 2345"

echo "Sending new peer..."
echo "PEER:Nemahs:PORT=2345:IP=192.168.0.1%" | $TCP
echo "Asking for peer list..."
echo "PEERS?" | $TCP
exit 0
