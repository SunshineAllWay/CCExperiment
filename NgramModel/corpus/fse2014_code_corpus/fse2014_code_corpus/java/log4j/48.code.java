package examples.lf5.UsingSocketAppenders;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.io.IOException;
import java.net.URL;
public class UsingSocketAppenders {
    private static Logger logger1 =
            Logger.getLogger(UsingSocketAppenders.class);
    private static Logger logger2 =
            Logger.getLogger("TestClass.Subclass");
    private static Logger logger3 =
            Logger.getLogger("TestClass.Subclass.Subclass");
    public static void main(String argv[]) {
        String resource =
                "/examples/lf5/UsingSocketAppenders/socketclient.properties";
        URL configFileResource =
                UsingSocketAppenders.class.getResource(resource);
        PropertyConfigurator.configure(configFileResource);
        logger1.debug("Hello, my name is Homer Simpson.");
        logger1.debug("Hello, my name is Lisa Simpson.");
        logger2.debug("Hello, my name is Marge Simpson.");
        logger2.debug("Hello, my name is Bart Simpson.");
        logger3.debug("Hello, my name is Maggie Simpson.");
        logger2.info("We are the Simpsons!");
        logger2.info("Mmmmmm .... Chocolate.");
        logger3.info("Homer likes chocolate");
        logger3.info("Doh!");
        logger3.info("We are the Simpsons!");
        logger1.warn("Bart: I am through with working! Working is for chumps!" +
                "Homer: Son, I'm proud of you. I was twice your age before " +
                "I figured that out.");
        logger1.warn("Mmm...forbidden donut.");
        logger1.warn("D'oh! A deer! A female deer!");
        logger1.warn("Truly, yours is a butt that won't quit." +
                "- Bart, writing as Woodrow to Ms. Krabappel.");
        logger2.error("Dear Baby, Welcome to Dumpsville. Population: you.");
        logger2.error("Dear Baby, Welcome to Dumpsville. Population: you.",
                new IOException("Dumpsville, USA"));
        logger3.error("Mr. Hutz, are you aware you're not wearing pants?");
        logger3.error("Mr. Hutz, are you aware you're not wearing pants?",
                new IllegalStateException("Error !!"));
        logger3.fatal("Eep.");
        logger3.fatal("Mmm...forbidden donut.",
                new SecurityException("Fatal Exception ... "));
        logger3.fatal("D'oh! A deer! A female deer!");
        logger2.fatal("Mmmmmm .... Chocolate.",
                new SecurityException("Fatal Exception"));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
        }
    }
}
