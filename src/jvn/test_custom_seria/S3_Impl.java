package jvn.test_custom_seria;

import java.io.Serializable;

public class S3_Impl<T extends Serializable> implements S3<T> {
    
    private T s;
    
    @Override
    public T getObj() {
        return s;
    }
    
    @Override
    public void setObj(T s) {
        this.s = s;
    }

    @Override
    public String toString(){
        return "S3_Impl { " + (s==null?"null":s.toString()) + " }";
    }
}