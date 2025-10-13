package Objects;

public class A_Impl implements A {

    private int value;

    public A_Impl() { this((int)(Math.random()*100)); }
    public A_Impl(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return this.value;
    }

    @Override
    public void setValue(int n) {
        this.value = n;
    }

    @Override
    public void addValue(int n) {
        this.value += n;
    }

    @Override
    public String toString() {
        return this.getClass().getName()+" : value = "+getValue();
    }
    
}