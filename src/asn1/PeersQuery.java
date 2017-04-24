package edu.cse4232.gossip.asn1;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASNObj;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;

/**
 * [APPLICATION 3] IMPLICIT NULL
 */
public class PeersQuery extends ASNObj {

    public final static byte TAG = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 3);

    public PeersQuery() {}

    /**
     * @return PeersQuery Encoder
     */
    @Override
    public Encoder getEncoder() {
        Encoder e = Encoder.getNullEncoder();
        e.setASN1Type(TAG);
        return e;
    }

    /**
     * @param decoder
     * @return PeersQuery Object
     * @throws ASN1DecoderFail
     */
    @Override
    public Object decode(Decoder decoder) throws ASN1DecoderFail {
        Decoder d = decoder.getContent();
        //if (d.getTypeByte() != 0) throw new ASN1DecoderFail("Wrong Decoder");
        return this;
    }

    /**
     * @return PEERS?\n
     */
    @Override
    public String toString() {
        return "PEERS?\\n";
    }
}
