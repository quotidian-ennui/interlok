/*
 * $RCSfile: MimeJunitHelper.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/11/12 10:26:54 $
 * $Author: lchan $
 */
package com.adaptris.core.services.mime;

import static com.adaptris.util.text.mime.MimeConstants.ENCODING_7BIT;
import static com.adaptris.util.text.mime.MimeConstants.ENCODING_8BIT;
import static com.adaptris.util.text.mime.MimeConstants.ENCODING_BASE64;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.text.mime.MultiPartOutput;

/**
 * @author lchan
 * @author $Author: lchan $
 */
public class MimeJunitHelper {

  public static final String PAYLOAD_1 = "The quick brown fox jumps over "
      + "the lazy dog";
  public static final String PAYLOAD_2 = "Sixty zippers were quickly picked "
      + "from the woven jute bag";
  public static final String PAYLOAD_3 = "Quick zephyrs blow, vexing daft Jim";

  public static final String PART1_CONTENT_ID = "part1";
  public static final String PART2_CONTENT_ID = "part2";
  public static final String PART3_CONTENT_ID = "part3";

  public static AdaptrisMessage create() throws Exception {
    MultiPartOutput output = new MultiPartOutput(new GuidGenerator().getUUID());
    output.getMimeHeader().addHeader("Subject", "This is the Subject");
    output.addPart(PAYLOAD_1, ENCODING_BASE64, PART1_CONTENT_ID);
    output.addPart(PAYLOAD_2, ENCODING_7BIT, PART2_CONTENT_ID);
    output.addPart(PAYLOAD_3, ENCODING_8BIT, PART3_CONTENT_ID);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(output.getBytes());
    msg.addMetadata(CoreConstants.MSG_MIME_ENCODED, "true");
    return msg;
  }
}
