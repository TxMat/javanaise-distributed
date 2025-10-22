package Objects;

import java.io.Serializable;

import jvn.Annotations.JvnAnnotate;

public interface SerializableMap<T extends Serializable> extends Serializable {
    
    @JvnAnnotate(JvnAnnotate.LockType.WRITE)
    public boolean put(String name, T t);
    @JvnAnnotate(JvnAnnotate.LockType.WRITE)
    public T remove(String name);
    @JvnAnnotate(JvnAnnotate.LockType.READ)
    public T get(String name);
    @JvnAnnotate(JvnAnnotate.LockType.READ)
    public int size();

    
    @JvnAnnotate(JvnAnnotate.LockType.READ)
    @Override
    public String toString();
}