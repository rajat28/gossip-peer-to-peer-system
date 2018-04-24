import net.ddp2p.ASN1.*;
import java.util.ArrayList;

//PeersAnswer ::= [1] EXPLICIT SEQUENCE OF Peer
@ASN1Type(_class = Encoder.CLASS_CONTEXT, _pc = Encoder.PC_CONSTRUCTED, _tag = 1)
public class PeerAnswer extends ASNObj {
	private final static byte TAG_CC1 = Encoder.buildASN1byteType(Encoder.CLASS_CONTEXT,Encoder.PC_CONSTRUCTED,(byte)1);
	public ArrayList<Peer> peers;

	PeerAnswer() {
		peers = new ArrayList<>();
	}

	public Encoder getEncoder() {
		// Encoder enc = new Encoder().initSequence();
		// enc.addToSequence(Encoder.getEncoder(peers));
		Encoder enc = Encoder.getEncoder(peers);
		//return enc.setASN1TypeExplicit(Encoder.CLASS_CONTEXT, Encoder.PC_CONSTRUCTED, new BigInteger("1"));
		return enc.setASN1TypeExplicit(PeerAnswer.class);
	}

	public PeerAnswer decode(Decoder dec) throws ASN1DecoderFail {
		Decoder d = dec.removeExplicitASN1Tag();
		peers = d.getFirstObject(true).getSequenceOfAL(Peer.getASN1Type(), new Peer());
		return this;
	}

	public static byte getASN1Type() {
		return TAG_CC1;
	}
}
