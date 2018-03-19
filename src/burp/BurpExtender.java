package burp;
import burp.gather.ContextMenuFactory;
import burp.gather.ScannerCheck;

import java.io.PrintWriter;
public class BurpExtender implements IBurpExtender
{
    private static IBurpExtenderCallbacks BURPCALLBACK ;

    private static PrintWriter stdout;

    public static PrintWriter getStdout() {
        return stdout;
    }

    public static IBurpExtenderCallbacks getBURPCALLBACK() {
        return BURPCALLBACK;
    }

    public static void setBURPCALLBACK(IBurpExtenderCallbacks BURPCALLBACK) {
        BurpExtender.BURPCALLBACK = BURPCALLBACK;
    }

    // implement IBurpExtender
    //

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        setBURPCALLBACK(callbacks);

        // set our extension name
        callbacks.setExtensionName("InfoGather");

        callbacks.registerContextMenuFactory(new ContextMenuFactory());

        callbacks.registerScannerCheck(new ScannerCheck());

        stdout = new PrintWriter(callbacks.getStdout(), true);
        // load extender successfully.
        stdout.println("load extender successfully.");
    }
}