import net.ddp2p.ASN1.*;

//Peer ::= [APPLICATION 2] IMPLICIT SEQUENCE {name UTF8String, port INTEGER, ip PrintableString}
@ASN1Type(_class = Encoder.CLASS_APPLICATION, _pc = Encoder.PC_CONSTRUCTED, _tag = 3)
public class PeersQuery extends ASNObjArrayable {
    final static byte TAG_AC3 = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 3);
    String query = "";

    public Encoder getEncoder() {
        Encoder e = new Encoder().initSequence();
        e.addToSequence(new Encoder(query));

        return e.setASN1TypeImplicit(Peer.class);
    }

    public PeersQuery decode(Decoder dec) throws ASN1DecoderFail {
        Decoder d = dec.getContentImplicit();
        query = d.getFirstObject(true).getString();

        if (d.getTypeByte() != 0) {
            throw new ASN1DecoderFail("Extra objects!");
        }
        return this;
    }

    public static byte getASN1Type() {
        return TAG_AC3;
    }

    public PeersQuery instance() throws CloneNotSupportedException {
        return new PeersQuery();
    }
}