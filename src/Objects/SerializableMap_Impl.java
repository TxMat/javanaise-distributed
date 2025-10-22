package Objects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SerializableMap_Impl<T extends Serializable> implements SerializableMap<T> {
    
    private final Map<String, T> internalMap;
    
    public SerializableMap_Impl() {
        this.internalMap = new HashMap<>();
    }
    
    @Override
    public boolean put(String name, T t) {
        return internalMap.put(name, t) == null;
    }
    
    @Override
    public T remove(String name) {
        return internalMap.remove(name);
    }
    
    @Override
    public T get(String name) {
        return internalMap.get(name);
    }
    
    @Override
    public int size() {
        return internalMap.size();
    }
    
    @Override
    public String toString() {
        return internalMap.toString();
    }
}