package org.apache.cassandra.concurrent;
public enum Stage
{
    READ,
    MUTATION,
    STREAM,
    GOSSIP,
    REQUEST_RESPONSE,
    ANTI_ENTROPY,
    MIGRATION,
    MISC,
    INTERNAL_RESPONSE,
    REPLICATE_ON_WRITE;
    public String getJmxType()
    {
        switch (this)
        {
            case ANTI_ENTROPY:
            case GOSSIP:
            case MIGRATION:
            case MISC:
            case STREAM:
            case INTERNAL_RESPONSE:
                return "internal";
            case MUTATION:
            case READ:
            case REQUEST_RESPONSE:
            case REPLICATE_ON_WRITE:
                return "request";
            default:
                throw new AssertionError("Unknown stage " + this);
        }
    }
    public String getJmxName()
    {
        String name = "";
        for (String word : toString().split("_"))
        {
            name += word.substring(0, 1) + word.substring(1).toLowerCase();
        }
        return name + "Stage";
    }
}
