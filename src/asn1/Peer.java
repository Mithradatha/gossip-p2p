package edu.cse4232.gossip.asn1;

import net.ddp2p.ASN1.*;

/**
 * [APPLICATION 2] IMPLICIT SEQUENCE {name UTF8String, port INTEGER, ip PrintableString}
 */
public class Peer extends ASNObj {

    public static final byte TAG = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 2);

    private String name;
    private int port;
    private String ip;

    public Peer() {}

    public Peer(String name, int port, String ip) {
        this.name = name;
        this.port = port;
        this.ip = ip;
    }

    public String getName() { return name; }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    /**
     * @return Peer Encoder
     */
    @Override
    public Encoder getEncoder() {
        Encoder e = new Encoder().initSequence();
        e.addToSequence(new Encoder(name, Encoder.TAG_UTF8String));
        e.addToSequence(new Encoder(port));
        e.addToSequence(new Encoder(ip, Encoder.TAG_PrintableString));
        e.setASN1Type(TAG);
        return e;
    }

    /**
     * @param decoder
     * @return Peer Object
     * @throws ASN1DecoderFail
     */
    @Override
    public Object decode(Decoder decoder) throws ASN1DecoderFail {
        Decoder d = decoder.getContent();
        name = d.getFirstObject(true).getString();
        port = d.getFirstObject(true).getInteger().intValue();
        ip = d.getFirstObject(true).getString();
        if (d.getTypeByte() != 0) throw new ASN1DecoderFail("Wrong Decoder");
        return this;
    }

    /**
     * Used for creating sequences of type Peer
     * @return new Peer Object
     * @throws CloneNotSupportedException
     */
    @Override
    public ASNObj instance() throws CloneNotSupportedException {
        // TODO Implement clone()?
        return new Peer();
    }

    /**
     * @return PEER:John:PORT=2356:IP=163.118.239.68%
     */
    @Override
    public String toString() {
        return String.format("PEER:%s:PORT=%d:IP=%s%%", name, port, ip);
    }

    /**
     * Used for Client GUI
     * @return Peer List formatted String
     */
    public String toFancyString() {
        return String.format("Name: %s  @  IP: %s Port: %d", name, ip, port);
    }
}
