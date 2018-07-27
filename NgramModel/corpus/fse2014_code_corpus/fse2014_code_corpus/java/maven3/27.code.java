package org.apache.maven.artifact.repository;
public class Authentication
{
    private String privateKey;
    private String passphrase;
    public Authentication( String userName, String password )
    {
        this.username = userName;
        this.password = password;
    }
    private String username;
    private String password;
    public String getPassword()
    {
        return password;
    }
    public void setPassword( String password )
    {
        this.password = password;
    }
    public String getUsername()
    {
        return username;
    }
    public void setUsername( final String userName )
    {
        this.username = userName;
    }
    public String getPassphrase()
    {
        return passphrase;
    }
    public void setPassphrase( final String passphrase )
    {
        this.passphrase = passphrase;
    }
    public String getPrivateKey()
    {
        return privateKey;
    }
    public void setPrivateKey( final String privateKey )
    {
        this.privateKey = privateKey;
    }
}
