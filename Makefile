JC = javac

SRC = src/java
ICE = src/slice
OUT = build

ICE_JAR := /usr/share/java/ice-3.6.3.jar
CLASSPATH := ${ICE_JAR}:${OUT}

PORTAL = Portal
SERVER = Server
CLIENT = Client

.SUFFIXES: .java .class

# .java.class:
	# mkdir -p ${OUT}
	# ${JC} -d ${OUT} -classpath ${CLASSPATH} $*.java

# CLASSES = ${SRC}/Server.java ${SRC}/Client.java ${SRC}/PrinterI.java

# classes: ${CLASSES:.java=.class}

default: build

build: portal server client


portal: ${OUT}/PortalI.class ${OUT}/Portal.class

${OUT}/PortalI.class: ${SRC}/PortalI.java ${ICE}/Portal.ice
	mkdir -p ${OUT}
	slice2java --output-dir ${SRC} ${ICE}/Portal.ice
	${JC} -d ${OUT} -classpath ${CLASSPATH} ${SRC}/PortalI.java ${SRC}/Demo/*.java

${OUT}/Portal.class: ${SRC}/Portal.java
	mkdir -p ${OUT}
	${JC} -d ${OUT} -classpath ${CLASSPATH} ${SRC}/Portal.java


server: ${OUT}/ServerI.class ${OUT}/Server.class

${OUT}/ServerI.class: ${SRC}/ServerI.java ${ICE}/Server.ice
	mkdir -p ${OUT}
	slice2java --output-dir ${SRC} ${ICE}/Server.ice
	${JC} -d ${OUT} -classpath ${CLASSPATH} ${SRC}/ServerI.java ${SRC}/Demo/*.java

${OUT}/Server.class: ${SRC}/Server.java
	mkdir -p ${OUT}
	${JC} -d ${OUT} -classpath ${CLASSPATH} ${SRC}/Server.java


client: ${OUT}/PortalI.class ${OUT}/Client.class

${OUT}/Client.class: ${SRC}/Client.java
	mkdir -p ${OUT}
	${JC} -d ${OUT} -classpath ${CLASSPATH} ${SRC}/Client.java


run-portal: portal
	- @export CLASSPATH=${CLASSPATH}; java ${PORTAL}

run-server: server
	- @export CLASSPATH=${CLASSPATH}; java ${SERVER}

run-client: client
	- @export CLASSPATH=${CLASSPATH}; java ${CLIENT}


clean:
	rm -rf ${OUT} ${SRC}/Demo


stop:
	$(shell jps | grep 'Server\|Client' | cut -d" " -f1 | xargs kill -9)
