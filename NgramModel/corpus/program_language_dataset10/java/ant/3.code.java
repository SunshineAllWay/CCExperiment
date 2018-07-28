import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface Distributed  {
    public DistributionTypes distribution() default DistributionTypes.LOCAL;
    public String protocol() default "RMI";
    public enum DistributionTypes { SINGLETON, LOCAL, FAULT_TOLERANT, FEDERATED, MOBILE};
}
