# YTSubscriber

YTSubscriber is a JavaFX application allowing the user to follow YouTube channels from a local application.


## Requirements

+ [Java](http://www.java.com/en/download/)
+ [Streamlink](https://streamlink.github.io/) only for watching the videos on your video player


## Installation

+ Download the project or only the YTSubscriber-x.x.jar file under the *release* directory
+ Run YTSubscriber-x.x.jar 


## Usage

+ Add channels one by one with its identifier, or a list of channels by importing OPML files
+ Check for new videos available
+ Watch videos on YouTube or directly on your video player
+ Keep track of your watched video


## Configuration

```
# Your YouTube API key to read the videos duration
apiKey=

# Media player you want to use to play the videos
mediaPlayer=
```


## Screenshots

![](/screenshots/ytsubscriber.png)


## Used libraries

+ [Gson](https://github.com/google/gson)
+ [OkHttp](https://github.com/square/okhttp/)
+ [log4j](https://logging.apache.org/log4j/2.x/)
+ [prettytime](https://github.com/ocpsoft/prettytime)
+ [Gradle Shadow](https://github.com/johnrengelman/shadow)
+ [SQLite JDBC Driver](https://github.com/xerial/sqlite-jdbc)


## License

Project licensed under the [MIT license](http://opensource.org/licenses/mit-license.php).
