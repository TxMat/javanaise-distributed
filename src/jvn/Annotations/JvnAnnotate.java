package jvn.Annotations;

// method annotation that indicates that the method should use Read or write lock for JVN objects
// if the method is a getter, it should use a read lock
// if the method is a setter, it should use a write lock
// if the method is a method that does not modify the state of the JVN object, it should use a read lock
// if the method is a method that modifies the state of the JVN object, it should use a write lock
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface JvnAnnotate {
    public enum LockType {
        READ,
        WRITE
    }

    LockType value();
}