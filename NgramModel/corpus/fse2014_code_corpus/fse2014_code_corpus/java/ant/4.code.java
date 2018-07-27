import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import java.util.Collection;
import java.util.Set;
import java.util.Arrays;
import java.util.Collections;
public class DistributedAnnotationFactory implements AnnotationProcessorFactory {
    private static final Collection<String> supportedAnnotations
            = Collections.unmodifiableCollection(Arrays.asList("*"));
    public Collection<String> supportedOptions() {
        return Collections.emptySet();
    }
    public Collection<String> supportedAnnotationTypes() {
        return supportedAnnotations;
    }
    public AnnotationProcessor getProcessorFor(
            Set<com.sun.mirror.declaration.AnnotationTypeDeclaration> annotationTypeDeclarations,
            AnnotationProcessorEnvironment env) {
        return new DistributedAnnotationProcessor(env);
    }
}
