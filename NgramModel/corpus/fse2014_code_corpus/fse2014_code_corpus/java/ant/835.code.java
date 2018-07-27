package org.apache.tools.ant.util.java15;
import org.apache.tools.ant.BuildException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.util.List;
import java.util.Iterator;
public class ProxyDiagnostics {
    private String destination;
    private URI destURI;
    public static final String DEFAULT_DESTINATION = "http://ant.apache.org/";
    public ProxyDiagnostics(String destination) {
        this.destination = destination;
        try {
            this.destURI = new URI(destination);
        } catch (URISyntaxException e) {
            throw new BuildException(e);
        }
    }
    public ProxyDiagnostics() {
        this(DEFAULT_DESTINATION);
    }
    public String toString() {
        ProxySelector selector = ProxySelector.getDefault();
        List list = selector.select(destURI);
        StringBuffer result = new StringBuffer();
        Iterator proxies = list.listIterator();
        while (proxies.hasNext()) {
            Proxy proxy = (Proxy) proxies.next();
            SocketAddress address = proxy.address();
            if (address == null) {
                result.append("Direct connection\n");
            } else {
                result.append(proxy.toString());
                if (address instanceof InetSocketAddress) {
                    InetSocketAddress ina = (InetSocketAddress) address;
                    result.append(' ');
                    result.append(ina.getHostName());
                    result.append(':');
                    result.append(ina.getPort());
                    if (ina.isUnresolved()) {
                        result.append(" [unresolved]");
                    } else {
                        InetAddress addr = ina.getAddress();
                        result.append(" [");
                        result.append(addr.getHostAddress());
                        result.append(']');
                    }
                }
                result.append('\n');
            }
        }
        return result.toString();
    }
}