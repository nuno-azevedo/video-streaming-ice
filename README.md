# Video Streaming Facility

<br>
Implementation of a Video Streaming facility that consists in three components:
* The portal is responsible for the registration or deletion of streams. For clients it acts as a publisher to announce new or deleted streams.
* The streaming servers are responsible for streaming a video using [FFmpeg](https://ffmpeg.org/). They send to the portal some info about the stream they are going to play.
* The clients are an interface to play the available streams, they get the available streams from the portal and then can connect to multiple streaming servers and watch their streams at the same time.


<br><br>
## Dependencies
- [JDK 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/linux_jdk.html)
- [ZeroC Ice](https://doc.zeroc.com/display/Ice36/Using+the+Linux+Binary+Distributions)
- [FFmpeg](https://ffmpeg.org/download.html)


<br><br>
## How to Build
```bash
$ make build
```


<br><br>
## How to Run

##### 1° Set Classpath
```bash
$ export CLASSPATH=${ICE_JAR}:${ICE_STORM_JAR}:build
$ # Example: export CLASSPATH=/usr/share/java/ice.jar:/usr/share/java/icestorm.jar:build
```

##### 2° Icebox
```bash
$ mkdir -p db
$ icebox --Ice.Config=configs/config.icebox
```

##### 3° Portal
```bash
$ java Portal ${PORTAL_PORT}
$ # Example: java Portal 11000
```

##### 4° Streaming Server
```bash
$ java Streamer ${PORTAL_PORT} ${VIDEO} ${NAME} ${ENDPOINT} ${RESOLUTION} ${BITRATE} ${KEYWORDS}
$ # Example: java Streamer 11000 videos/TheLetter.mp4 TheLetter tcp://127.0.0.1:12000 480x360 400 "Movie, Letter, Film"
```

##### 5° Client
```bash
$ java Client ${PORTAL_PORT}
$ # Example: java Client 11000
```


<br><br>
## Clean Build Files
```bash
$ make clean
```


<br><br>
## Stop Running Executables
```bash
$ make stop
```


<br><br>
## Free Endpoints
```bash
$ make free-addresses
```


<br><br>
## Client Commands
- **list** : Shows the list of streams available
- **search** *‘{keywords}’* : Searches for streams that contains the keywords given by argument
- **play** *‘name’* : Plays the stream with the name given by argument 
