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

######## STREAM EXAMPLE ########
PORT = 11000
VIDEO = videos/PopeyeAliBaba.mp4
NAME = PopeyeAliBaba
ENDPOINT = tcp://127.0.0.1:12000
RESOLUTION = 480x270
BITRATE = 400
KEYWORDS = "Kids, Popeye"
################################


.SUFFIXES: .java .class


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
	@export CLASSPATH=${CLASSPATH}; java ${PORTAL} ${PORT}

run-streamer: streamer
	@export CLASSPATH=${CLASSPATH}; java ${STREAMER} ${PORT} ${VIDEO} ${NAME} ${ENDPOINT} ${RESOLUTION} ${BITRATE} ${KEYWORDS}

run-client: client
	@export CLASSPATH=${CLASSPATH}; java ${CLIENT} ${PORT}


clean:
	rm -rf ${OUT} ${SRC}/Streaming db


stop:
	$(shell jps | grep '${PORTAL}\|${STREAMER}\|${CLIENT}' | awk '{ print $$1 }' | xargs kill -9)


free-addresses:
	$(shell lsof -i:10000 -i:11000 -i:12000 -i:12001 | tail -n +2 | awk '{ print $$2 }' | xargs kill -9)
