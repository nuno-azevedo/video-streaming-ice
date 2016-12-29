import Ice.Current;
import VideoStreaming.Stream;
import java.util.ArrayList;
import java.util.List;

public class PortalI extends VideoStreaming._PortalDisp {
    private List<Stream> Streams = new ArrayList<Stream>();

    // Calls from Streaming Servers
    public void register(Stream stream, Current current) {
        Streams.add(stream);
    }

    public void remove(Stream stream, Current current) {
        Streams.remove(stream);
    }

    // Calls from Clients
    public List<Stream> getStreams(Current current) {
        return Streams;
    }
}
