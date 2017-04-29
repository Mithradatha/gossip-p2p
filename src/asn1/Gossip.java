package edu.cse4232.gossip.asn1;

import net.ddp2p.ASN1.*;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;

/**
 * [APPLICATION 1] EXPLICIT SEQUENCE {sha256hash OCTET STRING, timestamp GeneralizedTime, message UTF8String}
 */
public class Gossip extends ASNObj {

    public final static byte TAG = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 1);

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SSS'Z'");

    private byte[] sha256hash;
    private Calendar timestamp;
    private String message;

    public Gossip() {
    }

    public Gossip(byte[] sha256hash, Calendar timestamp, String message) {
        this.sha256hash = sha256hash;
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getSha256hash() {
        return Base64.getEncoder().encodeToString(sha256hash);
    }

    public String getTimestamp() {
        return timestampToString(timestamp);
    }

    public String getMessage() {
        return message;
    }

    /**
     * Utility Function for Timestamp Display
     *
     * @param timestamp
     * @return Formatted String
     */
    public static String timestampToString(Calendar timestamp) {
        return formatter.format(timestamp.getTime());
    }

    /**
     * @return Gossip Encoder
     */
    @Override
    public Encoder getEncoder() {
        Encoder e = new Encoder().initSequence();
        e.addToSequence(new Encoder(sha256hash, Encoder.TAG_OCTET_STRING));
        if (timestamp != null)
            e.addToSequence(new Encoder(Encoder.getGeneralizedTime(timestamp), Encoder.TAG_GeneralizedTime));
        e.addToSequence(new Encoder(message, Encoder.TAG_UTF8String));
        Encoder wrapper = new Encoder().initSequence();
        wrapper.addToSequence(e);
        wrapper.setASN1Type(TAG);
        return wrapper;
    }

    /**
     * @param decoder
     * @return Gossip Object
     * @throws ASN1DecoderFail
     */
    @Override
    public Object decode(Decoder decoder) throws ASN1DecoderFail {
        Decoder d = decoder.getContent().getContent();
        sha256hash = d.getFirstObject(true).getBytes(Encoder.TAG_OCTET_STRING);
        if (d.getTypeByte() == Encoder.TAG_GeneralizedTime)
            timestamp = d.getFirstObject(true).getGeneralizedTimeCalender(Encoder.TAG_GeneralizedTime);
        message = d.getFirstObject(true).getString();
        if (d.getTypeByte() != 0) throw new ASN1DecoderFail("Wrong Decoder");
        return this;
    }

    /**
     * @return GOSSIP:mBHL7IKilvdcOFKR03ASvBNX//ypQkTRUvilYmB1/OY=:2017-01-09-16-18-20-001Z:Tom eats Jerry%
     */
    @Override
    public String toString() {
        return String.format("GOSSIP:%s:%s:%s%%", getSha256hash(),
                getTimestamp(), message);
    }
}
