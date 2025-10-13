package Objects;

import jvn.Annotations.JvnAnnotate;

public class A_Impl implements A {

    private int value;

    public A_Impl() { this((int)(Math.random()*100)); }
    public A_Impl(int value) {
        this.value = value;
    }

    @Override
    @JvnAnnotate( JvnAnnotate.LockType.READ)
    public int getValue() {
        return this.value;
    }

    @Override
    @JvnAnnotate( JvnAnnotate.LockType.WRITE)
    public void setValue(int n) {
        this.value = n;
    }

    @Override
    @JvnAnnotate( JvnAnnotate.LockType.WRITE)
    public void addValue(int n) {
        this.value += n;
    }

    @Override
    @JvnAnnotate( JvnAnnotate.LockType.READ)
    public String toString() {
        return this.getClass().getName()+" : value = "+value;
    }
    
}