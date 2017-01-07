# Video Streaming

<br>
### How to Build
```bash
$ make
```


<br><br>
### How to Run

##### 1° Icebox
```bash
$ make icebox
```
<center>**OR**</center>
```bash
$ mkdir -p db
$ icebox --Ice.Config=configs/config.icebox
```

<br>
##### 2° Portal
```bash
$ make run-portal
```
<center>**OR**</center>
```bash
$ java Portal ${PORT}
```

<br>
##### 3° Streaming Server
```bash
$ make run-streamer
```
<center>**OR**</center>
```bash
$ java Streamer ${PORT} ${VIDEO} ${NAME} ${ENDPOINT} ${RESOLUTION} ${BITRATE} ${KEYWORDS}
```

<br>
##### 4° Client
```bash
$ make run-client
```
<center>**OR**</center>
```bash
$ java Client ${PORT}
```


<br><br>
### Client Commands
- **list** : Shows the list of streams available
- **search** *‘[keywords]’* : Searches for streams that contains the keywords given by argument
- **play** *‘[name]’* : Plays the stream with the name given by argument 


<br><br>
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
