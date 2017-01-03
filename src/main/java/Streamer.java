import Streaming.*;

import javax.swing.*;
import java.io.IOException;

public class Streamer {
    public static void main(String args[]) {
        if (args.length != 7) {
            System.err.println("USAGE: java Streamer $PORT $VIDEO $NAME $ENDPOINT $RESOLUTION $BITRATE $KEYWORDS");
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
                args[2],
                new Endpoint(args[3].split("://")[0], args[3].split("://")[1], Integer.parseInt(args[3].split(":")[2])),
                new Resolution(Integer.parseInt(args[4].split("x")[0]), Integer.parseInt(args[4].split("x")[1])),
                Integer.parseInt(args[5]),
                args[6].split(" *, *")
            );
            if (!portal.register(stream)) {
                System.err.println("register: stream already exists");
                System.exit(1);
            }
            new Thread(() -> new Timer(5000, (z) -> portal.update(stream)).start()).start();

            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", args[1], "-vcodec", "libx264", "-f", "h264", args[3] + "?listen=1"
            );
            Process ffmpeg = null;
            try {
                ffmpeg = pb.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ffmpeg.waitFor();
            portal.remove(stream);
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
