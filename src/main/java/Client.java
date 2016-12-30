import VideoStreaming.Stream;
import java.util.Scanner;

public class Client {
    public static VideoStreaming.PortalPrx portal;

    public static void main(String args[]) {
        int status = 0;
        Ice.Communicator ic = null;
        try {
            ic = Ice.Util.initialize(args);
            Ice.ObjectPrx base = ic.stringToProxy("Portal: default -p 10000");

            portal = VideoStreaming.PortalPrxHelper.checkedCast(base);
            if (portal == null) throw new Error("Invalid proxy");

            Scanner scan = new Scanner(System.in);
            String cmd = new String();
            while(!cmd.equals("exit")) {
                System.out.print("stream: ");
                cmd = scan.nextLine();
                parser(cmd);
            }
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
        System.exit(status);
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
        else if(cmd_list[1].equals("play")) {
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
        System.out.printf("%30s   |   %27s   |   %12s   |   %9s   |   %40s\n", "", "", "", "", "");
        for (Stream s : portal.getStreams()) {
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
