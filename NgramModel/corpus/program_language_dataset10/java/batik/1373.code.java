package org.apache.batik.util;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class ClassFileUtilities {
    public static final byte CONSTANT_UTF8_INFO                = 1;
    public static final byte CONSTANT_INTEGER_INFO             = 3;
    public static final byte CONSTANT_FLOAT_INFO               = 4;
    public static final byte CONSTANT_LONG_INFO                = 5;
    public static final byte CONSTANT_DOUBLE_INFO              = 6;
    public static final byte CONSTANT_CLASS_INFO               = 7;
    public static final byte CONSTANT_STRING_INFO              = 8;
    public static final byte CONSTANT_FIELDREF_INFO            = 9;
    public static final byte CONSTANT_METHODREF_INFO           = 10;
    public static final byte CONSTANT_INTERFACEMETHODREF_INFO  = 11;
    public static final byte CONSTANT_NAMEANDTYPE_INFO         = 12;
    protected ClassFileUtilities() {
    }
    public static void main(String[] args) {
        boolean showFiles = false;
        if (args.length == 1 && args[0].equals("-f")) {
            showFiles = true;
        } else if (args.length != 0) {
            System.err.println("usage: org.apache.batik.util.ClassFileUtilities [-f]");
            System.err.println();
            System.err.println("  -f    list files that cause each jar file dependency");
            System.exit(1);
        }
        File cwd = new File(".");
        File buildDir = null;
        String[] cwdFiles = cwd.list();
        for (int i = 0; i < cwdFiles.length; i++) {
            if (cwdFiles[i].startsWith("batik-")) {
                buildDir = new File(cwdFiles[i]);
                if (!buildDir.isDirectory()) {
                    buildDir = null;
                } else {
                    break;
                }
            }
        }
        if (buildDir == null || !buildDir.isDirectory()) {
            System.out.println("Directory 'batik-xxx' not found in current directory!");
            return;
        }
        try {
            Map cs = new HashMap();
            Map js = new HashMap();
            collectJars(buildDir, js, cs);
            Set classpath = new HashSet();
            Iterator i = js.values().iterator();
            while (i.hasNext()) {
                classpath.add(((Jar) i.next()).jarFile);
            }
            i = cs.values().iterator();
            while (i.hasNext()) {
                ClassFile fromFile = (ClassFile) i.next();
                Set result = getClassDependencies(fromFile.getInputStream(),
                                                  classpath, false);
                Iterator j = result.iterator();
                while (j.hasNext()) {
                    ClassFile toFile = (ClassFile) cs.get(j.next());
                    if (fromFile != toFile && toFile != null) {
                        fromFile.deps.add(toFile);
                    }
                }
            }
            i = cs.values().iterator();
            while (i.hasNext()) {
                ClassFile fromFile = (ClassFile) i.next();
                Iterator j = fromFile.deps.iterator();
                while (j.hasNext()) {
                    ClassFile toFile = (ClassFile) j.next();
                    Jar fromJar = fromFile.jar;
                    Jar toJar = toFile.jar;
                    if (fromFile.name.equals(toFile.name)
                            || toJar == fromJar
                            || fromJar.files.contains(toFile.name)) {
                        continue;
                    }
                    Integer n = (Integer) fromJar.deps.get(toJar);
                    if (n == null) {
                        fromJar.deps.put(toJar, new Integer(1));
                    } else {
                        fromJar.deps.put(toJar, new Integer(n.intValue() + 1));
                    }
                }
            }
            List triples = new ArrayList(10);
            i = js.values().iterator();
            while (i.hasNext()) {
                Jar fromJar = (Jar) i.next();
                Iterator j = fromJar.deps.keySet().iterator();
                while (j.hasNext()) {
                    Jar toJar = (Jar) j.next();
                    Triple t = new Triple();
                    t.from = fromJar;
                    t.to = toJar;
                    t.count = ((Integer) fromJar.deps.get(toJar)).intValue();
                    triples.add(t);
                }
            }
            Collections.sort(triples);
            i = triples.iterator();
            while (i.hasNext()) {
                Triple t = (Triple) i.next();
                System.out.println
                    (t.count + "," + t.from.name + "," + t.to.name);
                if (showFiles) {
                    Iterator j = t.from.files.iterator();
                    while (j.hasNext()) {
                        ClassFile fromFile = (ClassFile) j.next();
                        Iterator k = fromFile.deps.iterator();
                        while (k.hasNext()) {
                            ClassFile toFile = (ClassFile) k.next();
                            if (toFile.jar == t.to
                                    && !t.from.files.contains(toFile.name)) {
                                System.out.println
                                    ("\t" + fromFile.name + " --> "
                                          + toFile.name);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected static class ClassFile {
        public String name;
        public List deps = new ArrayList(10);
        public Jar jar;
        public InputStream getInputStream() throws IOException {
            return jar.jarFile.getInputStream(jar.jarFile.getEntry(name));
        }
    }
    protected static class Jar {
        public String name;
        public File file;
        public JarFile jarFile;
        public Map deps = new HashMap();
        public Set files = new HashSet();
    }
    protected static class Triple implements Comparable {
        public Jar from;
        public Jar to;
        public int count;
        public int compareTo(Object o) {
            return ((Triple) o).count - count;
        }
    }
    private static void collectJars(File dir, Map jars, Map classFiles) throws IOException {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String n = files[i].getName();
            if (n.endsWith(".jar") && files[i].isFile()) {
                Jar j = new Jar();
                j.name = files[i].getPath();
                j.file = files[i];
                j.jarFile = new JarFile(files[i]);
                jars.put(j.name, j);
                Enumeration entries = j.jarFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry ze = (ZipEntry) entries.nextElement();
                    String name = ze.getName();
                    if (name.endsWith(".class")) {
                        ClassFile cf = new ClassFile();
                        cf.name = name;
                        cf.jar = j;
                        classFiles.put(j.name + '!' + cf.name, cf);
                        j.files.add(cf);
                    }
                }
            } else if (files[i].isDirectory()) {
                collectJars(files[i], jars, classFiles);
            }
        }
    }
    public static Set getClassDependencies(String path,
                                           Set classpath,
                                           boolean rec)
            throws IOException {
        return getClassDependencies(new FileInputStream(path), classpath, rec);
    }
    public static Set getClassDependencies(InputStream is,
                                           Set classpath,
                                           boolean rec)
            throws IOException {
        Set result = new HashSet();
        Set done = new HashSet();
        computeClassDependencies(is, classpath, done, result, rec);
        return result;
    }
    private static void computeClassDependencies(InputStream is,
                                                 Set classpath,
                                                 Set done,
                                                 Set result,
                                                 boolean rec)
            throws IOException {
        Iterator it = getClassDependencies(is).iterator();
        while (it.hasNext()) {
            String s = (String)it.next();
            if (!done.contains(s)) {
                done.add(s);
                Iterator cpit = classpath.iterator();
                while (cpit.hasNext()) {
                    InputStream depis = null;
                    String path = null;
                    Object cpEntry = cpit.next();
                    if (cpEntry instanceof JarFile) {
                        JarFile jarFile = (JarFile) cpEntry;
                        String classFileName = s + ".class";
                        ZipEntry ze = jarFile.getEntry(classFileName);
                        if (ze != null) {
                            path = jarFile.getName() + '!' + classFileName;
                            depis = jarFile.getInputStream(ze);
                        }
                    } else {
                        path = ((String) cpEntry) + '/' + s + ".class";
                        File f = new File(path);
                        if (f.isFile()) {
                            depis = new FileInputStream(f);
                        }
                    }
                    if (depis != null) {
                        result.add(path);
                        if (rec) {
                            computeClassDependencies
                                (depis, classpath, done, result, rec);
                        }
                    }
                }
            }
        }
    }
    public static Set getClassDependencies(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        if (dis.readInt() != 0xcafebabe) {
            throw new IOException("Invalid classfile");
        }
        dis.readInt();
        int len = dis.readShort();
        String[] strs = new String[len];
        Set classes = new HashSet();
        Set desc = new HashSet();
        for (int i = 1; i < len; i++) {
            int constCode = dis.readByte() & 0xff;
            switch ( constCode ) {
            case CONSTANT_LONG_INFO:
            case CONSTANT_DOUBLE_INFO:
                dis.readLong();
                i++;
                break;
            case CONSTANT_FIELDREF_INFO:
            case CONSTANT_METHODREF_INFO:
            case CONSTANT_INTERFACEMETHODREF_INFO:
            case CONSTANT_INTEGER_INFO:
            case CONSTANT_FLOAT_INFO:
                dis.readInt();
                break;
            case CONSTANT_CLASS_INFO:
                classes.add(new Integer(dis.readShort() & 0xffff));
                break;
            case CONSTANT_STRING_INFO:
                dis.readShort();
                break;
            case CONSTANT_NAMEANDTYPE_INFO:
                dis.readShort();
                desc.add(new Integer(dis.readShort() & 0xffff));
                break;
            case CONSTANT_UTF8_INFO:
                strs[i] = dis.readUTF();
                break;
            default:
                throw new RuntimeException("unexpected data in constant-pool:" + constCode );
            }
        }
        Set result = new HashSet();
        Iterator it = classes.iterator();
        while (it.hasNext()) {
            result.add(strs[((Integer)it.next()).intValue()]);
        }
        it = desc.iterator();
        while (it.hasNext()) {
            result.addAll(getDescriptorClasses(strs[((Integer)it.next()).intValue()]));
        }
        return result;
    }
    protected static Set getDescriptorClasses(String desc) {
        Set result = new HashSet();
        int  i = 0;
        char c = desc.charAt(i);
        switch (c) {
        case '(':
            loop: for (;;) {
                c = desc.charAt(++i);
                switch (c) {
                case '[':
                    do {
                        c = desc.charAt(++i);
                    } while (c == '[');
                    if (c != 'L') {
                        break;
                    }
                case 'L':
                    c = desc.charAt(++i);
                    StringBuffer sb = new StringBuffer();
                    while (c != ';') {
                        sb.append(c);
                        c = desc.charAt(++i);
                    }
                    result.add(sb.toString());
                    break;
                default:
                    break;
                case ')':
                    break loop;
                }
            }
            c = desc.charAt(++i);
            switch (c) {
            case '[':
                do {
                    c = desc.charAt(++i);
                } while (c == '[');
                if (c != 'L') {
                    break;
                }
            case 'L':
                c = desc.charAt(++i);
                StringBuffer sb = new StringBuffer();
                while (c != ';') {
                    sb.append(c);
                    c = desc.charAt(++i);
                }
                result.add(sb.toString());
                break;
            default:
            case 'V':
            }
            break;
        case '[':
            do {
                c = desc.charAt(++i);
            } while (c == '[');
            if (c != 'L') {
                break;
            }
        case 'L':
            c = desc.charAt(++i);
            StringBuffer sb = new StringBuffer();
            while (c != ';') {
                sb.append(c);
                c = desc.charAt(++i);
            }
            result.add(sb.toString());
            break;
        default:
        }
        return result;
    }
}
