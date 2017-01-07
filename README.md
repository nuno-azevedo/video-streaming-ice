# Video Streaming

<br>
### How to Build
```bash
$ make
```


<br>
### How to Run

##### 1° Icebox
```bash
$ make icebox
```
###### OR
```bash
$ mkdir -p db
$ icebox --Ice.Config=configs/config.icebox
```

##### 2° Portal
```bash
$ make run-portal
```
###### OR
```bash
$ java Portal ${PORT}
```

##### 3° Streaming Server
```bash
$ make run-streamer
```
###### OR
```bash
$ java Streamer ${PORT} ${VIDEO} ${NAME} ${ENDPOINT} ${RESOLUTION} ${BITRATE} ${KEYWORDS}
```

##### 4° Client
```bash
$ make run-client
```
###### OR
```bash
$ java Client ${PORT}
```

<br>
### Client Commands
- **list** : Shows the list of streams available
- **search** *‘[keywords]’* : Searches for streams that contains the keywords given by argument
- **play** *‘[name]’* : Plays the stream with the name given by argument 

<br>
### Clean build files
```bash
$ make clean
```

<br>
### Stop the running programs
```bash
$ make stop
```

<br>
### Free the endpoints
```bash
$ make free-addresses
```
