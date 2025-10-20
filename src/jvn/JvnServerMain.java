package jvn;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import Objects.A;
import Objects.A_Impl;
import jvn.Enums.ConsoleColor;
import jvn.Exceptions.JvnException;
import jvn.Impl.JvnMemoryEfficientServerImpl;
import jvn.Interfaces.JvnLocalServer;
import jvn.Interfaces.JvnObject;

public class JvnServerMain {
    
    public static JvnLocalServer server;
    
    public static void main(String[] args) {
        
        // server = JvnServerImpl.jvnGetServer();
        server = JvnMemoryEfficientServerImpl.jvnGetServer();
        
        ConsoleColor.magicLog("Local Server created !");
        
        new Thread(JvnServerMain::runConsole).start();
        
    }
    
    private static final Map<String, A> interceptors = new HashMap<>();
    
    public static void runConsole() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(ConsoleColor.toGreen("<<<----===---->>> "));
            String[] args = scanner.nextLine().trim().split("\\s+");
            
            if (args.length == 0) continue;
            
            try {
                switch (args[0]) {
                    case "exit", "q" -> exit(scanner);
                    case "test" -> test(args);
                    case "cpt" -> cpt(args);
                    case "list", "ls" -> ls();
                    case "create", "c" -> create(args);
                    case "lookup" -> lookup(args);
                    default -> ConsoleColor.magicLog("Commande inconnue.");
                }
            } catch (NumberFormatException | JvnException e) {
                ConsoleColor.magicError("Erreur : " + e.getMessage());
            }
        }
    }
    
    public static void test(String[] args) throws JvnException {
        if (args.length != 3) {
            ConsoleColor.magicLog("test actuel : \n> test <jon> <nb>\n- jon : Object Name\n- nb : Valeur à ajouter");
        }
        A a = interceptors.get(args[1]);
        
        if(a==null) {
            JvnObject jo = server.jvnLookupObject(args[1]);
            if(jo == null) return;
            a = (A) JvnInterceptor.createInterceptor(jo, args[1], server);
        }
        
        ConsoleColor.magicLog("BF : "+a);
        
        int v = 0;
        try {
            v = Integer.parseInt(args[2]);
        } catch(NumberFormatException e) {}
        a.addValue(v);
        
        ConsoleColor.magicLog("AF : "+a);
    }
    public static void ls(){
        interceptors.forEach((k, v) -> {
            ConsoleColor.magicLog(k + " = " + v.toString());
        });
    }
    public static void exit(Scanner scanner){
        ConsoleColor.magicLog("EXIT...");
        int exit_status = 0;
        try {
            server.jvnTerminate();
        } catch (JvnException e) {
            ConsoleColor.magicError(e.getMessage());
            exit_status = 1;
        }
        scanner.close();
        System.exit(exit_status);
    }
    public static void lookup(String[] args) throws JvnException {
        if (args.length != 2) return;
        
        JvnObject jo = server.jvnLookupObject(args[1]);
        A a = (A) JvnInterceptor.createInterceptor(jo, args[1], server);
        interceptors.put(args[1], a);
        ConsoleColor.magicLog(a);
    }
    public static void create(String[] args) throws JvnException {
        if (args.length != 2 && args.length != 3) return;
        int value = 0;
        if(args.length==3) {
            try {
                value = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                value = (int)(Math.random()*200-100);
                ConsoleColor.magicLog("Nombre non reconnu : "+args[2]+", valeur par défaut pour l'objet \""+args[1]+"\": "+value);
            }
        }
        A a = (A) JvnInterceptor.createInterceptor(new A_Impl(value), args[1], server);
        interceptors.put(args[1], a);
        ConsoleColor.magicLog(a);
    }
    public static void cpt(String[] args) throws JvnException {
        A cpt = interceptors.get("cpt");
        
        if (cpt == null) {
            JvnObject jo = server.jvnLookupObject("cpt");
            
            if (jo == null) {
                cpt = (A) JvnInterceptor.createInterceptor(new A_Impl(0), "cpt", server);
                for (int i = 5; i > 0; i--) {
                    ConsoleColor.magicLog(i);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        ConsoleColor.magicError("error : " + e.getMessage());
                    }
                }
            } else {
                cpt = (A) JvnInterceptor.createInterceptor(jo, "cpt", server);
            }
            interceptors.put("cpt", cpt);
        }
        
        long time_bf = System.currentTimeMillis();
        
        int nb = Integer.parseInt(args[1]);
        int origin = nb;
        while (nb > 0) {
            
            int v1 = cpt.getValue();
            cpt.addValue(1);
            int v2 = cpt.getValue();
            
            /*sysout*/ // ConsoleColor.magicLog("cpt passe de " + v1 + " à " + v2);
            
            nb--;
        }
        
        long time_af = System.currentTimeMillis();
        long diff = time_af-time_bf;
        ConsoleColor.magicLog(diff+"ms pour augmenter le compteur de "+origin+" ( "+((float)origin/diff)+".ms^-1)");
    }
}