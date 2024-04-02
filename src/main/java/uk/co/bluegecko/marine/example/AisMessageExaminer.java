package uk.co.bluegecko.marine.example;

import dk.dma.ais.message.AisMessage;
import dk.dma.ais.sentence.SentenceLine;
import dk.dma.ais.sentence.Vdm;
import java.time.Instant;

public class AisMessageExaminer {

	private final Vdm vdm;

	public AisMessageExaminer() {
		this.vdm = new Vdm();
	}

	public static final void main(String... args) throws Exception {
		AisMessageExaminer examiner = new AisMessageExaminer();

		examiner.parseWithPreamble();
	}

	private void parseWithPreamble() throws Exception {
		String line = "\\s:2573335,c:1696574125*02\\!BSVDM,1,1,,B,B3mK<:00089sVNa8gQGQ3wd1nDgr,0*60";

		System.out.println(">>>>>>>>>>");
		SentenceLine sentenceLine = new SentenceLine(line);
		System.out.println("head: " + sentenceLine.getSentenceHead());
		int id = vdm.parse(sentenceLine);
		System.out.println("<<<<<<<<<<");
		if (vdm.getCommentBlock() != null) {
			if (vdm.getCommentBlock().contains("s")) {
				System.out.println("s:" + vdm.getCommentBlock().getString("s"));
			}
			System.out.println("c:" + Instant.ofEpochSecond(vdm.getCommentBlock().getTimestamp()));
		}

		AisMessage message = AisMessage.getInstance(vdm);

		System.out.println(message);

//		AisMessage message = AisMessage.getInstance(vdm);
//		System.out.println(message);
//		System.out.println(message.getSourceTag());
	}

	private void parseMultipart() throws Exception {
		String aisSentence1 = "!AIVDM,2,1,9,B,53nFBv01SJ<thHp6220H4heHTf2222222222221?50:454o<`9QSlUDp,0*09";
		String aisSentence2 = "!AIVDM,2,2,9,B,888888888888880,2*2E";

		vdm.parse(aisSentence1);
		if (vdm.isCompletePacket()) {
			AisMessage message1 = AisMessage.getInstance(vdm);
			System.out.println(message1);
		} else {
			System.out.println("Partial message");
		}

		vdm.parse(aisSentence2);
		if (vdm.isCompletePacket()) {
			AisMessage message2 = AisMessage.getInstance(vdm);
			System.out.println(message2);
		} else {
			System.out.println("Partial message");
		}
	}


}