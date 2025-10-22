package jvn.test_custom_seria;

import java.io.Serializable;

import jvn.Annotations.JvnAnnotate;

public interface S1 extends Serializable {
    
    @JvnAnnotate( JvnAnnotate.LockType.READ)
    public int getValue();
    
    @JvnAnnotate( JvnAnnotate.LockType.WRITE)
    public void setValue(int v);
    
    @JvnAnnotate( JvnAnnotate.LockType.WRITE)
    public void addValue(int v);
    
    @JvnAnnotate(JvnAnnotate.LockType.READ)
    @Override
    public String toString();
}