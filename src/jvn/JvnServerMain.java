package jvn;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import Objects.A;
import Objects.A_Impl;
import jvn.Exceptions.JvnException;
import jvn.Impl.JvnServerImpl;
import jvn.Interfaces.JvnLocalServer;
import jvn.Interfaces.JvnObject;

public class JvnServerMain {
    
    public static JvnLocalServer server;
    
    public static void main(String[] args) {
        
        server = JvnServerImpl.jvnGetServer();
        
        System.out.println("[ "+System.currentTimeMillis()+" ] "+"Local Server created !");
        
        new Thread(() -> runConsole()).start();
        
    }
    private static final Map<String, JvnObject> aaaaaaaa = new HashMap<>();
    
    public static void runConsole() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            String[] args = line.trim().split("\\s+");
            
            if (args.length == 0 || args[0].isEmpty()) continue;
            
            try {
                switch (args[0]) {
                    case "exit", "q" -> {
                        System.out.println("[ "+System.currentTimeMillis()+" ] "+"EXIT...");
                        scanner.close();
                        System.exit(0);
                    }
                    case "test" -> {
                        test(args);
                    }
                    case "cpt" -> {
                        cpt(args);
                    }
                    case "RL", "LR" -> {
                        if(args.length != 2) break;
                        JvnObject o = aaaaaaaa.get(args[1]);
                        o.jvnLockRead();
                    }
                    case "WL", "LW" -> {
                        if(args.length != 2) break;
                        JvnObject o = aaaaaaaa.get(args[1]);
                        o.jvnLockWrite();
                    }
                    case "UL" -> {
                        if(args.length != 2) break;
                        JvnObject o = aaaaaaaa.get(args[1]);
                        o.jvnUnLock();
                    }
                    case "list", "ls" -> {
                        aaaaaaaa.forEach((k,v)->{
                            System.out.println("[ "+System.currentTimeMillis()+" ] "+k+" = "+v.toString());
                        });
                    }
                    case "meth", "m" -> {
                        // m <obj> <fun n°> <args>
                        if(args.length < 3) break;
                        
                        A a = (A)aaaaaaaa.get(args[1]).jvnGetSharedObject();
                        int v = a.getValue();
                        switch (args[2]) {
                            case "0" -> {
                                a.addValue(Integer.parseInt(args[3]));
                                System.out.println("[ "+System.currentTimeMillis()+" ] "+args[1]+" est passé de "+v+" à "+a.getValue());
                            }
                            case "1" -> {
                                a.setValue(Integer.parseInt(args[3]));
                                System.out.println("[ "+System.currentTimeMillis()+" ] "+args[1]+" est passé de "+v+" à "+a.getValue());
                            }
                            case "2" -> {
                                System.out.println("[ "+System.currentTimeMillis()+" ] "+args[1]+" a la valeur "+v);
                            }
                            default -> {
                                System.out.println("[ "+System.currentTimeMillis()+" ] "+"Commande Inconnu");
                            }
                        }
                    }
                    case "create", "c" -> {
                        if(args.length != 2) break;
                        
                        JvnObject jo = server.jvnCreateObject(new A_Impl());
                        aaaaaaaa.put(args[1], jo);
                        
                        server.jvnRegisterObject(args[1], jo);
                    }
                    case "lookup" -> {
                        if(args.length != 2) break;
                        
                        JvnObject jo = server.jvnLookupObject(args[1]);
                        aaaaaaaa.put(args[1], jo);
                        
                    }
                    default -> System.out.println("[ "+System.currentTimeMillis()+" ] "+"Commande inconnue.");
                }
            } catch (NumberFormatException | JvnException e) {
                System.out.println("[ "+System.currentTimeMillis()+" ] "+"Erreur : " + e.getMessage());
            }
        }
    }
    @SuppressWarnings("CallToPrintStackTrace")
    public static void test(String[] args){
        try {
            int nb = 1;
            if(args.length == 1) {
                System.out.println("TESTING");
                int i = 0;
                do { 
                    String jon = "a"+i;
                    JvnObject jo = server.jvnLookupObject(jon);
                    
                    System.out.println("i="+i+" jo==null="+(jo==null));
                    if(jo == null) break;
                    aaaaaaaa.put(jon, jo);
                    
                    i++;
                } while (true);
                
                trueLoop(nb);
            } else if(args.length == 3 && args[1].equals("c")) {
                for(int i = Integer.parseInt(args[2])-1; i >= 0; i--){
                    JvnObject jo = server.jvnCreateObject(new A_Impl());
                    aaaaaaaa.put("a"+i, jo);
                    server.jvnRegisterObject("a"+i, jo);
                }
                trueLoop(nb);
            } else if(args[1].equals("test")) {
                if(args[2].equals("a")) {
                    JvnObject jo = server.jvnCreateObject(new A_Impl());
                    aaaaaaaa.put("a", jo);
                    server.jvnRegisterObject("a", jo);
                } else if(args[2].equals("b")) {
                    JvnObject jo = server.jvnLookupObject("a");
                    aaaaaaaa.put("a", jo);
                    jo.jvnLockWrite();
                    jo.jvnUnLock();
                }
            }
        } catch (JvnException e) {
            e.printStackTrace();
        }
    }
    public static void trueLoop(int nb) throws JvnException{
        while(true) {
            String name = "a"+(int)(Math.random()*nb);
            // x ms
            // +- y ms
            int x = 1;
            int y = 0;
            int to = (int)(Math.random()*y+x);
            
            JvnObject a = aaaaaaaa.get(name);
            
            if (Math.random()<0.5) {
                System.out.println("LR : ");
                a.jvnLockRead();
                System.out.println("[ "+System.currentTimeMillis()+" ] "+name+" : LockRead relaché dans "+to+"ms. Lit la valeur : "+((A)(a.jvnGetSharedObject())).getValue());
            } else {
                System.out.println("LW : ");
                a.jvnLockWrite();
                int v1 = ((A)(a.jvnGetSharedObject())).getValue();
                int v = (int)(Math.random()*100-50);
                ((A)(a.jvnGetSharedObject())).addValue(v); // [v-50 ; v+50]
                int v2 = ((A)(a.jvnGetSharedObject())).getValue();
                System.out.println("[ "+System.currentTimeMillis()+" ] "+name+" : LockWrite relaché dans "+to+"ms. Passe de "+v1+" à "+v2+" ("+v+")");
            }
            /*
            aaaaaaaa.forEach((k,v)->{
            System.out.println("[ "+System.currentTimeMillis()+" ] "+k+" = "+v.toString());
            });
            */
            try {
                Thread.sleep(to);
                a.jvnUnLock();
                System.out.println("unlocked");
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("error");
            }
            System.out.println("continue");
        }
    }
    private static JvnObject cpt;
    public static void cpt(String[] args) throws JvnException {
        if(cpt==null) {
            cpt = server.jvnLookupObject("cpt");
            if(cpt==null) {
                A a = new A_Impl(0);
                cpt = server.jvnCreateObject(a);
                server.jvnRegisterObject("cpt", cpt);
                for(int i = 5; i >= 0; i--) {
                    System.out.println(i);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("error : "+e.getMessage());
                    }
                }
            }
        }
        
        int nb = Integer.parseInt(args[1]);
        while(nb > 0) {
            cpt.jvnLockWrite();
            
            int v1 = ((A)(cpt.jvnGetSharedObject())).getValue();
            
            ((A)(cpt.jvnGetSharedObject())).addValue(1);
            
            int v2 = ((A)(cpt.jvnGetSharedObject())).getValue();
            
            System.out.println("[ "+System.currentTimeMillis()+" ] cpt passe de "+v1+" à "+v2);
            
            cpt.jvnUnLock();
            
            nb--;
        }
    }
}