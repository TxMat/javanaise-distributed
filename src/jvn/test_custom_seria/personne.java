package jvn.test_custom_seria;

import java.io.Serializable;

public class personne implements Serializable {
    
    private final String frst_name;
    private final String last_name;
    private final int age;
    
    public personne(){
        frst_name = "a".repeat((int)(Math.random()*15));
        last_name = "b".repeat((int)(Math.random()*15));
        age = (int)(Math.random()*25);
    }
    
    public String getFN(){
        return frst_name;
    }
    public String getLN(){
        return last_name;
    }
    public int getAge(){
        return age;
    }
}
