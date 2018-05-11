package reso.examples.gobackn;

import reso.common.AbstractTimer;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.scheduler.AbstractScheduler;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TimerTask;

public class GoBackNSenderApp extends GoBackNApp{
    private static final long TIMEOUT_DELAY = 2500;
    private static final String APP_NAME = "[SENDER]:  " ;
    private static final String PACKET_SENT = "Packet sent:  " ;
    private static final String SLOW_START_THRESHOLD = "Slow start threshold value:  " ;
    private LinkedList<SeqNTimer> timerList = new LinkedList<SeqNTimer>();
    private int windowCurrentIndex = 1;
    private ArrayList<GoBackNMessage> msgList = new ArrayList<GoBackNMessage>();
    private int windowStartIndex = 1;
    private int windowEndIndex = 1;
    private int windowSize = 1;
    private int badACKCount = 0;
    private final int MSG_AMMOUNT = 50;
    private final double SENDING_DELAY = 0.1;


    private GoBackNTimer timer;
    private String WINDOW_SIZE = "Congestion window size:  ";
    private int confirmWindow = 1;
    private boolean slowStart = true;
    private final String SLOW_START= "### SLOW START ###";
    private int slowStartThreshold = 7;
    private final String ADD_INCREASE = "### ADDITIVE INCREASE ###";
    private final String EVENT_TRIPLE_ACK = "3 duplicate ACKs receivedd ==> send again from window start";
    private final String EVENT_TIMEOUT = "Packet timeout ==> send again from window start";

    public GoBackNSenderApp(IPHost host, IPAddress dst) {
        super(host, dst);
    }


    public class GoBackNTimer extends AbstractTimer {


        public GoBackNTimer(AbstractScheduler scheduler, double interval) {
            super(scheduler, interval, true);
        }

        @Override
        protected void run() throws Exception {
            /* Sinon, on envoie le message de l'indice indiqué */

            if (windowCurrentIndex <= MSG_AMMOUNT  && windowCurrentIndex <= windowEndIndex  ) {
                final GoBackNMessage msg = msgList.get(windowCurrentIndex - 1);
                try {
                    /* Sending Packet */

                    ip.send(IPAddress.ANY, dst, GoBackNProtocol.IP_PROTO_GOBACKN, msg);

                    /* Create timer for this packet */
                    SeqNTimer timer = new SeqNTimer(msg.getSeqN());
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            lossEvent(true);
                            this.cancel();
                        }
                    }, TIMEOUT_DELAY);
                    /* Add timer to timerList */
                    timerList.addLast(timer);

                    windowCurrentIndex++;
                    /* Packet sent */
                    System.out.println(APP_NAME + PACKET_SENT + msg.getSeqN());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void messageReceived(GoBackNMessage msg){
        int seqN = msg.getSeqN();
        if(seqN >= windowStartIndex) {
            SeqNTimer timer = timerList.peekFirst();
            while (timer != null && timer.getSeqN() <= seqN) {
                timer.cancel();
                timerList.removeFirst();
                timer = timerList.peekFirst();
            }


            if (seqN == MSG_AMMOUNT) {
                for (SeqNTimer time : timerList) {
                    time.cancel();
                }
                this.stop();
            }

            windowStartIndex = seqN + 1;
            if (seqN >= confirmWindow)
                confirmWindow();
            windowEndIndex = seqN + windowSize;
            badACKCount = 0;
        }
        else{
            badACKCount += 1;
            if(badACKCount >= 3){
                badACKCount = 0;
                lossEvent(false);
            }
        }


    }

    private void lossEvent(boolean timeout) {
        for(SeqNTimer timer: timerList)
            timer.cancel();
        windowCurrentIndex = windowStartIndex;
        if(timeout) {
            System.out.println(APP_NAME + EVENT_TIMEOUT);
            slowStart = true;
            slowStartThreshold = Math.max(1, windowSize/2);
            windowSize = 1;
            windowEndIndex = windowStartIndex;

            System.out.println(APP_NAME + SLOW_START + WINDOW_SIZE + windowSize);
        }
        /* 3 ack */
        else {
            System.out.println(APP_NAME + EVENT_TRIPLE_ACK);
            slowStart = false;
            slowStartThreshold =  Math.max(1, windowSize/2);
            windowSize =  Math.max(1, windowSize/2);
            windowEndIndex = windowStartIndex + windowSize - 1;
            /* Affiche la taille de la fenêtre dans le log */
            System.out.println(APP_NAME + ADD_INCREASE + WINDOW_SIZE + windowSize);
        }
    }

    private void increaseWindow() {
        windowSize ++;
        windowEndIndex++;

        System.out.println(APP_NAME + WINDOW_SIZE + windowSize);
    }

    private void confirmWindow() {
        if(windowSize >= slowStartThreshold){
            if(slowStart){
                System.out.println(APP_NAME + ADD_INCREASE);
            }
            else{
                increaseWindow();
            }
            slowStart = false;
        }
        else{
            if(!slowStart)
                System.out.println(APP_NAME + SLOW_START);
            slowStart = true;
        }
        confirmWindow = windowStartIndex + windowSize -1;
    }



    @Override
    public void start()
            throws Exception{
        ip.addListener(GoBackNProtocol.IP_PROTO_GOBACKN, new GoBackNProtocol((IPHost) host, this));
        /* generating message to send */
        for(int i = 1; i<=MSG_AMMOUNT; i++) {
            GoBackNMessage msg = new GoBackNMessage(i);
            msgList.add(msg);
        }
        System.out.println(APP_NAME + WINDOW_SIZE + windowSize);
        System.out.println(APP_NAME + SLOW_START_THRESHOLD + slowStartThreshold);
        System.out.println(APP_NAME + "sending " + MSG_AMMOUNT + " packets" + "\n\n");
        /* Initializing sending packets timer */
        this.timer = new GoBackNTimer(host.getNetwork().getScheduler(), SENDING_DELAY);
        this.timer.start();
    }
    @Override
    public void stop(){
        this.timer.stop();
    }
}
