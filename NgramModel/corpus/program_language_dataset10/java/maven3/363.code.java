package org.apache.maven.lifecycle;
public class NoGoalSpecifiedException
    extends Exception
{
    public NoGoalSpecifiedException( String message )
    {
        super( message );
    }
}
