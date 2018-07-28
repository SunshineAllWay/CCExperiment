package org.apache.cassandra.contrib.stress.util;
import org.apache.cassandra.contrib.stress.Session;
import org.apache.cassandra.contrib.stress.Stress;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.InvalidRequestException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
public abstract class OperationThread extends Thread
{
    public final int index;
    protected final Session session;
    protected final Cassandra.Client client;
    protected final Range range;
    protected Double nextGaussian = null;
    public OperationThread(int idx)
    {
        index = idx;
        session = Stress.session;
        int keysPerThread = session.getKeysPerThread();
        range = new Range((int) (keysPerThread * (idx + session.getSkipKeys())), keysPerThread * (idx + 1));
        client = session.getClient();
    }
    protected List<String> generateValues()
    {
        List<String> values = new ArrayList<String>();
        for (int i = 0; i < session.getCardinality(); i++)
        {
            String hash = getMD5(Integer.toString(i));
            int times = session.getColumnSize() / hash.length();
            int sumReminder = session.getColumnSize() % hash.length();
            values.add(new StringBuilder(multiplyString(hash, times)).append(hash.substring(0, sumReminder)).toString());
        }
        return values;
    }
    protected byte[] generateKey()
    {
        return (session.useRandomGenerator()) ? generateRandomKey() : generateGaussKey();
    }
    private byte[] generateRandomKey()
    {
        String format = "%0" + session.getTotalKeysLength() + "d";
        return String.format(format, Stress.randomizer.nextInt(session.getNumKeys() - 1)).getBytes();
    }
    private byte[] generateGaussKey()
    {
        String format = "%0" + session.getTotalKeysLength() + "d";
        for (;;)
        {
            double token = nextGaussian(session.getMean(), session.getSigma());
            if (0 <= token && token < session.getNumKeys())
            {
                return String.format(format, (int) token).getBytes();
            }
        }
    }
    private double nextGaussian(int mu, float sigma)
    {
        Random random = Stress.randomizer;
        Double currentState = nextGaussian;
        if (currentState == null)
        {
            double x2pi  = random.nextDouble() * 2 * Math.PI;
            double g2rad = Math.sqrt(-2.0 * Math.log(1.0 - random.nextDouble()));
            currentState = Math.cos(x2pi) * g2rad;
            nextGaussian = Math.sin(x2pi) * g2rad;
        }
        return mu + currentState * sigma;
    }
    private String getMD5(String input)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hash = new StringBuilder(new BigInteger(1, messageDigest).toString(16));
            while (hash.length() < 32)
                hash.append("0").append(hash);
            return hash.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
    private String multiplyString(String str, int times)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < times; i++)
            result.append(str);
        return result.toString();
    }
    protected String getExceptionMessage(Exception e)
    {
        String className = e.getClass().getSimpleName();
        String message = (e instanceof InvalidRequestException) ? ((InvalidRequestException) e).getWhy() : e.getMessage();
        return (message == null) ? "(" + className + ")" : String.format("(%s): %s", className, message);
    }
}
