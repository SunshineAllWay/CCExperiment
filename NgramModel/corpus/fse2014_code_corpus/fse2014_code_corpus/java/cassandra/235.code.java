package org.apache.cassandra.dht;
public class StringToken extends Token<String>
{
    public StringToken(String token)
    {
        super(token);
    }
    @Override
    public int compareTo(Token<String> o)
    {
        return token.compareTo(o.token);
    }
}
