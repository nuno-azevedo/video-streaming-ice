import IceInternal.Ex;
import Streaming.*;

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Streamer {
    public static void main(String args[]) {
        if (args.length != 7) {
            System.err.println("USAGE: java Streamer $PORT $VIDEO $NAME $ENDPOINT $RESOLUTION $BITRATE $KEYWORDS");
            System.exit(1);
        }

        String portal_port = args[0];
        String video = args[1];
        String name = args[2];
        String transport = args[3].split("://")[0];
        String ip = args[3].split("://")[1].split(":")[0];
        int ffmpeg_port = Integer.parseInt(args[3].split(":")[2]);
        int ffplay_port = ffmpeg_port + 1;
        int width = Integer.parseInt(args[4].split("x")[0]);
        int height = Integer.parseInt(args[4].split("x")[1]);
        int bitrate = Integer.parseInt(args[5]);
        String keywords[] = args[6].split(" *, *");

        int status = 0;
        Ice.Communicator ic = null;
        Ice.Util.initialize(args);
        try {
            ic = Ice.Util.initialize(args);
            Ice.ObjectPrx base = ic.stringToProxy("Portal: default -p " + portal_port);
            PortalPrx portal = PortalPrxHelper.checkedCast(base);
            if (portal == null) throw new Error("Invalid proxy");

            Stream stream = new Stream(
                name,
                new Endpoint(transport, ip, ffplay_port),
                new Resolution(width, height),
                bitrate,
                keywords
            );
            if (!portal.register(stream)) {
                System.err.println("register: stream already exists");
                System.exit(1);
            }
            new Timer(5000, (z) -> portal.update(stream)).start();
//            Runtime.getRuntime().addShutdownHook(new Thread(() -> portal.remove(stream)));

            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", video,
                 "-vcodec", "libx264", "-f", "h264", "-s", width + "x" + height,
                 transport + "://" + ip + ":" + ffmpeg_port + "?listen=1"
            );
            pb.start();
            Thread.sleep(1000);

            Socket input = new Socket(ip, ffmpeg_port);
            InputStream inputStream = input.getInputStream();
            ServerSocket serverSocket = new ServerSocket(ffplay_port);
            List<Socket> clients = new ArrayList<>();
            new Thread(() -> {
                while (true) {
                    Socket client = null;
                    try {
                        client = serverSocket.accept();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    clients.add(client);
                }
            }).start();

            byte bytes[] = new byte[2048];
            while (inputStream.read(bytes) >= 1) {
                for (int i = 0; i < clients.size(); i++) {
                    try {
                        DataOutputStream writer = new DataOutputStream(clients.get(i).getOutputStream());
                        writer.write(bytes);
                    } catch (Exception e) {
                        clients.remove(i);
                    }
                }
            }
        } catch (Ice.LocalException e) {
            e.printStackTrace();
            status = 1;
        } catch (Exception e) {
            System.err.println(e.getMessage());
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
            System.exit(status);
        }
    }
}
