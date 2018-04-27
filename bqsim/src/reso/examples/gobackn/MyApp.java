package reso.examples.gobackn;

import reso.common.AbstractApplication;
import reso.examples.pingpong.PingPongProtocol;
import reso.ip.IPAddress;
import reso.ip.IPHost;


public class MyApp extends AbstractApplication{

    public MyApp(IPHost host, IPAddress dst){

    }

    public void start() {
        ip.addListener(PingPongProtocol.IP_PROTO_PINGPONG, new PingPongProtocol((IPHost) host));
    }
    public void stop() {}

}