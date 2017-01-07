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

            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", video, "-nostats", "-loglevel", "0", "-f", "mpegts", "-analyzeduration", "500k",
                "-probesize", "500k", "-r", "30", "-g", "30", "-s", width + "x" + height, "-c:v", "libx264",
                "-pix_fmt", "yuv420p", "-preset", "ultrafast", "-tune", "zerolatency", "-b:v", bitrate + "k",
                "-b:a", "32k", transport + "://" + ip + ":" + ffmpeg_port + "?listen=1"
            );
            pb.start();
            Thread.sleep(1000);

            Socket input = new Socket(ip, ffmpeg_port);
            InputStream inputStream = input.getInputStream();
            ServerSocket serverSocket = new ServerSocket(ffplay_port);
            List<Socket> clients = new ArrayList<>();
            while (true) {
                Socket client = serverSocket.accept();
                clients.add(client);
                new Thread(() -> {
                    byte bytes[] = new byte[4096];
                    try {
                        DataOutputStream writer = new DataOutputStream(client.getOutputStream());
                        while (inputStream.read(bytes) >= 1) writer.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
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
