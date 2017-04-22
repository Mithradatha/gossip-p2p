@ECHO OFF
TITLE Peer Witness
ECHO Notifying Server At %1 On Port %2 Of New Peer...
java -cp %EXEC_CP% %BASE_PKG%.server.PeerWitness -h %1 -p %2
EXIT