package org.apache.tools.ant.taskdefs.email;
import junit.framework.TestCase;
public class EmailAddressTest extends TestCase {
    public EmailAddressTest(String name) {
        super(name);
    }
    public void setUp() {
    }
    public void test1() {
        expectNameAddress( new EmailAddress("address (name)") );
    }
    public void test2() {
        expectNameAddress( new EmailAddress("(name) address") );
    }
    public void test3() {
        expectNameAddress( new EmailAddress("name <address>") );
    }
    public void test4() {
        expectNameAddress( new EmailAddress("<address> name") );
    }
    public void test5() {
        expectNameAddress( new EmailAddress("<address> (name)") );
    }
    public void test6() {
        expectNameAddress( new EmailAddress("(name) <address>") );
    }
    public void test7() {
        expectNameAddress2( new EmailAddress("address (<name>)") );
    }
    public void test8() {
        expectNameAddress2( new EmailAddress("(<name>) address") );
    }
    public void test9() {
        expectNameAddress3( new EmailAddress("address") );
    }
    public void testA() {
        expectNameAddress3( new EmailAddress("<address>") );
    }
    public void testB() {
        expectNameAddress3( new EmailAddress(" <address> ") );
    }
    public void testC() {
        expectNameAddress3( new EmailAddress("< address >") );
    }
    public void testD() {
        expectNameAddress3( new EmailAddress(" < address > ") );
    }
    private void expectNameAddress(EmailAddress e) {
        assertEquals( "name", e.getName() );
        assertEquals( "address", e.getAddress() );
    }
    private void expectNameAddress2(EmailAddress e) {
        assertEquals( "<name>", e.getName() );
        assertEquals( "address", e.getAddress() );
    }
    private void expectNameAddress3(EmailAddress e) {
        assertTrue( "Expected null, found <" + e.getName() + ">",
            e.getName() == null );
        assertEquals( "address", e.getAddress() );
    }
}
