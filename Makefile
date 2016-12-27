JC = javac

SRC = src/java
ICE = src/slice
OUT = build

ICE_JAR := /usr/share/java/ice-3.6.3.jar
CLASSPATH := ${ICE_JAR}:${OUT}

SERVER = Server
CLIENT = Client

.SUFFIXES: .java .class

# .java.class:
	# mkdir -p ${OUT}
	# ${JC} -d ${OUT} -classpath ${CLASSPATH} $*.java

# CLASSES = ${SRC}/Server.java ${SRC}/Client.java ${SRC}/PrinterI.java

# classes: ${CLASSES:.java=.class}

default: build

build: server client


server: printer ${OUT}/Server.class

${OUT}/Server.class: ${SRC}/Server.java
	mkdir -p ${OUT}
	${JC} -d ${OUT} -classpath ${CLASSPATH} ${SRC}/Server.java


client: printer ${OUT}/Client.class

${OUT}/Client.class: ${SRC}/Client.java
	mkdir -p ${OUT}
	${JC} -d ${OUT} -classpath ${CLASSPATH} ${SRC}/Client.java


printer: ${OUT}/PrinterI.class

${OUT}/PrinterI.class: ${SRC}/PrinterI.java ${SRC}/Demo/Printer.java
	mkdir -p ${OUT}
	${JC} -d ${OUT} -classpath ${CLASSPATH} ${SRC}/PrinterI.java ${SRC}/Demo/*.java

${SRC}/Demo/Printer.java: ${ICE}/Printer.ice
	slice2java --output-dir ${SRC} ${ICE}/Printer.ice


run-server: server
	- @export CLASSPATH=${CLASSPATH}; java ${SERVER}

run-client: client
	- @export CLASSPATH=${CLASSPATH}; java ${CLIENT}


clean:
	rm -rf ${OUT} ${SRC}/Demo


stop:
	$(shell jps | grep 'Server\|Client' | cut -d" " -f1 | xargs kill -9)
