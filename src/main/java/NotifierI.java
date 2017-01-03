public class NotifierI extends Streaming._NotifierDisp {
    public void inform(String message, Ice.Current current) {
        System.out.print("\n" + message + "\nstream: ");
    }
}
