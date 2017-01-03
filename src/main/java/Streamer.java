import Streaming.*;

import javax.swing.*;

public class Streamer {
    public static void main(String args[]) {
        if (args.length != 1) {
            System.err.println("USAGE: java Streamer $PORT");
            System.exit(1);
        }

        int status = 0;
        Ice.Communicator ic = null;
        try {
            ic = Ice.Util.initialize(args);
            Ice.ObjectPrx base = ic.stringToProxy("Portal: default -p " + args[0]);
            PortalPrx portal = PortalPrxHelper.checkedCast(base);
            if (portal == null) throw new Error("Invalid proxy");

            Stream stream = new Stream(
                "The Vagabond",
                new Endpoint("tcp", "127.0.0.1", 12000),
                new Resolution(480, 720),
                400,
                new String[] { "Film", "Story", "Vagabond" }
            );
            if (!portal.register(stream)) {
                System.err.println("register: stream already exists");
                System.exit(1);
            }

            new Thread(() -> new Timer(5000, (z) -> portal.update(stream)).start()).start();
            while (true);
//            portal.remove(stream);
        } catch (Ice.LocalException e) {
            e.printStackTrace();
            status = 1;
        } catch (Exception e) {
            System.err.println(e.getMessage());
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
}
