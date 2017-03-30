#!/bin/bash

TCP="java -cp src TCPClient localhost 2345"
UDP="java -cp src UDPClient localhost 2345"
TESTPEER="PEER:Nemahs:Port=2345:IP=192.168.0.1%"


echo "Sending new peer..."
echo $TESTPEER | $TCP
echo "Asking for peer list..."
echo "PEERS?" | $TCP

echo "Trying UDP now..."
echo "Sending new peer..."
echo $TESTPEER | $UDP
echo "Asking for peer list..."
echo "PEERS?" | $UDP

exit 0
