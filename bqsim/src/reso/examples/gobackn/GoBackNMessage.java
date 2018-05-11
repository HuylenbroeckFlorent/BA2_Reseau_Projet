package reso.examples.gobackn;

import reso.common.Message;

public class GoBackNMessage implements Message
{
    private final int seqN;

    public GoBackNMessage(int seqN)
    {
        this.seqN=seqN;
    }

    public int getSeqN()
    {
        return seqN;
    }

    @Override
    public int getByteLength()
    {
        return 0;
    }
}