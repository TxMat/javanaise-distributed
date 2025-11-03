package Objects;

import java.io.Serializable;

import jvn.Annotations.JvnAnnotate;

public interface A extends Serializable {
    
    @JvnAnnotate( JvnAnnotate.LockType.READ)
    public int getValue();
    @JvnAnnotate( JvnAnnotate.LockType.WRITE)
    public void setValue(int n);
    @JvnAnnotate( JvnAnnotate.LockType.WRITE)
    public void addValue(int n);

    @JvnAnnotate( JvnAnnotate.LockType.WRITE)
    void waitWrite(long seconds);
}
