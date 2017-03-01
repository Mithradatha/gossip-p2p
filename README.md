Due Date: March 5 


Instructions:
Program an iterative server that stores locally (in some files or in a sqlite database) a set of peers and gossip text messages that were already seen by this machine. 
Each of these messages should be stored together with their SHA-256 digest value encoded into ascii using base64. 
The server should recognize commands both over TCP and UDP. 
The 3 types of accepted messages (that can be send from sock or ncat) are:

1) "GOSSIP:mBHL7IKilvdcOFKR03ASvBNX//ypQkTRUvilYmB1/OY=:2017-01-09-16-18-20-001Z:Tom eats Jerry%"

Telling the message "Tom eat Jerry" generated at 01/09/2017 at 16h, minute 18, second 20, and millisecond 001 UTC. 
Note that ":" is used for separating fields and a command is terminated with the null character, "%". 
The SHA-256 of the message string "2017-01-09-16-18-20-001Z:Tom eats Jerry" is encoded in Sha-256 base 64 as "mBHL7IKilvdcOFKR03ASvBNX//ypQkTRUvilYmB1/OY=", obtained here with: echo -n "2017-01-09-16-18-20-001Z:Tom eats Jerry" | sha256sum -b | cut -f 1 -d ' '| xxd -r -p | uuencode -m -
Upon receiving this message the server should check if it already new it and simply discard it if it was known, while printing "DISCARDED" on the standard error. 
Otherwise store it, broadcast it to all known peers, and print it on the standard error.

2) "PEER:John:PORT=2356:IP=163.118.239.68%"

To tell the server about a peer called "John" with IP=163.118.239.68 at port 2356. 
The server is expected to store the name and address of this peer if it is not yet known, or to update its address.

3) "PEERS?\n"

Is used to query for the list of peers at this address. 
The expected answer has the format: 
"PEERS|2|John:PORT=2356:IP=163.118.239.68|Mary:PORT=2355:IP=163.118.237.60|%"
Telling that this peer knows 2 peers, John and Mary, with the corresponding addresses given.


Submission:
The server should compile either with the command make or "compile.sh". 
It should be launched with a script called as "run.sh 2356" that starts the server to listen on TCP and UDP ports 2356.
You are penalized if any path of some file or database is hardcoded in java or C (they can be hardcoded in the script used forexecution).
In case you store data in a local file, the file should be textual and you must submit a documentation with the format of the file.
The C and Java code have to process command line arguments using the simple version of the standard UNIX getopt function (or the provided Java equivalent GetOpt class). 
The command line arguments must support the option "-p 23012"to specify the port on which the server listens, and the options "-d /data/file" to specify the database or folder for files containing thelocally stored data.
Provide files script1.sh to test commands. 
Provide a report according to the above requirements. 
In case you use custom files instead of sqlite, you need to provide a detailed documentation for their format in your report. 
You should let your server running in your account on code01.fit.edu and post its port on canvas.
