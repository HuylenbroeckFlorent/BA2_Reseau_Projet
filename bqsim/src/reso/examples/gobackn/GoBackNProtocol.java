package reso.examples.gobackn;

import reso.ip.Datagram;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;


public class GoBackNProtocol implements IPInterfaceListener {
    public static final int IP_PROTO_GOBACKN= Datagram.allocateProtocolNumber("GOBACKN");
    protected final IPHost host;

    private GoBackNApp app;

    public GoBackNProtocol(IPHost host, GoBackNApp app) {
        this.host= host;
        this.app = app;
    }

    public void receive(IPInterfaceAdapter src, Datagram datagram)
            throws Exception {
        // message can be ack or packet
        GoBackNMessage msg= (GoBackNMessage) datagram.getPayload();
        // message received
        app.messageReceived(msg);
    }

}
