import VideoStreaming.Notifier;

import IceStorm.AlreadySubscribed;
import IceStorm.BadQoS;
import IceStorm.InvalidSubscriber;
import IceStorm.NoSuchTopic;

import java.util.Map;

public class Subscriber extends Ice.Application {
    public static void main(String args[]) {
        Subscriber app = new Subscriber();
        int status = app.main("Subscriber", args, "configs/config.sub");
        System.exit(status);
    }

    @Override
    public int run(String args[]) {
        String name = "VideoStreaming";

//        Ice.Communicator ic = Ice.Util.initialize();
//        Ice.ObjectPrx obj = ic.stringToProxy("IceStorm/TopicManager: tcp -p 9999");
        Ice.ObjectPrx obj = communicator().propertyToProxy("TopicManager.Proxy");
        IceStorm.TopicManagerPrx manager = IceStorm.TopicManagerPrxHelper.checkedCast(obj);
        if (manager == null) {
            System.err.println("Invalid proxy");
            return 1;
        }

//        Ice.ObjectAdapter adapter = ic.createObjectAdapter("Notifier.Subscriber");
        Ice.ObjectAdapter adapter = communicator().createObjectAdapter("Notifier.Subscriber");
        Notifier notifier = new NotifierI();
        Ice.ObjectPrx proxy = adapter.addWithUUID(notifier).ice_oneway();
        adapter.activate();

        IceStorm.TopicPrx topic = null;
        try {
            topic = manager.retrieve(name);
            Map qos = null;
            topic.subscribeAndGetPublisher(qos, proxy);
        } catch (InvalidSubscriber | AlreadySubscribed | NoSuchTopic | BadQoS e) {
            e.printStackTrace();
        }
//        ic.waitForShutdown();
        communicator().waitForShutdown();
        topic.unsubscribe(proxy);

        return 0;
    }
}