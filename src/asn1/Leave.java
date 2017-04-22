package edu.cse4232.gossip.asn1;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASNObj;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;

// Leave ::= [APPLICATION 4] EXPLICIT SEQUENCE {name UTF8String}

public class Leave extends ASNObj {

    public static final byte TAG = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 4);

    private String name;

// --Commented out by Inspection START (4/22/2017 2:06 PM):
//    public Leave(String name)
//    {
//        this.name = name;
//    }
// --Commented out by Inspection STOP (4/22/2017 2:06 PM)

    public Leave() {}

    public String getName() {return name;}


    @Override
    public Encoder getEncoder() {
        Encoder e = new Encoder().initSequence();
        e.addToSequence(new Encoder(name, Encoder.TAG_UTF8String));
        Encoder wrapper = new Encoder().initSequence();
        wrapper.addToSequence(e);
        wrapper.setASN1Type(TAG);
        return wrapper;
    }

    @Override
    public Object decode(Decoder decoder) throws ASN1DecoderFail {
        Decoder d = decoder.getContent().getContent();
        name = d.getFirstObject(true).getString();
        return this;
    }
}
