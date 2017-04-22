@ECHO OFF
TITLE %3 Client
ECHO Connecting To Server At %1 On Port %2...
IF /i "%3" == "tcp" (SET PROTOCOL=T) ELSE (SET PROTOCOL=U)
IF /i "%4" == "true" (SET GUI=I) ELSE (SET GUI="")
java -cp %EXEC_CP% %BASE_PKG%.client.Client -s %1 -p %2 -%PROTOCOL% -%GUI%
EXIT