package org.apache.cassandra.tools;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import org.apache.commons.cli.*;
public class ClusterCmd {
    private static final String HOST_OPT_LONG = "host";
    private static final String HOST_OPT_SHORT = "h";
    private static final String PORT_OPT_LONG = "port";
    private static final String PORT_OPT_SHORT = "p";
    private static final int defaultPort = 8080;
    private static Options options = null;
    private CommandLine cmd = null;
    private NodeProbe probe;
    static
    {
        options = new Options();
        Option optHost = new Option(HOST_OPT_SHORT, HOST_OPT_LONG, true, "node hostname or ip address");
        optHost.setRequired(true);
        options.addOption(optHost);
        options.addOption(PORT_OPT_SHORT, PORT_OPT_LONG, true, "remote jmx agent port number (defaults to " + defaultPort + ")");
    }
    private ClusterCmd(String[] cmdArgs) throws ParseException, IOException, InterruptedException
    {
        parseArgs(cmdArgs);
        String host = cmd.getOptionValue(HOST_OPT_SHORT);
        String portNum = cmd.getOptionValue(PORT_OPT_SHORT);
        int port;
        if (portNum != null)
        {
            try
            {
                port = Integer.parseInt(portNum);
            }
            catch (NumberFormatException e)
            {
                throw new ParseException("Port must be a number");
            }
        }
        else
        {
            port = defaultPort;
        }
        probe = new NodeProbe(host, port);
    }
    public ClusterCmd(String host, int port) throws IOException, InterruptedException
    {
        probe = new NodeProbe(host, port);
    }
    public ClusterCmd(String host) throws IOException, InterruptedException
    {
        this(host, defaultPort);
    }
    private void parseArgs(String[] args) throws ParseException
    {
        CommandLineParser parser = new PosixParser();
        cmd = parser.parse(options, args);
    }
    private String[] getArgs()
    {
        return cmd.getArgs();
    }
    private static void printUsage()
    {
        HelpFormatter hf = new HelpFormatter();
        String header = String.format(
                "%nAvailable commands: get_endpoints [keyspace] [key], global_snapshot [name], clear_global_snapshot," +
                "truncate <keyspace> <cfname>");
        String usage = String.format("java %s -host <arg> <command>%n", ClusterCmd.class.getName());
        hf.printHelp(usage, "", options, header);
    }
    public void close() throws IOException
    {
        probe.close(); 
    }
    public void printEndpoints(String keyspace, String key)
    {
        List<InetAddress> endpoints = probe.getEndpoints(keyspace, key);
        System.out.println(String.format("%-17s: %s", "Key", key));
        System.out.println(String.format("%-17s: %s", "Endpoints", endpoints));
    }
    public void takeGlobalSnapshot(String snapshotName) throws IOException, InterruptedException
    {
        for (String liveNode : probe.getLiveNodes())
        {
            try
            {
                NodeProbe hostProbe = new NodeProbe(liveNode, probe.port);
                hostProbe.takeSnapshot(snapshotName);
                System.out.println(liveNode + " snapshot taken");
            }
            catch (IOException e)
            {
                System.out.println(liveNode + " snapshot FAILED: " + e.getMessage());
            }
        }
    }
    public void clearGlobalSnapshot() throws IOException, InterruptedException
    {
        for (String liveNode : probe.getLiveNodes())
        {
            try
            {
                NodeProbe hostProbe = new NodeProbe(liveNode, probe.port);
                hostProbe.clearSnapshot();
                System.out.println(liveNode + " snapshot cleared");
            }
            catch (IOException e)
            {
                System.out.println(liveNode + " snapshot clear FAILED: " + e.getMessage());
            }
        }
    }
    public void truncate(String tableName, String cfName)
    {
        probe.truncate(tableName, cfName);
    }
    public static void main(String[] args) throws IOException, InterruptedException
    {
        ClusterCmd clusterCmd = null;
        try
        {
            clusterCmd = new ClusterCmd(args);
        }
        catch (ParseException pe)
        {
            System.err.println(pe.getMessage());
            ClusterCmd.printUsage();
            System.exit(1);
        }
        catch (IOException ioe)
        {
            System.err.println("Error connecting to remote JMX agent!");
            ioe.printStackTrace();
            System.exit(3);
        }
        if (clusterCmd.getArgs().length < 1)
        {
            System.err.println("Missing argument for command.");
            ClusterCmd.printUsage();
            System.exit(1);
        }
        String[] arguments = clusterCmd.getArgs();
        String cmdName = arguments[0];
        if (cmdName.equals("get_endpoints"))
        {
            if (arguments.length <= 2)
            {
                System.err.println("missing keyspace and/or key argument");
                ClusterCmd.printUsage();
                System.exit(1);
            }
            clusterCmd.printEndpoints(arguments[1], arguments[2]);
        }
        else if (cmdName.equals("global_snapshot"))
        {
            String snapshotName = "";
            if (arguments.length > 1)
            {
                snapshotName = arguments[1];
            }
            clusterCmd.takeGlobalSnapshot(snapshotName);
        }
        else if (cmdName.equals("clear_global_snapshot"))
        {
            clusterCmd.clearGlobalSnapshot();
        }
        else if (cmdName.equals("truncate"))
        {
            if (arguments.length != 3)
            {
                System.err.println("truncate requires <keyspace> and <columnfamily> arguments");
            }
            String tableName = arguments[1];
            String cfName = arguments[2];
            clusterCmd.truncate(tableName, cfName);
        }
        else
        {
            System.err.println("Unrecognized command: " + cmdName + ".");
            ClusterCmd.printUsage();
            System.exit(1);
        }
        System.exit(0);
    }
}
