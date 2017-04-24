package edu.cse4232.gossip.asn1;

import net.ddp2p.ASN1.*;

/**
 * [1] EXPLICIT SEQUENCE OF Peer
 */
public class PeersAnswer extends ASNObj {

    public final static byte TAG = Encoder.buildASN1byteType(Encoder.CLASS_CONTEXT, Encoder.PC_CONSTRUCTED, (byte) 1);

    private Peer[] peers;

    public PeersAnswer() {}

    public PeersAnswer(Peer[] peers) {
        this.peers = peers;
    }

    public Peer[] getPeers() { return peers; }

    /**
     * @return PeersAnswer Encoder
     */
    @Override
    public Encoder getEncoder() {
        Encoder e = Encoder.getEncoder(peers);
        e.setASN1Type(TAG);
        return e;
    }

    /**
     * @param decoder
     * @return PeersAnswer Object
     * @throws ASN1DecoderFail
     */
    @Override
    public Object decode(Decoder decoder) throws ASN1DecoderFail {
        peers = decoder.getSequenceOf(Peer.TAG, new Peer[0], new Peer());
        return this;
    }

    /**
     * @return PEERS|2|John:PORT=2356:IP=163.118.239.68|Mary:PORT=2355:IP=163.118.237.60|%
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PEERS|").append(peers.length).append("|");
        for (Peer peer : peers) {
            builder.append(peer.getName());
            builder.append(":PORT=").append(peer.getPort());
            builder.append(":IP=").append(peer.getIp());
            builder.append("|");
        }
        return builder.append("%").toString();
    }
}
