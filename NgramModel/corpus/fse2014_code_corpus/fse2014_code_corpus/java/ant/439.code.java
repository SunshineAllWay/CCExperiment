package org.apache.tools.ant.taskdefs.optional.extension;
import java.io.File;
import java.text.ParseException;
import java.util.jar.Manifest;
import org.apache.tools.ant.BuildException;
class LibraryDisplayer {
    void displayLibrary(final File file)
        throws BuildException {
        final Manifest manifest = ExtensionUtil.getManifest(file);
        displayLibrary(file, manifest);
    }
    void displayLibrary(final File file,
                         final Manifest manifest)
        throws BuildException {
        final Extension[] available = Extension.getAvailable(manifest);
        final Extension[] required = Extension.getRequired(manifest);
        final Extension[] options = Extension.getOptions(manifest);
        final Specification[] specifications = getSpecifications(manifest);
        if (0 == available.length && 0 == required.length && 0 == options.length
            && 0 == specifications.length) {
            return;
        }
        final String message = "File: " + file;
        final int size = message.length();
        printLine(size);
        System.out.println(message);
        printLine(size);
        if (0 != available.length) {
            System.out.println("Extensions Supported By Library:");
            for (int i = 0; i < available.length; i++) {
                final Extension extension = available[ i ];
                System.out.println(extension.toString());
            }
        }
        if (0 != required.length) {
            System.out.println("Extensions Required By Library:");
            for (int i = 0; i < required.length; i++) {
                final Extension extension = required[ i ];
                System.out.println(extension.toString());
            }
        }
        if (0 != options.length) {
            System.out.println("Extensions that will be used by Library if present:");
            for (int i = 0; i < options.length; i++) {
                final Extension extension = options[ i ];
                System.out.println(extension.toString());
            }
        }
        if (0 != specifications.length) {
            System.out.println("Specifications Supported By Library:");
            for (int i = 0; i < specifications.length; i++) {
                final Specification specification = specifications[ i ];
                displaySpecification(specification);
            }
        }
    }
    private void printLine(final int size) {
        for (int i = 0; i < size; i++) {
            System.out.print("-");
        }
        System.out.println();
    }
    private Specification[] getSpecifications(final Manifest manifest)
        throws BuildException {
        try {
            return Specification.getSpecifications(manifest);
        } catch (final ParseException pe) {
            throw new BuildException(pe.getMessage(), pe);
        }
    }
    private void displaySpecification(final Specification specification) {
        final String[] sections = specification.getSections();
        if (null != sections) {
            final StringBuffer sb = new StringBuffer("Sections: ");
            for (int i = 0; i < sections.length; i++) {
                sb.append(" ");
                sb.append(sections[ i ]);
            }
            System.out.println(sb);
        }
        System.out.println(specification.toString());
    }
}
