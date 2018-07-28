package org.apache.batik.util;
import org.apache.batik.test.*;
public class ApplicationSecurityEnforcerTest extends DefaultTestSuite {
    static final Class APP_MAIN_CLASS = org.apache.batik.apps.svgbrowser.Main.class;
    static final String APP_SECURITY_POLICY = "org/apache/batik/apps/svgbrowser/resources/svgbrowser.policy";
    public ApplicationSecurityEnforcerTest(){
        addTest(new CheckNoSecurityManagerOverride());
        addTest(new CheckSecurityEnforcement());
        addTest(new CheckSecurityRemoval());
        addTest(new CheckNoPolicyFile());
    }
    static ApplicationSecurityEnforcer buildTestTarget(){
        return new ApplicationSecurityEnforcer(APP_MAIN_CLASS,
                                               APP_SECURITY_POLICY);
    }
    static class CheckNoSecurityManagerOverride extends AbstractTest {
        public boolean runImplBasic(){
            ApplicationSecurityEnforcer aseA
                = buildTestTarget();
            aseA.enforceSecurity(true);
            ApplicationSecurityEnforcer aseB
                = buildTestTarget();
            boolean passed = false;
            try {
                aseB.enforceSecurity(true);
            } catch (SecurityException se){
                System.out.println(">>>>>>>>>>>>> got expected SecurityException A");
                try {
                    System.out.println(">>>>>>>>>>>>> got expected SecurityException B");
                    aseB.enforceSecurity(false);
                } catch (SecurityException se2){
                    passed = true;
                }
            }
            aseA.enforceSecurity(false);
            return passed;
        }
    }
    static class CheckSecurityEnforcement extends AbstractTest {
        public boolean runImplBasic() {
            ApplicationSecurityEnforcer ase = buildTestTarget();
            try {
                ase.enforceSecurity(true);
                SecurityManager sm = System.getSecurityManager();
                if (sm == ase.lastSecurityManagerInstalled){
                    return true;
                }
            } finally {
                System.setSecurityManager(null);
            }
            return false;
        }
    }
    static class CheckSecurityRemoval extends AbstractTest {
        public boolean runImplBasic() {
            ApplicationSecurityEnforcer ase = buildTestTarget();
            try {
                ase.enforceSecurity(true);
                ase.enforceSecurity(false);
                SecurityManager sm = System.getSecurityManager();
                if (sm == null && ase.lastSecurityManagerInstalled == null) {
                    return true;
                }
            } finally {
                System.setSecurityManager(null);
            }
            return false;
        }
    }
    static class CheckNoPolicyFile extends AbstractTest {
        public boolean runImplBasic() {
            ApplicationSecurityEnforcer ase =
                new ApplicationSecurityEnforcer(APP_MAIN_CLASS,
                                                "dont.exist.policy");
            try {
                ase.enforceSecurity(true);
            } catch (NullPointerException se) {
                return true;
            } finally {
                ase.enforceSecurity(false);
            }
            return false;
        }
    }
}
