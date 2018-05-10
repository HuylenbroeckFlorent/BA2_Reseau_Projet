package reso.examples.gobackn;

import reso.ip.IPAddress;
import reso.ip.IPHost;

public class GoBackNReceiverApp extends GoBackNApp{
    private static final String APP_NAME = "[RECEIVER]:  ";
    private static final String MESSAGE_RECEIVED = "Packet received:  ";
    private static final String EVENT_RECEIVED = "Packet is ok ==> sending ACK";

    public GoBackNReceiverApp(IPHost host, IPAddress dst) {
        super(host, dst);
    }

    private void sendACK(int seqN) {
        final GoBackNACK ack = new GoBackNACK(seqN);
        try {
            ip.send(IPAddress.ANY, dst, GoBackNProtocol.IP_PROTO_GOBACKN, ack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageReceived(GoBackNMessage msg) {
        final int seqN = msg.getSeqN();
        System.out.println(APP_NAME + MESSAGE_RECEIVED + seqN + EVENT_RECEIVED);
        sendACK(seqN);

    }

    @Override
    public void start()
        throws Exception{
        ip.addListener(GoBackNProtocol.IP_PROTO_GOBACKN, new GoBackNProtocol((IPHost) host, this));
    }
}
