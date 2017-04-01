package com.cse4232.gossip.helper.asn;

import net.ddp2p.ASN1.*;

import java.util.Calendar;

// Gossip ::= [APPLICATION 1] EXPLICIT SEQUENCE {sha256hash OCTET STRING, timestamp GeneralizedTime, message UTF8String}

public class Gossip extends ASNObj {

    private final static byte TAG_AP1 = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 1);
    public static final byte TAG = 97;

    private String sha256hash;
    private Calendar timestamp;
    private String message;

    public Gossip() {}

    public Gossip(String sha256hash, Calendar timestamp, String message) {
        this.sha256hash = sha256hash;
        this.timestamp = timestamp;
        this.message = message;
    }

    public Gossip(String sha256hash, String message) {
        this.sha256hash = sha256hash;
        this.message = message;
    }

    public String getSha256hash() { return sha256hash; }

    public Calendar getTimestamp() { return timestamp; }

    public String getMessage() { return message; }

    @Override
    public Encoder getEncoder() {
        Encoder e = new Encoder().initSequence();
        e.addToSequence(new Encoder(sha256hash, Encoder.TAG_OCTET_STRING));
        if (timestamp != null)
            e.addToSequence(new Encoder(Encoder.getGeneralizedTime(timestamp), Encoder.TAG_GeneralizedTime));
        e.addToSequence(new Encoder(message, Encoder.TAG_UTF8String));
        e.setASN1Type(TAG_AP1);
        return e;
    }

    @Override
    public Object decode(Decoder decoder) throws ASN1DecoderFail {
        Decoder d = decoder.getContent();
        sha256hash = d.getFirstObject(true).getString(Encoder.TAG_OCTET_STRING);
        if (d.getTypeByte() == Encoder.TAG_GeneralizedTime)
            timestamp = d.getFirstObject(true).getGeneralizedTimeCalender(Encoder.TAG_GeneralizedTime);
        message = d.getFirstObject(true).getString();
        if (d.getTypeByte() != 0) throw new ASN1DecoderFail("Wrong Decoder");
        return this;
    }

    @Override
    public String toString() {
        return "Gossip{" +
                "sha256hash='" + sha256hash + '\'' +
                ", timestamp=" + ASN1_Util.getStringDate(timestamp) +
                ", message='" + message + '\'' +
                '}';
    }
}
