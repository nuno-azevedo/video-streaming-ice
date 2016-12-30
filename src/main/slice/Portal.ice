module VideoStreaming {
    class Endpoint {
        ["java:getset"] string transport;
        ["java:getset"] string ip;
        ["java:getset"] int port;
    };

    class Resolution {
        ["java:getset"] int width;
        ["java:getset"] int height;
    };

    sequence<string> Keywords;

    class Stream {
        ["java:getset"] string name;
        ["java:getset"] Endpoint endpoint;
        ["java:getset"] Resolution resolution;
        ["java:getset"] int bitrate;
        ["java:getset"] Keywords keywords;
    };

    ["java:type:java.util.LinkedList<Stream>"]
    sequence<Stream> Streams;

    interface Portal {
        void register(Stream stream);
        void remove(Stream stream);
        Streams getStreams();
    };

    interface Notifier {
        void inform(string stream);
    };
};
