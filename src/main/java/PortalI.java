import Streaming.NotifierPrxHelper;
import Streaming.Stream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PortalI extends Streaming._PortalDisp {
    private Map<Stream, Long> Streams = new HashMap<Stream, Long>();
    Streaming.NotifierPrx Notifier = null;

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
                topic = manager.retrieve("Streams");
            } catch (IceStorm.NoSuchTopic e) {
                try {
                    topic = manager.create("Streams");
                } catch (IceStorm.TopicExists ex) {
                    System.err.println("Temporary failure");
                    return;
                }
            }
        }

        Ice.ObjectPrx publisher = topic.getPublisher().ice_oneway();
        Notifier = NotifierPrxHelper.uncheckedCast(publisher);

        new Thread(() -> {
            while (true) {
                for (Stream s : Streams.keySet())
                    if (System.currentTimeMillis() - Streams.get(s) >= 60000)
                        Streams.remove(s);
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Calls from Streaming Servers
    public void register(Stream stream, Ice.Current current) {
        Streams.put(stream, System.currentTimeMillis());
        Notifier.inform(String.format("[%s: %s]", "New Stream", stream.getName()));
    }

    public void remove(Stream stream, Ice.Current current) {
        Streams.remove(stream);
        Notifier.inform(String.format("[%s: %s]", "Removed Stream", stream.getName()));
    }

    public void update(Stream stream, Ice.Current current) {
        if (Streams.get(stream) != null)
            Streams.put(stream, System.currentTimeMillis());
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
}
