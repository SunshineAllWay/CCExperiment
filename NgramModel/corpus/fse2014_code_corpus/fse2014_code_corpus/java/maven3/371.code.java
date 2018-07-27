package org.apache.maven.lifecycle.internal;
class CurrentPhaseForThread
{
    private static final InheritableThreadLocal<String> threadPhase = new InheritableThreadLocal<String>();
    public static void setPhase( String phase )
    {
        threadPhase.set( phase );
    }
    public static boolean isPhase( String phase )
    {
        return phase.equals( threadPhase.get() );
    }
}
