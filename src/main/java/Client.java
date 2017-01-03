import IceStorm.AlreadySubscribed;
import IceStorm.BadQoS;
import IceStorm.InvalidSubscriber;
import IceStorm.NoSuchTopic;
import Streaming.Notifier;
import Streaming.PortalPrx;
import Streaming.PortalPrxHelper;
import Streaming.Stream;

import java.util.Scanner;

public class Client extends Ice.Application {
    public static PortalPrx Portal = null;

    public static void main(String args[]) {
        Client app = new Client();
        int status = app.main("Subscriber", args, "configs/config.sub");
        System.exit(status);
    }

    @Override
    public int run(String args[]) {
        int status = 0;
        Ice.Communicator ic = null;
        try {
            ic = Ice.Util.initialize(args);
            Ice.ObjectPrx base = ic.stringToProxy("Portal: default -p 11000");

            Portal = PortalPrxHelper.checkedCast(base);
            if (Portal == null) throw new Error("Invalid proxy");

            Ice.ObjectPrx obj = communicator().propertyToProxy("TopicManager.Proxy");
            IceStorm.TopicManagerPrx manager = IceStorm.TopicManagerPrxHelper.checkedCast(obj);
            if (manager == null) {
                System.err.println("Invalid proxy");
                return 1;
            }

            Ice.ObjectAdapter adapter = communicator().createObjectAdapter("Notifier.Subscriber");
            Notifier notifier = new NotifierI();
            Ice.ObjectPrx proxy = adapter.addWithUUID(notifier).ice_oneway();
            adapter.activate();

            IceStorm.TopicPrx topic = null;
            try {
                topic = manager.retrieve("Streams");
                topic.subscribeAndGetPublisher(null, proxy);
            } catch (InvalidSubscriber | AlreadySubscribed | NoSuchTopic | BadQoS e) {
                e.printStackTrace();
            }
            new Thread(() -> {
                communicator().waitForShutdown();
            }).start();

            Scanner scan = new Scanner(System.in);
            String cmd = new String();
            while(!cmd.equals("exit")) {
                System.out.print("stream: ");
                cmd = scan.nextLine();
                parser(cmd);
            }

            topic.unsubscribe(proxy);
        } catch (Ice.LocalException e) {
            e.printStackTrace();
            status = 1;
        } catch (Exception e) {
            System.err.println(e.toString());
            status = 1;
        }
        if (ic != null) {
            try {
                ic.destroy();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                status = 1;
            }
        }
        return status;
    }

    private static void parser(String cmd) {
        String cmd_list[] = cmd.split(" +");
        if (cmd_list.length == 0 || cmd_list[0].equals("")) {
            return;
        }
        else if(cmd_list[0].equals("list")) {
            if (cmd_list.length == 1) list();
            else System.err.printf("%s %s: %s\n", cmd_list[0], cmd_list[1], "too many arguments");
        }
        else if(cmd_list[0].equals("search")) {
            if (cmd_list.length == 1)  System.err.printf("%s: %s\n", cmd_list[0], "missing arguments");
            else if (cmd_list.length == 2) search(cmd_list[1]);
            else System.err.printf("%s: %s\n", cmd_list[0], "too many arguments");
        }
        else if(cmd_list[0].equals("play")) {
            if (cmd_list.length == 1)  System.err.printf("%s: %s\n", cmd_list[0], "missing arguments");
            else if (cmd_list.length == 2) play(cmd_list[1]);
            else System.err.printf("%s: %s\n", cmd_list[0], "too many arguments");
        }
        else System.err.printf("%s: %s\n", cmd_list[0], "command not found");
    }

    private static void list() {
        System.out.printf(
            "%30s   |   %27s   |   %12s   |   %9s   |   %40s\n",
            "[Name]", "[Endpoint]", "[Resolution]", "[Bitrate]", "[Keywords]"
        );
        for (Stream s : Portal.getAll()) {
            System.out.printf(
                "%30s   |   %27s   |   %12s   |   %9s   |   %40s\n",
                s.getName(),
                s.getEndpoint().getTransport() + "://" + s.getEndpoint().getIp() + ":" + s.getEndpoint().getPort(),
                s.getResolution().getWidth() + "x" + s.getResolution().getHeight(),
                s.getBitrate(),
                "[" + String.join(", ", s.getKeywords()) + "]"
            );
        }
    }

    private static void search(String keywords) {

    }

    private static void play(String name) {

    }
}
