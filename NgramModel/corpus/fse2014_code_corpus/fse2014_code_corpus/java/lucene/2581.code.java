package org.apache.solr.update.processor;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.solr.common.util.Hash;
public class Lookup3Signature extends Signature {
  protected long hash;
  public Lookup3Signature() {
  }
  public void add(String content) {
    hash = Hash.lookup3ycs64(content,0,content.length(),hash);
  }
  public byte[] getSignature() {
    return new byte[]{(byte)(hash>>56),(byte)(hash>>48),(byte)(hash>>40),(byte)(hash>>32),(byte)(hash>>24),(byte)(hash>>16),(byte)(hash>>8),(byte)(hash>>0)};
  }
}