package org.apache.cassandra.security;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import org.apache.cassandra.config.EncryptionOptions;
public final class SSLFactory
{
    private static final String PROTOCOL = "TLS";
    private static final String ALGORITHM = "SunX509";
    private static final String STORE_TYPE = "JKS";
    public static SSLServerSocket getServerSocket(EncryptionOptions options, InetAddress address, int port) throws IOException
    {
        SSLContext ctx = createSSLContext(options);
        SSLServerSocket serverSocket = (SSLServerSocket)ctx.getServerSocketFactory().createServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.setEnabledCipherSuites(options.cipherSuites);
        serverSocket.bind(new InetSocketAddress(address, port), 100);
        return serverSocket;
    }
    public static SSLSocket getSocket(EncryptionOptions options, InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException
    {
        SSLContext ctx = createSSLContext(options);
        SSLSocket socket = (SSLSocket) ctx.getSocketFactory().createSocket(address, port, localAddress, localPort);
        socket.setEnabledCipherSuites(options.cipherSuites);
        return socket;
    }
    public static SSLSocket getSocket(EncryptionOptions options) throws IOException
    {
        SSLContext ctx = createSSLContext(options);
        SSLSocket socket = (SSLSocket) ctx.getSocketFactory().createSocket();
        socket.setEnabledCipherSuites(options.cipherSuites);
        return socket;
    }
    private static SSLContext createSSLContext(EncryptionOptions options) throws IOException {
        SSLContext ctx;
        try {
            ctx = SSLContext.getInstance(PROTOCOL);
            TrustManagerFactory tmf = null;
            KeyManagerFactory kmf = null;
            tmf = TrustManagerFactory.getInstance(ALGORITHM);
            KeyStore ts = KeyStore.getInstance(STORE_TYPE);
            ts.load(new FileInputStream(options.truststore), options.truststore_password.toCharArray());
            tmf.init(ts);
            kmf = KeyManagerFactory.getInstance(ALGORITHM);
            KeyStore ks = KeyStore.getInstance(STORE_TYPE);
            ks.load(new FileInputStream(options.keystore), options.keystore_password.toCharArray());
            kmf.init(ks, options.keystore_password.toCharArray());
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (Exception e) {
            throw new IOException("Error creating the initializing the SSL Context", e);
        }
        return ctx;
    }
}
