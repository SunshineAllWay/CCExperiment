package org.apache.cassandra.config;
public class EncryptionOptions
{
    public InternodeEncryption internode_encryption = InternodeEncryption.none;
    public String keystore = "conf/.keystore";
    public String keystore_password = "cassandra";
    public String truststore = "conf/.truststore";
    public String truststore_password = "cassandra";
    public String[] cipherSuites = {"TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA"};
    public static enum InternodeEncryption
    {
        all,
        none
    }
}
