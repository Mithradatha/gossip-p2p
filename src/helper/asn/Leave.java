package com.cse4232.gossip.helper.asn;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASNObj;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;

import java.math.BigInteger;

/**
 * Created by Nemahs on 4/18/2017.
 */
public class Leave extends ASNObj {

    private String name;
    public static final byte TAG = 96;

    public Leave(String name)
    {
        this.name = name;
    }


    public String getName() {return name;}


    @Override
    public Encoder getEncoder() {
        Encoder e = new Encoder().initSequence();
        e.addToSequence(new Encoder(name, Encoder.TAG_UTF8String));
        e.setExplicitASN1Tag((int)Encoder.CLASS_APPLICATION, (int)Encoder.PC_CONSTRUCTED, BigInteger.valueOf(4));
        return e;
    }

    @Override
    public Object decode(Decoder decoder) throws ASN1DecoderFail {
        Decoder d = decoder.getContent();
        name = d.getFirstObject(true).getString();
        return this;
    }
}
