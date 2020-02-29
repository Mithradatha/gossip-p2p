Overview
======
Program an iterative server that stores locally (in some files or in a sqlite database) a set of peers and gossip text messages that were already seen by this machine. 
Each of these messages should be stored together with their SHA-256 digest value encoded into ascii using base64. 
The server should recognize commands both over TCP and UDP. 
The 3 types of accepted messages (that can be send from sock or ncat) are:

`GOSSIP:mBHL7IKilvdcOFKR03ASvBNX//ypQkTRUvilYmB1/OY=:2017-01-09-16-18-20-001Z:Tom eats Jerry%`

Telling the message "Tom eat Jerry" generated at 01/09/2017 at 16h, minute 18, second 20, and millisecond 001 UTC. 
Note that ":" is used for separating fields and a command is terminated with the null character, "%". 
The SHA-256 of the message string "2017-01-09-16-18-20-001Z:Tom eats Jerry" is encoded in Sha-256 base 64 as "mBHL7IKilvdcOFKR03ASvBNX//ypQkTRUvilYmB1/OY=", obtained here with: `echo -n "2017-01-09-16-18-20-001Z:Tom eats Jerry" | sha256sum -b | cut -f 1 -d ' '| xxd -r -p | uuencode -m` -
Upon receiving this message the server should check if it already new it and simply discard it if it was known, while printing "DISCARDED" on the standard error. 
Otherwise store it, broadcast it to all known peers, and print it on the standard error.

`PEER:John:PORT=2356:IP=163.118.239.68%`

To tell the server about a peer called "John" with IP=163.118.239.68 at port 2356. 
The server is expected to store the name and address of this peer if it is not yet known, or to update its address.

`PEERS?\n`

Is used to query for the list of peers at this address. 
The expected answer has the format: 
`PEERS|2|John:PORT=2356:IP=163.118.239.68|Mary:PORT=2355:IP=163.118.237.60|%`
Telling that this peer knows 2 peers, John and Mary, with the corresponding addresses given.


Windows Compilation
======

`javac -classpath ".;.\libraries\sqllite-jdbc-3.14.2.1\sqlite-jdbc-3.14.2.1.jar" .\src\*.java`

Windows Execution
======

ServerWindow> `java -classpath ".;.\libraries\sqllite-jdbc-3.14.2.1\sqlite-jdbc-3.14.2.1.jar;.\src" Server -p 2345 -d gossip.db`

TCPClientWindow> `java -classpath ".;.\src" TCPClient localhost 2345`

UDPClientWindow> `java -classpath ".;.\src" UDPClient localhost 2345`
