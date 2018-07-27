package org.apache.cassandra.utils;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
public class KeyPair {
  public static Map<String,String> generate() throws JSchException {
    com.jcraft.jsch.KeyPair pair = com.jcraft.jsch.KeyPair.genKeyPair(
        new JSch(),  com.jcraft.jsch.KeyPair.RSA);
    ByteArrayOutputStream publicKeyOut = new ByteArrayOutputStream();
    ByteArrayOutputStream privateKeyOut = new ByteArrayOutputStream();
    pair.writePublicKey(publicKeyOut, "whirr");
    pair.writePrivateKey(privateKeyOut);
    String publicKey = new String(publicKeyOut.toByteArray());
    String privateKey = new String(privateKeyOut.toByteArray());
    return ImmutableMap.<String, String> of("public", publicKey,
        "private", privateKey);
  }
}
