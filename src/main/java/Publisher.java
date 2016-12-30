import VideoStreaming.NotifierPrx;
import VideoStreaming.NotifierPrxHelper;

public class Publisher extends Ice.Application {
    public static void main(String args[]) {
        Publisher app = new Publisher();
        int status = app.main("Publisher", args, "config.pub");
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

        IceStorm.TopicPrx topic = null;
        while (topic == null) {
            try {
                topic = manager.retrieve(name);
            } catch (IceStorm.NoSuchTopic e) {
                try {
                    topic = manager.create(name);
                } catch (IceStorm.TopicExists ex) {
                    System.err.println("Temporary failure");
                    return 1;
                }
            }
        }

        Ice.ObjectPrx publisher = topic.getPublisher().ice_oneway();
        NotifierPrx notifier = NotifierPrxHelper.uncheckedCast(publisher);
        try {
            while (true) {
                notifier.inform("NEW STREAM !");
                try {
                    Thread.currentThread();
                    Thread.sleep(3000);
                } catch (InterruptedException e) { }
            }
        } catch (Ice.CommunicatorDestroyedException ex) { }

        return 0;
    }
}