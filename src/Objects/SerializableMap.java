package Objects;

import java.io.Serializable;

public interface SerializableMap<T extends Serializable> extends Serializable {
    
    public boolean put(String name, T t);
    public T remove(String name);
    public T get(String name);
    public int size();
    
}