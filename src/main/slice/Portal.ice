module Streaming {
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
        bool register(Stream stream);
        void remove(Stream stream);
        void update(Stream stream);
        Stream get(string stream);
        Streams getAll();
    };

    interface Notifier {
        void inform(string stream);
    };
};
