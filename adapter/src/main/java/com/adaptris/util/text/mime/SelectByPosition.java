package com.adaptris.util.text.mime;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.validation.constraints.Min;

import com.adaptris.annotation.AutoPopulated;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Selects a MimeBodyPart based on its position within the Multipart.
 * 
 * @config mime-select-by-position
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("mime-select-by-position")
public class SelectByPosition implements PartSelector {

  @AutoPopulated
  @Min(0)
  private int position;

  public SelectByPosition() {
    setPosition(0);
  }

  public SelectByPosition(int i) {
    this();
    setPosition(i);
  }

  /**
   *
   * @see PartSelector#select(MultiPartInput)
   */
  @Override
  public MimeBodyPart select(MultiPartInput m) throws MessagingException {
    return m.getBodyPart(getPosition());
  }

  @Override
  public List<MimeBodyPart> select(MimeMultipart in) throws MessagingException {
    ArrayList<MimeBodyPart> list = new ArrayList<MimeBodyPart>();
    list.add((MimeBodyPart)in.getBodyPart(getPosition()));
    return list;
  }


  /**
   * @return the position
   */
  public int getPosition() {
    return position;
  }

  /**
   * The position of the MimeBodyPart to select within the multi part.
   *
   * @param i the position to select, starting from 0
   */
  public void setPosition(int i) {
    position = i;
  }

}
