package org.apache.maven.model;
import junit.framework.TestCase;
public class MailingListTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new MailingList().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new MailingList().equals( null ) );
        new MailingList().equals( new MailingList() );
    }
    public void testEqualsIdentity()
    {
        MailingList thing = new MailingList();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new MailingList().toString() );
    }
}
