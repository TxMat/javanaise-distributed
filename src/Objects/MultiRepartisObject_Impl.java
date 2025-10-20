package Objects;

import java.util.HashMap;
import java.util.Map;

import jvn.Interfaces.JvnObject;

public class MultiRepartisObject_Impl implements MultiRepartisObject {

    private final Map<String, JvnObject> objects = new HashMap<>();
    
    @Override
    public JvnObject getJvnObject(String jon) {
        return objects.get(jon);
    }
    @Override
    public void addJvnObject(String jon, JvnObject jo) {
        objects.put(jon, jo);
    }
    @Override
    public JvnObject removeJvnObject(String jon) {
        return objects.remove(jon);
    }

    public String toString() {
        return objects.size()+" objects";
    }
}