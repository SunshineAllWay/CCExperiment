package org.apache.tools.ant.types.selectors;
import java.io.File;
import org.apache.tools.ant.types.Comparison;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Parameter;
public class SizeSelector extends BaseExtendSelector {
    private static final int  KILO = 1000;
    private static final int  KIBI = 1024;
    private static final int  KIBI_POS = 4;
    private static final int  MEGA = 1000000;
    private static final int  MEGA_POS = 9;
    private static final int  MEBI = 1048576;
    private static final int  MEBI_POS = 13;
    private static final long GIGA = 1000000000L;
    private static final int  GIGA_POS = 18;
    private static final long GIBI = 1073741824L;
    private static final int  GIBI_POS = 22;
    private static final long TERA = 1000000000000L;
    private static final int  TERA_POS = 27;
    private static final long TEBI = 1099511627776L;
    private static final int  TEBI_POS = 31;
    private static final int  END_POS = 36;
    public static final String SIZE_KEY = "value";
    public static final String UNITS_KEY = "units";
    public static final String WHEN_KEY = "when";
    private long size = -1;
    private long multiplier = 1;
    private long sizelimit = -1;
    private Comparison when = Comparison.EQUAL;
    public SizeSelector() {
    }
    public String toString() {
        StringBuffer buf = new StringBuffer("{sizeselector value: ");
        buf.append(sizelimit);
        buf.append("compare: ").append(when.getValue());
        buf.append("}");
        return buf.toString();
    }
    public void setValue(long size) {
        this.size = size;
        if (multiplier != 0 && size > -1) {
            sizelimit = size * multiplier;
        }
    }
    public void setUnits(ByteUnits units) {
        int i = units.getIndex();
        multiplier = 0;
        if (i > -1 && i < KIBI_POS) {
            multiplier = KILO;
        } else if (i < MEGA_POS) {
            multiplier = KIBI;
        } else if (i < MEBI_POS) {
            multiplier = MEGA;
        } else if (i < GIGA_POS) {
            multiplier = MEBI;
        } else if (i < GIBI_POS) {
            multiplier = GIGA;
        } else if (i < TERA_POS) {
            multiplier = GIBI;
        } else if (i < TEBI_POS) {
            multiplier = TERA;
        } else if (i < END_POS) {
            multiplier = TEBI;
        }
        if (multiplier > 0 && size > -1) {
            sizelimit = size * multiplier;
        }
    }
    public void setWhen(SizeComparisons when) {
        this.when = when;
    }
    public void setParameters(Parameter[] parameters) {
        super.setParameters(parameters);
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                String paramname = parameters[i].getName();
                if (SIZE_KEY.equalsIgnoreCase(paramname)) {
                    try {
                        setValue(Long.parseLong(parameters[i].getValue()));
                    } catch (NumberFormatException nfe) {
                        setError("Invalid size setting "
                                + parameters[i].getValue());
                    }
                } else if (UNITS_KEY.equalsIgnoreCase(paramname)) {
                    ByteUnits units = new ByteUnits();
                    units.setValue(parameters[i].getValue());
                    setUnits(units);
                } else if (WHEN_KEY.equalsIgnoreCase(paramname)) {
                    SizeComparisons scmp = new SizeComparisons();
                    scmp.setValue(parameters[i].getValue());
                    setWhen(scmp);
                } else {
                    setError("Invalid parameter " + paramname);
                }
            }
        }
    }
    public void verifySettings() {
        if (size < 0) {
            setError("The value attribute is required, and must be positive");
        } else if (multiplier < 1) {
            setError("Invalid Units supplied, must be K,Ki,M,Mi,G,Gi,T,or Ti");
        } else if (sizelimit < 0) {
            setError("Internal error: Code is not setting sizelimit correctly");
        }
    }
    public boolean isSelected(File basedir, String filename, File file) {
        validate();
        if (file.isDirectory()) {
            return true;
        }
        long diff = file.length() - sizelimit;
        return when.evaluate(diff == 0 ? 0 : (int) (diff / Math.abs(diff)));
    }
    public static class ByteUnits extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[]{"K", "k", "kilo", "KILO",
                                "Ki", "KI", "ki", "kibi", "KIBI",
                                "M", "m", "mega", "MEGA",
                                "Mi", "MI", "mi", "mebi", "MEBI",
                                "G", "g", "giga", "GIGA",
                                "Gi", "GI", "gi", "gibi", "GIBI",
                                "T", "t", "tera", "TERA",
                 "Ti", "TI", "ti", "tebi", "TEBI"
            };
        }
    }
    public static class SizeComparisons extends Comparison {
    }
}
