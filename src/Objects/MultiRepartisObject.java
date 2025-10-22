package Objects;

import java.io.Serializable;

import jvn.Annotations.JvnAnnotate;
import jvn.Interfaces.JvnObject;

public interface MultiRepartisObject extends Serializable {
    
    @JvnAnnotate( JvnAnnotate.LockType.READ )
    JvnObject getJvnObject(String jon);
    
    @JvnAnnotate( JvnAnnotate.LockType.WRITE )
    void addJvnObject(String jon, JvnObject jo);
    
    @JvnAnnotate( JvnAnnotate.LockType.WRITE )
    JvnObject removeJvnObject(String jon);
    
    @JvnAnnotate(JvnAnnotate.LockType.READ)
    @Override
    public String toString();
}