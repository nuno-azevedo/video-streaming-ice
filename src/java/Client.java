public class Client {
    public static void main(String args[]) {
        int status = 0;
        Ice.Communicator ic = null;
        try {
            ic = Ice.Util.initialize(args);
            Ice.ObjectPrx base = ic.stringToProxy("Portal: default -p 10000");
            Demo.PortalPrx portal = Demo.PortalPrxHelper.checkedCast(base);
            if (portal == null) throw new Error("Invalid proxy");
            portal.print("Hello Portal!");

            base = ic.stringToProxy("Server: default -p 10001");
            Demo.ServerPrx server = Demo.ServerPrxHelper.checkedCast(base);
            if (server == null) throw new Error("Invalid proxy");
            server.print("Hello Server!");
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
