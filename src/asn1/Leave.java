package edu.cse4232.gossip.asn1;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASNObj;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;

/**
 * [APPLICATION 4] EXPLICIT SEQUENCE {name UTF8String}
 */
public class Leave extends ASNObj {

    public static final byte TAG = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 4);

    private String name;

    public Leave() {}

    public String getName() {return name;}

    /**
     * @return Leave Encoder
     */
    @Override
    public Encoder getEncoder() {
        Encoder e = new Encoder().initSequence();
        e.addToSequence(new Encoder(name, Encoder.TAG_UTF8String));
        Encoder wrapper = new Encoder().initSequence();
        wrapper.addToSequence(e);
        wrapper.setASN1Type(TAG);
        return wrapper;
    }

    /**
     * @param decoder
     * @return Leave Object
     * @throws ASN1DecoderFail
     */
    @Override
    public Object decode(Decoder decoder) throws ASN1DecoderFail {
        Decoder d = decoder.getContent().getContent();
        name = d.getFirstObject(true).getString();
        return this;
    }

    /**
     * @return LEAVE:John%
     */
    @Override
    public String toString() {
        return String.format("LEAVE:%s%%", name);
    }
}
