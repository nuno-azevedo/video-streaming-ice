JC = javac

SRC = src/main/java
ICE = src/main/slice
OUT = build

ICE_JAR := /usr/share/java/ice.jar
ICE_STORM_JAR := /usr/share/java/icestorm.jar
CLASSPATH := ${ICE_JAR}:${ICE_STORM_JAR}:${OUT}

PORTAL = Portal
STREAMER = Streamer
CLIENT = Client

.SUFFIXES: .java .class

# .java.class:
	# mkdir -p ${OUT}
	# ${JC} -d ${OUT} -classpath ${CLASSPATH} $*.java

# CLASSES = ${SRC}/Streamer.java ${SRC}/Client.java ${SRC}/PrinterI.java

# classes: ${CLASSES:.java=.class}

default: build

build: portal streamer client


portal: ${OUT}/PortalI.class ${OUT}/Portal.class

${OUT}/PortalI.class: ${SRC}/PortalI.java ${ICE}/Portal.ice
	mkdir -p ${OUT}
	slice2java --output-dir ${SRC} ${ICE}/Portal.ice
	${JC} -d ${OUT} -classpath ${CLASSPATH} ${SRC}/PortalI.java ${SRC}/Streaming/*.java

${OUT}/Portal.class: ${SRC}/Portal.java
	mkdir -p ${OUT}
	${JC} -d ${OUT} -classpath ${CLASSPATH} ${SRC}/Portal.java


streamer: ${OUT}/PortalI.class ${OUT}/Streamer.class

${OUT}/Streamer.class: ${SRC}/Streamer.java
	mkdir -p ${OUT}
	${JC} -d ${OUT} -classpath ${CLASSPATH} ${SRC}/Streamer.java


client: ${OUT}/NotifierI.class ${OUT}/Client.class

${OUT}/NotifierI.class: ${SRC}/NotifierI.java ${ICE}/Portal.ice
	mkdir -p ${OUT}
	slice2java --output-dir ${SRC} ${ICE}/Portal.ice
	${JC} -d ${OUT} -classpath ${CLASSPATH} ${SRC}/NotifierI.java ${SRC}/Streaming/*.java

${OUT}/Client.class: ${SRC}/Client.java
	mkdir -p ${OUT}
	${JC} -d ${OUT} -classpath ${CLASSPATH} ${SRC}/Client.java


icebox:
	mkdir -p db
	icebox --Ice.Config=configs/config.icebox


run-portal: portal
	- @export CLASSPATH=${CLASSPATH}; java ${PORTAL}

run-streamer: streamer
	- @export CLASSPATH=${CLASSPATH}; java ${STREAMER}

run-client: client
	- @export CLASSPATH=${CLASSPATH}; java ${CLIENT}


clean:
	rm -rf ${OUT} ${SRC}/Streaming db


stop:
	$(shell jps | grep 'Portal\|Streamer\|Client' | cut -d" " -f1 | xargs kill -9)


free-addresses:
	$(shell lsof -i:10000 -i:11000 | tail -n +2 | awk '{print $$2}' | xargs kill -9)
