package org.apache.cassandra.cli;
import org.apache.cassandra.tools.NodeProbe;
import java.io.InputStream;
import java.io.PrintStream;
public class CliSessionState
{
    public String  hostName;      
    public int     thriftPort;    
    public boolean framed = true; 
    public boolean debug = false; 
    public String  username;      
    public String  password;      
    public String  keyspace;      
    public boolean batch = false; 
    public String  filename = ""; 
    public int     jmxPort = 8080;
    public InputStream in;
    public PrintStream out;
    public PrintStream err;
    public CliSessionState()
    {
        in = System.in;
        out = System.out;
        err = System.err;
    }
    public void setOut(PrintStream newOut)
    {
        this.out = newOut;   
    }
    public void setErr(PrintStream newErr)
    {
        this.err = newErr;
    }
    public boolean inFileMode()
    {
        return !this.filename.isEmpty();
    }
    public NodeProbe getNodeProbe()
    {
        try
        {
            return new NodeProbe(hostName, jmxPort);
        }
        catch (Exception e)
        {
            err.printf("WARNING: Could not connect to the JMX on %s:%d, information won't be shown.%n%n", hostName, jmxPort);
        }
        return null;
    }
}
