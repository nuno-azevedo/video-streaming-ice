SRC = src

SERVER = Server
SERVER_SRC = ${SRC}/Printer.cpp ${SRC}/Server.cpp

CLIENT = Client
CLIENT_SRC = ${SRC}/Printer.cpp ${SRC}/Client.cpp

PRINTER_SRC = ${SRC}/Printer.cpp ${SRC}/Printer.h

default: build

build: printer server client

clean:
	rm -rf ${SERVER} ${CLIENT} ${PRINTER_SRC}

server: printer
	c++ -I./${SRC} ${SERVER_SRC} -o ${SERVER} -lIce -lIceUtil -lpthread

client: printer
	c++ -I./${SRC} ${CLIENT_SRC} -o ${CLIENT} -lIce -lIceUtil -lpthread

printer:
	slice2cpp ${SRC}/Printer.ice --output-dir ${SRC}

run-server: server
	./${SERVER}

run-client: client
	./${CLIENT}

stop:
	- pkill ${SERVER}
	- pkill ${CLIENT}
