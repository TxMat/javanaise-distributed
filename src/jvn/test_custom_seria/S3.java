package jvn.test_custom_seria;

import java.io.Serializable;

import jvn.Annotations.JvnAnnotate;

public interface S3<T extends Serializable> extends Serializable {

    @JvnAnnotate(JvnAnnotate.LockType.READ)
    T getObj();
    
    @JvnAnnotate(JvnAnnotate.LockType.WRITE)
    void setObj(T s);

    @JvnAnnotate(JvnAnnotate.LockType.READ)
    @Override
    String toString();
}