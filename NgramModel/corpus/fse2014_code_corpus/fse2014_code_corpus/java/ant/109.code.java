package org.apache.tools.ant.filters;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Parameter;
public final class SortFilter extends BaseParamFilterReader
    implements ChainableReader {
    private static final String REVERSE_KEY = "reverse";
    private static final String COMPARATOR_KEY = "comparator";
    private Comparator comparator = null;
    private boolean reverse;
    private List lines;
    private String line = null;
    private Iterator iterator = null;
    public SortFilter() {
        super();
    }
    public SortFilter(final Reader in) {
        super(in);
    }
    public int read() throws IOException {
        if (!getInitialized()) {
            initialize();
            setInitialized(true);
        }
        int ch = -1;
        if (line != null) {
            ch = line.charAt(0);
            if (line.length() == 1) {
                line = null;
            } else {
                line = line.substring(1);
            }
        } else {
            if (lines == null) {
                lines = new ArrayList();
                for (line = readLine(); line != null; line = readLine()) {
                    lines.add(line);
                }
                sort();
                iterator = lines.iterator();
            }
            if (iterator.hasNext()) {
                line = (String) iterator.next();
            } else {
                line = null;
                lines = null;
                iterator = null;
            }
            if (line != null) {
                return read();
            }
        }
        return ch;
    }
    public Reader chain(final Reader rdr) {
        SortFilter newFilter = new SortFilter(rdr);
        newFilter.setReverse(isReverse());
        newFilter.setComparator(getComparator());
        newFilter.setInitialized(true);
        return newFilter;
    }
    public boolean isReverse() {
        return reverse;
    }
    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }
    public Comparator getComparator() {
        return comparator;
    }
    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
    }
    public void add(Comparator comparator) {
        if (this.comparator != null && comparator != null) {
            throw new BuildException("can't have more than one comparator");
        }
        setComparator(comparator);
    }
    private void initialize() throws IOException {
        Parameter[] params = getParameters();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                final String paramName = params[i].getName();
                if (REVERSE_KEY.equals(paramName)) {
                    setReverse(Boolean.valueOf(params[i].getValue())
                               .booleanValue());
                    continue;
                }
                if (COMPARATOR_KEY.equals(paramName)) {
                    try {
                        String className = (String) params[i].getValue();
                        setComparator((Comparator) (Class.forName(className)
                                                    .newInstance()));
                        continue;
                    } catch (InstantiationException e) {
                        throw new BuildException(e);
                    } catch (IllegalAccessException e) {
                        throw new BuildException(e);
                    } catch (ClassNotFoundException e) {
                        throw new BuildException(e);
                    } catch (ClassCastException e) {
                        throw new BuildException("Value of comparator attribute"
                                                 + " should implement"
                                                 + " java.util.Comparator"
                                                 + " interface");
                    } catch (Exception e) {
                        throw new BuildException(e);
                    }
                }
            }
        }
    }
    private void sort() {
        if (comparator == null) {
            if (reverse) {
                Collections.sort(lines, new Comparator() {
                        public int compare(Object o1, Object o2) {
                            String s1 = (String) o1;
                            String s2 = (String) o2;
                            return (-s1.compareTo(s2));
                        }
                    });
            } else {
                Collections.sort(lines);
            }
        } else {
            Collections.sort(lines, comparator);
        }
    }
}
