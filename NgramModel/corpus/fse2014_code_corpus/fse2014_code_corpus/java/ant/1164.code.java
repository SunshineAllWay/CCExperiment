package org.apache.tools.ant.util.regexp;
public class JakartaOroMatcherTest extends RegexpMatcherTest {
    public RegexpMatcher getImplementation() {
        return new JakartaOroMatcher();
    }
    public JakartaOroMatcherTest(String name) {
        super(name);
    }
}
