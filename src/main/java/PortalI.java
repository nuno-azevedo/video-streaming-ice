import Streaming.Endpoint;
import Streaming.NotifierPrx;
import Streaming.NotifierPrxHelper;
import Streaming.Stream;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PortalI extends Streaming._PortalDisp {
    private Map<Stream, Long> Streams = new HashMap<Stream, Long>();
    private NotifierPrx Notifier = null;

    PortalI() {
        Ice.ObjectPrx obj = Ice.Application.communicator().propertyToProxy("TopicManager.Proxy");
        IceStorm.TopicManagerPrx manager = IceStorm.TopicManagerPrxHelper.checkedCast(obj);
        if (manager == null) {
            System.err.println("Invalid proxy");
            return;
        }

        IceStorm.TopicPrx topic = null;
        while (topic == null) {
            try {
                topic = manager.retrieve("StreamsNotifier");
            } catch (IceStorm.NoSuchTopic e) {
                try {
                    topic = manager.create("StreamsNotifier");
                } catch (IceStorm.TopicExists ex) {
                    System.err.println("Temporary failure");
                    return;
                }
            }
        }

        Ice.ObjectPrx publisher = topic.getPublisher().ice_oneway();
        Notifier = NotifierPrxHelper.uncheckedCast(publisher);

        new Timer(10000, (z) -> {
            List<Stream> outdated = new ArrayList<Stream>();
            for (Stream s : Streams.keySet())
                if (System.currentTimeMillis() - Streams.get(s) >= 10000)
                    outdated.add(s);
            for (Stream s : outdated)
                remove(s);
        }).start();
    }

    // Calls from Streaming Servers
    public boolean register(Stream stream, Ice.Current current) {
        for (Stream s : Streams.keySet()) {
            if (compare(stream, s)) {
                System.err.println("register: stream already exists");
                return false;
            }
        }
        Streams.put(stream, System.currentTimeMillis());
        Notifier.inform(String.format("[%s: %s]", "Stream is now available", stream.getName()));
        System.err.println("register: ‘" + stream.getName() + "’ stream successfully registered");
        return true;
    }

    public void remove(Stream stream, Ice.Current current) {
        for (Stream s : Streams.keySet()) {
            if (compare(stream, s)) {
                Streams.remove(s);
                Notifier.inform(String.format("[%s: %s]", "Stream was removed", stream.getName()));
                System.err.println("remove: ‘" + stream.getName() + "’ stream successfully removed");
                return;
            }
        }
        System.err.println("remove: ‘" + stream.getName() + "’ stream not found");
    }

    public void update(Stream stream, Ice.Current current) {
        for (Stream s : Streams.keySet()) {
            if (compare(stream, s)) {
                Streams.put(s, System.currentTimeMillis());
                return;
            }
        }
        System.err.println("update: ‘" + stream.getName() + "’ stream not found");
    }

    // Calls from Clients
    public Stream get(String stream, Ice.Current current) {
        for (Stream s : Streams.keySet())
            if (s.name.equals(stream)) return s;
        return null;
    }

    public List<Stream> getAll(Ice.Current current) {
        return Streams.keySet().stream().collect(Collectors.toList());
    }

    // Auxiliar Functions
    private boolean compare(Stream s1, Stream s2) {
        Endpoint e1 = s1.getEndpoint();
        Endpoint e2 = s2.getEndpoint();
        return e1.getTransport().equals(e2.getTransport()) && e1.getIp().equals(e2.getIp()) && e1.getPort() == e2.getPort();
    }
}
