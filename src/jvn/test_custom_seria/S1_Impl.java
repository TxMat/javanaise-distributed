package jvn.test_custom_seria;

public class S1_Impl implements S1 {

    private int v;

    public S1_Impl(int v) {
        this.v = v;
    }

    @Override
    public int getValue() {
        return v;
    }

    @Override
    public void setValue(int v) {
        this.v = v;
    }

    @Override
    public void addValue(int v) {
        this.v+=v;
    }
 
    @Override
    public String toString() {
        return "S1_impl : v = "+v;
    }

}