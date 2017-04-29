#!/bin/bash

#JAVAC="/mnt/c/Program Files/Java/jdk1.8.0_131/bin/javac.exe"
JAVAC="javac"

#===========================
# BEGIN > Project Variables
#===========================

FILE_SEP="/"
PATH_SEP=":"

LIB_DIR="lib"
RES_DIR="img"
SRC_DIR="src"
OUT_DIR="out"

BASE_PKG="edu.cse4232.gossip"

JARS=" ddp2p-asn1/DD.jar sqllite-jdbc-3.14.2.1/sqlite-jdbc-3.14.2.1.jar forms_rt-142.1/forms_rt-142.1.jar"

SRCS="sources.txt"

LIBS=.${JARS// /$PATH_SEP$LIB_DIR$FILE_SEP}
COMP_CP=$LIBS$PATH_SEP$RES_DIR
EXEC_CP=$COMP_CP$PATH_SEP$OUT_DIR

#LIBS=$LIBS_DIR\*
#COMP_CP=$RES_DIR$PATH_SEP$LIBS
#EXEC_CP=$OUT_DIR$PATH_SEP$COMP_CP

#=========================
# END > Project Variables
#=========================

if [ -d $OUT_DIR ] 
	then rm -rf $OUT_DIR && echo "Removing"
fi

mkdir $OUT_DIR && echo "Making"

echo $LIBS
echo $COMP_CP
echo $EXEC_CP

find $SRC_DIR -name "*.java" > $SRCS

cat $SRCS

echo "$JAVAC" -cp "$COMP_CP" -d $OUT_DIR @$SRCS

"$JAVAC" -cp "$COMP_CP" -d $OUT_DIR @$SRCS

exit

