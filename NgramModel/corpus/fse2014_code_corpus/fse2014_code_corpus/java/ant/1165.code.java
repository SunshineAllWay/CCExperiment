package org.apache.tools.ant.util.regexp;
public class JakartaOroRegexpTest extends RegexpTest {
    public Regexp getRegexpImplementation() {
        return new JakartaOroRegexp();
    }
    public JakartaOroRegexpTest(String name) {
        super(name);
    }
}
