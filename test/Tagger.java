package test;

import net.ddp2p.ASN1.Encoder;

public class Tagger {

    public static void main(String... args) {

        System.out.println("Gossip: " + Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte)1));
        System.out.println("Peer: " + Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte)2));
        System.out.println("PeersQuery: " + Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte)3));
        System.out.println("PeersAnswer: " + Encoder.buildASN1byteType(Encoder.CLASS_CONTEXT, Encoder.PC_CONSTRUCTED, (byte)1));
        System.out.println("Leave: " + Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte)4));
    }
}
