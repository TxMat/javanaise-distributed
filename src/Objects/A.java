package Objects;

import jvn.Annotations.JvnAnnotate;

import java.io.Serializable;

public interface A extends Serializable {

    @JvnAnnotate(JvnAnnotate.LockType.READ)
    int getValue();

    @JvnAnnotate(JvnAnnotate.LockType.WRITE)
    void setValue(int n);

    @JvnAnnotate(JvnAnnotate.LockType.WRITE)
    void addValue(int n);

    @JvnAnnotate(JvnAnnotate.LockType.WRITE)
    void waitWrite(long seconds);

}
