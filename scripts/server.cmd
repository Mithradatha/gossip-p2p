@ECHO OFF
TITLE Server
ECHO Starting Server On Port %1...
java -cp %EXEC_CP% %BASE_PKG%.server.Server -p %1 -d %2
EXIT