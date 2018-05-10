package reso.examples.gobackn;

public class GoBackNPacket extends GoBackNMessage {

    /**
     * @param seqN Sequence number linked to the packet.
     */
    public GoBackNPacket(int seqN) {
        super(seqN);
    }

}
