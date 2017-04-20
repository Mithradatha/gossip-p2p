package com.cse4232.gossip.helper.asn;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASNObj;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;

// PeersQuery ::= [APPLICATION 3] IMPLICIT NULL

public class PeersQuery extends ASNObj {

    public final static byte TAG = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 3);

    public PeersQuery() {}

    @Override
    public Encoder getEncoder() {
        Encoder e = Encoder.getNullEncoder();
        e.setASN1Type(TAG);
        return e;
    }

    @Override
    public Object decode(Decoder decoder) throws ASN1DecoderFail {
        Decoder d = decoder.getContent();
        //if (d.getTypeByte() != 0) throw new ASN1DecoderFail("Wrong Decoder");
        return this;
    }

    @Override
    public String toString() {
        return "PEERS?\\n";
    }
}
