package reso.examples.gobackn;

import reso.common.AbstractApplication;
import reso.examples.pingpong.PingPongProtocol;
import reso.ip.IPAddress;
import reso.ip.IPHost;


public class GoBackNApp extends AbstractApplication{

    public GoBackNApp(IPHost host, IPAddress dst)
    {
        super(host, "GoBackNApp");
    }

    public void start() {
        ip.addListener(PingPongProtocol.IP_PROTO_PINGPONG, new PingPongProtocol((IPHost) host));
    }
    public void stop() {}

}