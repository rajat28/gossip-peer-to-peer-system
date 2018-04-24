import net.ddp2p.ASN1.*;

//Gossip ::= [APPLICATION 1] EXPLICIT SEQUENCE {sha256hash OCTET STRING, timestamp GeneralizedTime, message UTF8String}
@ASN1Type(_class = Encoder.CLASS_APPLICATION, _pc = Encoder.PC_CONSTRUCTED, _tag = 1)
public class Gossip extends ASNObjArrayable {
	final static byte TAG_AC1 = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION,Encoder.PC_CONSTRUCTED,(byte)1);
	String sha256hash;
	String timestamp;
	String message;
	public Encoder getEncoder() {
		Encoder e = new Encoder().initSequence();
		e.addToSequence(new Encoder(sha256hash).setASN1TypeImplicit(Encoder.TAG_OCTET_STRING));
		e.addToSequence(new Encoder(timestamp).setASN1TypeImplicit(Encoder.TAG_GeneralizedTime));
		e.addToSequence(new Encoder(message).setASN1TypeImplicit(Encoder.TAG_UTF8String));
		return e.setASN1TypeExplicit(Gossip.class);
	}
	public Gossip decode(Decoder dec) throws ASN1DecoderFail {
		Decoder d = dec.getContentExplicit();
		sha256hash = d.getFirstObject(true).getString(Encoder.TAG_OCTET_STRING);
		timestamp = d.getFirstObject(true).getString(Encoder.TAG_GeneralizedTime);
		message = d.getFirstObject(true).getString(Encoder.TAG_UTF8String);
		if (d.getTypeByte() != 0)
			throw new ASN1DecoderFail("Extra objects!");
		return this;
	}

	public static byte getASN1Type() {
		return TAG_AC1;
		//return Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte)2);
	}

	public Gossip instance() throws CloneNotSupportedException {
		return new Gossip();
	}
	

}
