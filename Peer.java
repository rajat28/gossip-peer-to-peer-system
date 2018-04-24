import net.ddp2p.ASN1.*;

//Peer ::= [APPLICATION 2] IMPLICIT SEQUENCE {name UTF8String, port INTEGER, ip PrintableString}
@ASN1Type(_class = Encoder.CLASS_APPLICATION, _pc = Encoder.PC_CONSTRUCTED, _tag = 2)
public class Peer extends ASNObjArrayable {
	final static byte TAG_AC2 = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION,Encoder.PC_CONSTRUCTED,(byte)2);
	String name;
	int port;
	String ip;

	public Peer(String name, int port, String ip) {
		this.name = name;
		this.port = port;
		this.ip = ip;
	}

	public Peer() {}

	public Encoder getEncoder() {
		Encoder e = new Encoder().initSequence();
		e.addToSequence(new Encoder(name).setASN1TypeImplicit(Encoder.TAG_UTF8String));
		e.addToSequence(new Encoder(port).setASN1TypeImplicit(Encoder.TAG_INTEGER));
		e.addToSequence(new Encoder(ip).setASN1TypeImplicit(Encoder.TAG_PrintableString));
		return e.setASN1TypeImplicit(Peer.class);
	}

	public Peer decode(Decoder dec) throws ASN1DecoderFail {
		Decoder d = dec.getContentImplicit();
		name = d.getFirstObject(true).getString(Encoder.TAG_UTF8String);
		port = d.getFirstObject(true).getInteger(Encoder.TAG_INTEGER).intValue();
		ip = d.getFirstObject(true).getString(Encoder.TAG_PrintableString);
		if (d.getTypeByte() != 0)
			throw new ASN1DecoderFail("Extra objects!");
		return this;
	}

	public static byte getASN1Type() {
		return TAG_AC2;
	}

	public Peer instance() throws CloneNotSupportedException {
		return new Peer();
	}
}
