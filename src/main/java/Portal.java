public class Portal extends Ice.Application {
    public static void main(String args[]) {
        if (args.length != 1) {
            System.err.println("USAGE: java Portal $PORT");
            System.exit(1);
        }

        Portal app = new Portal();
        int status = app.main("Portal", args, "configs/config.pub");
        System.exit(status);
    }

    @Override
    public int run(String args[]) {
        int status = 0;
        Ice.Communicator ic = null;
        try {
            ic = Ice.Util.initialize(args);
            Ice.ObjectAdapter adapter = ic.createObjectAdapterWithEndpoints("PortalAdapter", "default -p " + args[0]);
            Ice.Object object = new PortalI();
            adapter.add(object, ic.stringToIdentity("Portal"));
            adapter.activate();
            ic.waitForShutdown();
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
        }
        return status;
    }
}
