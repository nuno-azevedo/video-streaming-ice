import IceStorm.AlreadySubscribed;
import IceStorm.BadQoS;
import IceStorm.InvalidSubscriber;
import IceStorm.NoSuchTopic;
import Streaming.Notifier;
import Streaming.PortalPrx;
import Streaming.PortalPrxHelper;
import Streaming.Stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Client extends Ice.Application {
    public static PortalPrx Portal = null;

    public static void main(String args[]) {
        if (args.length != 1) {
            System.err.println("USAGE: java Client $PORT");
            System.exit(1);
        }

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
            Ice.ObjectPrx base = ic.stringToProxy("Portal: default -p " + args[0]);

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
                topic = manager.retrieve("StreamsNotifier");
                topic.subscribeAndGetPublisher(null, proxy);
            } catch (InvalidSubscriber | AlreadySubscribed | NoSuchTopic | BadQoS e) {
                e.printStackTrace();
            }

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
        } finally {
            if (ic != null) {
                try {
                    ic.destroy();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    status = 1;
                }
            }
        }
        return status;
    }

    private static void parser(String cmd) {
        List <String> cmds = new ArrayList<String>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(cmd);
        while (m.find()) cmds.add(m.group(1).replaceAll(" *\" *", ""));

        int args = 0;
        if (cmds.size() == 0) {
            return;
        }
        else if(cmds.get(0).equals("list")) {
            if (cmds.size() == 1) list();
            else args = cmds.size() - 1;
        }
        else if(cmds.get(0).equals("search")) {
            if (cmds.size() == 2) search(cmds.get(1));
            else args = cmds.size() - 2;
        }
        else if(cmds.get(0).equals("play")) {
            if (cmds.size() == 2) play(cmds.get(1));
            else args = cmds.size() - 2;
        }
        else System.err.printf("%s: %s\n", cmds.get(0), "invalid command");
        if (args < 0) System.err.printf("%s: %s\n", cmds.get(0), "missing arguments");
        else if (args > 0) System.err.printf("%s: %s\n", cmds.get(0), "too many arguments");
    }

    private static void list() {
        System.out.printf(
            "%30s   |   %27s   |   %12s   |   %9s   |   %40s\n",
            "[Name]", "[Endpoint]", "[Resolution]", "[Bitrate]", "[Keywords]"
        );
        for (Stream s : Portal.getAll()) print(s);
    }

    private static void search(String keys) {
        System.out.printf(
            "%30s   |   %27s   |   %12s   |   %9s   |   %40s\n",
            "[Name]", "[Endpoint]", "[Resolution]", "[Bitrate]", "[Keywords]"
        );
        List<String> keywords = Arrays.asList(keys.split(" *, *"));
        for (Stream s : Portal.getAll()) {
            List<String> matches = Arrays.asList(s.getKeywords()).stream().filter(keywords::contains).collect(Collectors.toList());
            if (matches.size() == keywords.size()) print(s);
        }
    }

    private static void play(String name) {
        for (Stream s : Portal.getAll()) {
            if (s.getName().equals(name)) {
                ProcessBuilder pb = new ProcessBuilder(
                    "ffplay", "-nostats", "-loglevel", "0",
                    s.getEndpoint().getTransport() + "://" + s.getEndpoint().getIp() + ":" + s.getEndpoint().getPort()
                );
                try {
                    pb.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        System.err.println("play: stream not found");
    }

    private static void print(Stream s) {
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
