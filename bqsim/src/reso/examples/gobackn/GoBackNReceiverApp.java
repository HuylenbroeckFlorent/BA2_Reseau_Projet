package reso.examples.gobackn;

import reso.ip.IPAddress;
import reso.ip.IPHost;

import java.util.Random;

public class GoBackNReceiverApp extends GoBackNApp{

    // Strings used multiple time
    private static final String LOG_APP_NAME = "[RECEIVER] ";
    private static final String LOG_PACKET_RECEIVED = "Packet received: ";

    // Unexpected events probability (PROB_X / NUMBER_OF_EVENT)
    private static final int PROB_ACK_NOT_SENT = 1;
    private static final int PROB_ACK_DELAYED = 950;
    private static final int PROB_ACK_TIMED_OUT = 1;
    private static final int PROB_PACKET_LOST = 1;
    private static final int PROB_PACKET_CORRUPTED = 1;
    private static final int NUMBER_OF_EVENT = 1000;

    // Small delay range (in ms)
    private static final int SMALL_DELAY_RANGE_MIN = 50;
    private static final int SMALL_DELAY_RANGE_MAX = 300;

    // Random, to simulate packet loss, packet timeouts, random ACK sending delay, ...
    private final Random rd = new Random();


    // Current sequence number the app has checked
    private int curSeqN;

    GoBackNReceiverApp(IPHost host, IPAddress dst) {
        super(host, dst);
        curSeqN=0;
    }

    /**
     * Sends ACK
     * @param seqN  int, Sequence number
     * @return  boolean, whether if ACK has been sent or not
     */
    private boolean sendACK(int seqN){
        // Randomly decides not to send ACK
        if(rd.nextInt(NUMBER_OF_EVENT)<=PROB_ACK_NOT_SENT){
            System.out.println(LOG_APP_NAME+"("+seqN+") ACK was lost");
            return false;
        }


        // Creates ACK
        final GoBackNMessage ack = new GoBackNMessage(seqN);

        // ACK is randomly delayed (small delay)
        if(rd.nextInt(NUMBER_OF_EVENT)<=PROB_ACK_DELAYED){
            System.out.println(LOG_APP_NAME+"("+seqN+") ACK got delayed");
            try {
                Thread.sleep(rd.nextInt(SMALL_DELAY_RANGE_MAX-SMALL_DELAY_RANGE_MIN) + SMALL_DELAY_RANGE_MIN);
            }catch(InterruptedException ie){
                ie.printStackTrace();
                }
        }
        // ACK is randomly sent after time-out
        else if(rd.nextInt(NUMBER_OF_EVENT)<=PROB_ACK_TIMED_OUT){
            System.out.println(LOG_APP_NAME+"("+seqN+") ACK got hugely delayed (delay>timeout)");
             try{
                 Thread.sleep(GoBackNSenderApp.TIMEOUT_DELAY);
             }catch(InterruptedException ie2){
                 ie2.printStackTrace();
             }
        }

        // Sends ACK
        try {
            ip.send(IPAddress.ANY, dst, GoBackNProtocol.IP_PROTO_GOBACKN, ack);
            System.out.println(LOG_APP_NAME+"("+seqN+") ACK sent");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void messageReceived(GoBackNMessage msg) {
        final int seqN = msg.getSeqN();

        // Received packet is the currently awaited packet
        if(seqN==this.curSeqN+1){

            // Randomly loses the packet
            if(rd.nextInt(NUMBER_OF_EVENT)<=PROB_PACKET_LOST){return;}

            // Packet is randomly corrupted
            if(rd.nextInt(NUMBER_OF_EVENT)<=PROB_PACKET_CORRUPTED){
                System.out.println(LOG_APP_NAME+ LOG_PACKET_RECEIVED +seqN+" - packet is corrupted > sending last OK packet ACK");
                sendACK(curSeqN);
                return;
            }

            // Packet is OK, sends ACK
            System.out.println(LOG_APP_NAME+ LOG_PACKET_RECEIVED +seqN+" - packet is OK > sending ACK");
            if(sendACK(seqN))
                curSeqN++;
        }

        // Received packet has already been received
        else if(seqN<curSeqN+1){
            System.out.println(LOG_APP_NAME+ LOG_PACKET_RECEIVED +seqN+" - packet already received > sending ACK again");
            this.sendACK(seqN);
        }

        // Received packet is ahead of currently awaited packet (means at least one packet has been lost)
        else{
            System.out.println(LOG_APP_NAME+ LOG_PACKET_RECEIVED +seqN+" - packet is ahead of current > sending last OK packet ACK");
            this.sendACK(curSeqN);
        }
    }

    @Override
    public void start()
        throws Exception{
        ip.addListener(GoBackNProtocol.IP_PROTO_GOBACKN, new GoBackNProtocol((IPHost) host, this));
    }
}
