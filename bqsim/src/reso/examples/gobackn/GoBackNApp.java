package reso.examples.gobackn;

import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public abstract class GoBackNApp extends AbstractApplication{
    private final IPLayer ip;
    private final IPAddress dst;

    public GoBackNApp(IPHost host, IPAddress dst)
    {
        super(host, "GoBackNApp");
        this.dst= dst;
        ip= host.getIPLayer();
    }
    public void messageReceived(){}
    public void start(){}
    public void stop(){}

}