package reso.examples.gobackn;

import reso.common.Link;
import reso.common.Network;
import reso.ethernet.EthernetAddress;
import reso.ethernet.EthernetFrame;
import reso.ethernet.EthernetInterface;
import reso.examples.static_routing.AppSniffer;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.scheduler.AbstractScheduler;
import reso.scheduler.Scheduler;
import reso.utilities.NetworkBuilder;

public class Main {
    private static final boolean ENABLE_SNIFFER= false;
    private static final int LINK_SIZE=5000000;
    private static final int DEBIT=100000;
    public static void main(String [] args) {
        AbstractScheduler scheduler= new Scheduler();
        Network network= new Network(scheduler);
        try {

            final EthernetAddress MAC_ADDR1= EthernetAddress.getByAddress(0x00, 0x26, 0xbb, 0x4e, 0xfc, 0x28);
            final EthernetAddress MAC_ADDR2= EthernetAddress.getByAddress(0x00, 0x26, 0xbb, 0x4e, 0xfc, 0x29);
            final IPAddress IP_ADDR1= IPAddress.getByAddress(192, 168, 0, 1);
            final IPAddress IP_ADDR2= IPAddress.getByAddress(192, 168, 0, 2);

            IPHost host1= NetworkBuilder.createHost(network, "H1", IP_ADDR1, MAC_ADDR1);
            host1.getIPLayer().addRoute(IP_ADDR2, "eth0");
            /* Init sender */
            if (ENABLE_SNIFFER)
                host1.addApplication(new AppSniffer(host1, new String [] {"eth0"}));
            host1.addApplication(new GoBackNSenderApp(host1, IP_ADDR2));

            IPHost host2= NetworkBuilder.createHost(network,"H2", IP_ADDR2, MAC_ADDR2);
            host2.getIPLayer().addRoute(IP_ADDR1, "eth0");
            /* Init receiver */
            host2.addApplication(new GoBackNReceiverApp(host2, IP_ADDR1));

            EthernetInterface h1_eth0= (EthernetInterface) host1.getInterfaceByName("eth0");
            EthernetInterface h2_eth0= (EthernetInterface) host2.getInterfaceByName("eth0");

            new Link<EthernetFrame>(h1_eth0, h2_eth0, LINK_SIZE, DEBIT);

            host1.start();
            host2.start();

            scheduler.run();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
    }

}