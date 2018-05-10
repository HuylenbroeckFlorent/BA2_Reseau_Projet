package reso.examples.gobackn;

import java.util.Timer;

/* Timer linked to a sequence number*/
public class SeqNTimer extends Timer {

    private int seqN;

    public SeqNTimer(int seqN) {
        super();
        this.seqN = seqN;
    }

    public int getSeqN() {
        return this.seqN;
    }
}