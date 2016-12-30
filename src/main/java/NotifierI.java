import Ice.Current;

public class NotifierI extends VideoStreaming._NotifierDisp {
    public void inform(String stream, Current current) {
        System.out.println(stream);
    }
}