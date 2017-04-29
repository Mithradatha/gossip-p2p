#!/bin/bash

#===========================
# BEGIN > Project Variables
#===========================

FILE_SEP="/"
PATH_SEP=":"

LIB_DIR="lib"
RES_DIR="img"
OUT_DIR="out"

BASE_PKG="edu.cse4232.gossip"

JARS=" ddp2p-asn1/DD.jar sqllite-jdbc-3.14.2.1/sqlite-jdbc-3.14.2.1.jar forms_rt-142.1/forms_rt-142.1.jar"

LIBS=.${JARS// /$PATH_SEP$LIB_DIR$FILE_SEP}
EXEC_CP=$LIBS$PATH_SEP$RES_DIR$PATH_SEP$OUT_DIR

#=========================
# END > Project Variables
#=========================

#===========================
# BEGIN > Program Arguments
#===========================

HOST="localhost"
PORT="2345"

#=========================
# END > Program Arguments
#=========================

echo "Connecting To Server At $HOST On Port $PORT..."

# Start TCP:
java -cp %EXEC_CP% %BASE_PKG%.client.Client -s $HOST -p $PORT -T
