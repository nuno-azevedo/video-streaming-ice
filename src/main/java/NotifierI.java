public class NotifierI extends Streaming._NotifierDisp {
    public void inform(String stream, Ice.Current current) {
        System.out.print(stream + "\nstream: ");
    }
}