# Video Streaming to browser

Direct Access to the stream http://0.0.0.0:8180

<img src="doc/origamiweb0.gif"/>

Using the Sample React Client

<img src="doc/origamiweb.gif"/>

* Top Left: MJpeg encoded Video Stream sent by the server
* Top Right: List of Filters read from a file on the server
* Bottom Left: FPS Filter requested  from the client
* Bottom Right: Yolo detection requested from the client

Starts the streaming server

```
 mvn compile exec:java
```

Starts the react client

```
cd sample-react-client
npm i 
npm run start
```

