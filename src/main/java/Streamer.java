import Streaming.*;

public class Streamer {
    public static void main(String args[]) {
        int status = 0;
        Ice.Communicator ic = null;
        try {
            ic = Ice.Util.initialize(args);
            Ice.ObjectPrx base = ic.stringToProxy("Portal: default -p 11000");
            PortalPrx portal = PortalPrxHelper.checkedCast(base);
            if (portal == null) throw new Error("Invalid proxy");

            Stream stream = new Stream(
                "The Vagabond",
                new Endpoint("tcp", "127.0.0.1", 10000),
                new Resolution(480, 720),
                400,
                new String[] { "Film", "Story", "Vagabond" }
            );
            portal.register(stream);
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