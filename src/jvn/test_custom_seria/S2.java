package jvn.test_custom_seria;

import java.io.Serializable;

import jvn.Annotations.JvnAnnotate;

public interface S2 extends Serializable {

    @JvnAnnotate(JvnAnnotate.LockType.WRITE)
    public boolean add(String name, Serializable s);

    @JvnAnnotate(JvnAnnotate.LockType.READ)
    public Serializable get(String name);

    @JvnAnnotate(JvnAnnotate.LockType.READ)
    public Serializable size();

    @JvnAnnotate(JvnAnnotate.LockType.WRITE)
    public Serializable remove(String name);

    @JvnAnnotate(JvnAnnotate.LockType.READ)
    public String toSting();
}