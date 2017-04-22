@ECHO OFF

SETLOCAL enabledelayedexpansion

SET VERSION=1.4

TITLE  Gossip Project [ Version %VERSION% ]
PROMPT $G

::===========================
:: BEGIN > Project Variables
::===========================

SET FILE_SEP=\
SET PATH_SEP=;

SET LIB_DIR=lib
SET RES_DIR=img
SET SRC_DIR=src
SET OUT_DIR=out

SET BASE_PKG=edu.cse4232.gossip

SET JARS= ddp2p-asn1\DD.jar sqllite-jdbc-3.14.2.1\sqlite-jdbc-3.14.2.1.jar forms_rt-142.1\forms_rt-142.1.jar

SET SRCS=sources.txt

SET LIBS=.!JARS: =%PATH_SEP%%LIB_DIR%%FILE_SEP%!
SET COMP_CP=%LIBS%%PATH_SEP%%RES_DIR%
SET EXEC_CP=%COMP_CP%%PATH_SEP%%OUT_DIR%

::=========================
:: END > Project Variables
::=========================

::====================
:: BEGIN > Menu Items
::====================

SET /a COUNTER=0

SET PROMPT_SYMBOL=" >> "

:menu
ECHO.
ECHO  1. Compile Project
ECHO  2. Start Server
ECHO  3. Start Peer Witness
ECHO  4. Start Client
ECHO  5. Exit
ECHO.
SET /p ANSWER=%PROMPT_SYMBOL%
ECHO.

IF "%ANSWER%" == "1" (
	ECHO  Compiling Source Files...
	CALL :cmd_compile 2>NUL
	ECHO  Finished Compilation...
	GOTO :menu
)

IF "%ANSWER%" == "2" (
	ECHO  Server Parameters
	ECHO  =================
	ECHO.
	SET /p PORT="[?] Port: "
	SET DB_FILE=server!COUNTER!.db
	SET /a COUNTER+=1
	SET /p DB_FILE="[?] Database File: "
	START "Server" "scripts\server.cmd" !PORT! !DB_FILE!
	GOTO :menu
)

IF "%ANSWER%" == "3" (
	ECHO  Peer Witness Parameters
	ECHO  =======================
	ECHO.
	SET /p HOST="[?] Host: "
	SET /p PORT="[?] Port: "
	START "Peer Witness" "scripts\witness.cmd" !HOST! !PORT!
	GOTO :menu
)

IF "%ANSWER%" == "4" (
	ECHO  Client Parameters
	ECHO  =================
	ECHO.
	SET /p HOST="[?] Host: "
	SET /p PORT="[?] Port: "
	SET /p PROTOCOL="[?] Protocol [TCP/UDP]: "
	SET /p GUI="[?] Gui [T/F]: "
	IF /i "!GUI!" == "t" (SET GUI=TRUE) ELSE (SET GUI=FALSE)
	START "Client" "scripts\client.cmd" !HOST! !PORT! !PROTOCOL! !GUI!
	GOTO :menu
)

IF "%ANSWER%" == "5" (
	ECHO  Exiting...
	GOTO :end
)

IF /i "%ANSWER%" == "cls" (
	CLS
	GOTO :menu
)

IF /i "%ANSWER%" == "exit" (
	ECHO  Exiting...
	GOTO :end
)

ECHO  Please Enter A Number In The Range [1, 5]
GOTO :menu

::==================
:: END > Menu Items
::==================

::===========================
:: BEGIN > Command Functions
::===========================

:cmd_compile
IF EXIST %OUT_DIR% RMDIR /s /q %OUT_DIR%
MKDIR %OUT_DIR%
CD %SRC_DIR%
DIR /s /B *.java > ..\%SRCS%
CD %~dp0
javac -cp %COMP_CP% -d %OUT_DIR% @%SRCS%
GOTO :eof

::=========================
:: END > Command Functions
::=========================

:end
ENDLOCAL
EXIT
