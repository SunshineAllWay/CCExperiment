package org.apache.batik.test;
public class SimpleTestRunner {
    public static final String ERROR_CLASS_CAST =
        "Messages.SimpleTestRuner.error.class.cast";
    public static final String ERROR_CLASS_NOT_FOUND =
        "Messages.SimpleTestRuner.error.class.not.found";
    public static final String ERROR_INSTANTIATION =
        "Messages.SimpleTestRunner.error.instantiation";
    public static final String ERROR_ILLEGAL_ACCESS =
        "Messages.SimpleTestRunner.error.illegal.access";
    public static final String USAGE
        = "Messages.SimpleTestRunner.usage";
    public static void main(String[] args) throws Exception{
        if(args.length < 1){
            System.err.println(Messages.formatMessage(USAGE, null));
            System.exit(0);
        }
        String className = args[0];
        Class cl = null;
        try{
            cl = Class.forName(className);
        }catch(ClassNotFoundException e){
            System.err.println(Messages.formatMessage(ERROR_CLASS_NOT_FOUND,
                                                      new Object[]{className,
                                                      e.getClass().getName(),
                                                      e.getMessage()}));
            System.exit(0);
        }
        Test t = null;
        try{
            t = (Test)cl.newInstance();
        }catch(ClassCastException e){
            System.err.println(Messages.formatMessage(ERROR_CLASS_CAST,
                                                      new Object[]{ className,
                                                                    e.getClass().getName(),
                                                                    e.getMessage()
                                                      }));
            System.exit(0);
        }catch(InstantiationException e){
            System.err.println(Messages.formatMessage(ERROR_INSTANTIATION,
                                                      new Object[]{ className,
                                                                    e.getClass().getName(),
                                                                    e.getMessage() } ));
            System.exit(0);
        }catch(IllegalAccessException e){
            System.err.println(Messages.formatMessage(ERROR_ILLEGAL_ACCESS,
                                                      new Object[] { className,
                                                                     e.getClass().getName(),
                                                                     e.getMessage() }));
            System.exit(0);
        }
        TestReport tr = t.run();
        try{
            TestReportProcessor p
                = new org.apache.batik.test.xml.XMLTestReportProcessor();
            p.processReport(tr);
        }catch(TestException e){
            System.out.println(e.getClass().getName());
            System.out.println(e.getMessage());
            Exception source = e.getSourceError();
            if(source != null) {
                System.out.println(source);
                System.out.println(source.getMessage());
                source.printStackTrace();
            }
        }
        System.exit(1);
    }
}
