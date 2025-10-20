package jvn;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import Objects.A;
import Objects.A_Impl;
import Objects.MultiRepartisObject;
import Objects.MultiRepartisObject_Impl;
import jvn.Enums.ConsoleColor;
import jvn.Exceptions.JvnException;
import jvn.Impl.JvnServerImpl;
import jvn.Interfaces.JvnLocalServer;
import jvn.Interfaces.JvnObject;
import jvn.test_custom_seria.S1;
import jvn.test_custom_seria.S1_Impl;
import jvn.test_custom_seria.S3;
import jvn.test_custom_seria.S3_Impl;

public class JvnServerMain {
    
    public static JvnLocalServer server;
    
    public static void main(String[] args) {
        
        server = JvnServerImpl.jvnGetServer(args.length >= 1 ? args[0] : "127.0.0.1");
        
        ConsoleColor.magicLog(server==null);
        ConsoleColor.magicLog(JvnServerImpl.jvnGetServer(null)==null);
        ConsoleColor.magicLog("Local Server created !");
        
        new Thread(JvnServerMain::runConsole).start();
    }
    
    private static final Map<String, A> interceptors = new HashMap<>();
    private static final Map<String, S1> s1_interceptors = new HashMap<>();
    private static final Map<String, S3> s_interceptors = new HashMap<>();
    
    public static void runConsole() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(ConsoleColor.toGreen("<<<----===---->>> "));
            String[] args = scanner.nextLine().trim().split("\\s+");
            
            if (args.length == 0) continue;
            
            try {
                switch (args[0]) {
                    case "exit", "q" -> exit(scanner);
                    case "mro" -> mro(args);
                    case "test" -> test(args);
                    case "test2" -> test2(args);
                    case "cpt" -> cpt(args);
                    case "list", "ls" -> ls();
                    case "create", "c" -> create(args);
                    case "lookup" -> lookup(args);
                    case "waitwrite", "ww" -> ww(args);
                    case "help" -> help();
                    default -> ConsoleColor.magicLog("Commande inconnue.");
                }
            } catch (NumberFormatException | JvnException e) {
                ConsoleColor.magicError("Erreur : " + e.getMessage());
            }
        }
    }
    
    public static void help(){
        ConsoleColor.magicLog("mro                        : mro help");
        ConsoleColor.magicLog("test2                      : test2 help");
        ConsoleColor.magicLog("test                       : test des truc");
        ConsoleColor.magicLog("cpt <nb>                   : Pour un stress test de compteur");
        ConsoleColor.magicLog("list                       :");
        ConsoleColor.magicLog("create <A/S> <jon> [value] : Créer un JvnObject de type A ou S1");
        ConsoleColor.magicLog("lookup <A/S/S3> <jon>      : Récupérer un objet déjà créé et sur le Coord (de type A, S ou S3)");
        ConsoleColor.magicLog("waitwrite                  : ");
        
    }
    
    public static void test2(String[] args) throws JvnException{
        /* C'est pété
        S2 s = JvnInterceptor.createInterceptor(new S2_Impl(server), args[1], server);
        Serializable s1 = JvnInterceptor.createInterceptor(new S1_Impl(-10), "s1", server);
        Serializable s2 = JvnInterceptor.createInterceptor(new S1_Impl(0), "s2", server);
        Serializable s3 = JvnInterceptor.createInterceptor(new S1_Impl(10), "s3", server);
        s.add("s1", s1);
        s.add("str", "Bonjour");
        s.add("s2", s2);
        s.add("s3", s3);
        s.add("prsn", new personne());
        ConsoleColor.magicLog("S2 s = .... ; \ns.toString() :\n"+s.toSting());
        */
        
        // test2 create <name>
        
        /*
        * test2 create <name> <A/S1>
        * test2 set <jon> in <s3 name>
        * test2 meth <meth name> under <s3 name>
        */
        
        if(args.length < 2 || args[1].equals("help")) {
            ConsoleColor.magicLog("test2 HELP : \ntest2 auto\ntest2 create <name> <A/S>\ntest2 set <jon> in <s3 name>\ntest2 meth <meth name> under <s3 name>");
            return;
        }
        
        switch (args[1]) {
            case "auto" -> {
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("help();"));
                help();
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("create(new String[]{\"A\", \"a0\", \"10\"});"));
                create(new String[]{"c", "A", "a0", "10"});
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("create(new String[]{\"S\", \"s0\", \"20\"});"));
                create(new String[]{"c", "S", "s0", "20"});
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("ls();"));
                ls();
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("test2(new String[]{\"create\", \"s3_a\", \"A\"});"));
                test2(new String[]{"test2", "create", "s3_a", "A"});
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("test2(new String[]{\"create\", \"s3_s\", \"S\"});"));
                test2(new String[]{"test2", "create", "s3_s", "S"});
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("ls();"));
                ls();
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("test2(new String[]{\"set\", \"a0\", \"in\", \"s3_a\"});"));
                test2(new String[]{"test2", "set", "a0", "in", "s3_a"});
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("test2(new String[]{\"set\", \"s0\", \"in\", \"s3_s\"});"));
                test2(new String[]{"test2", "set", "s0", "in", "s3_s"});
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("ls();"));
                ls();
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("test2(new String[]{\"meth\", \"add\", \"under\", \"s3_a\"});"));
                test2(new String[]{"test2", "meth", "add", "under", "s3_a"});
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("ls();"));
                ls();
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("test2(new String[]{\"meth\", \"add\", \"under\", \"s3_s\"});"));
                test2(new String[]{"test2", "meth", "add", "under", "s3_s"});
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("ls();"));
                ls();
            }
            case "create", "c" -> {
                if(args.length !=4) {
                    ConsoleColor.magicLog("USAGE : test2 create <name> <A/S1>");
                    return;
                }
                
                S3<?> s;
                
                switch (args[3]) {
                    case "A" -> s = new S3_Impl<A>();
                    case "S" -> s = new S3_Impl<S1>();
                    default -> { return; }
                }
                
                s = JvnInterceptor.createInterceptor(s, args[2], server);
                s_interceptors.put(args[2], s);
                ConsoleColor.magicLog(ConsoleColor.toGreen("OK."));
            }
            case "set","s" -> {
                if(args.length !=5) {
                    ConsoleColor.magicLog("USAGE : test2 set <jon> in <s3 name>");
                    return;
                }   A a = interceptors.get(args[2]);
                S1 s1 = s1_interceptors.get(args[2]);
                if(a == null && s1 == null) {
                    ConsoleColor.magicError("Objet A/S1 non trouvé : "+args[2]);
                    return;
                }   if(a!=null) {
                    S3<A> s = s_interceptors.get(args[4]);
                    if(s==null) {
                        ConsoleColor.magicError("Objet S3 non trouvé : "+args[2]);
                        return;
                    }
                    s.setObj(a);
                } else {
                    S3<S1> s = s_interceptors.get(args[4]);
                    if(s==null) {
                        ConsoleColor.magicError("Objet S3 non trouvé : "+args[2]);
                        return;
                    }
                    s.setObj(s1);
                }
            }
            case "meth","m" ->{
                if(args.length !=5) {
                    ConsoleColor.magicLog("USAGE : test2 meth <meth name> under <s3 name>");
                    return;
                }       

                S3<?> s = s_interceptors.get(args[4]);
                if(s==null) {
                    ConsoleColor.magicError("Objet S3 non trouvé : "+args[2]);
                    return;
                }

                Object o = s.getObj();
                int value = 0;
                if(args[2].equals("add") || args[2].equals("set")) {
                    value = (int)(Math.random()*100-50);
                    ConsoleColor.magicLog("Meth value = "+value);
                }
                switch (o) {
                    case A a -> {
                        switch (args[2]) {
                            case "add" -> a.addValue(value);
                            case "set" -> a.setValue(value);
                            case "get" -> ConsoleColor.magicLog("Value : "+a.getValue());
                            default -> {}
                        }
                    }
                    case S1 s1 -> {
                        switch (args[2]) {
                            case "add" -> s1.addValue(value);
                            case "set" -> s1.setValue(value);
                            case "get" -> ConsoleColor.magicLog("Value : "+s1.getValue());
                            default -> {}
                        }
                    }
                    default -> {}
                }
            }
            default -> {}
        }
    }
    
    public static void test(String[] args) throws JvnException {
        ConsoleColor.magicLog(JvnServerImpl.jvnGetServer(null)==null);
        if (args.length != 3) {
            ConsoleColor.magicLog("test actuel : \n> test <jon> <nb>\n- jon : Object Name\n- nb : Valeur à ajouter");
            return;
        }
        A a = interceptors.get(args[1]);
        
        if(a==null) {
            JvnObject jo = server.jvnLookupObject(args[1]);
            if(jo == null) return;
            a = JvnInterceptor.createInterceptor(jo, args[1], server);
        }
        
        ConsoleColor.magicLog("BF : "+a);
        
        int v = 0;
        try {
            v = Integer.parseInt(args[2]);
        } catch(NumberFormatException e) {}
        a.addValue(v);
        
        ConsoleColor.magicLog("AF : "+a);
    }
    public static void ls() {
        interceptors.forEach((k, v) -> {
            ConsoleColor.magicLog(k + " = " + v.toString());
        });
        s1_interceptors.forEach((k, v) -> {
            ConsoleColor.magicLog(k + " = " + v.toString());
        });
        s_interceptors.forEach((k, v) -> {
            ConsoleColor.magicLog(k + " = " + v.toString());
        });
        ConsoleColor.magicLog("mro = "+(mro==null?"null":mro.toString()));
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
        if (args.length != 3) return;
        
        JvnObject jo = server.jvnLookupObject(args[2]);
        
        switch (args[1]) {
            case "A" -> {
                A a = JvnInterceptor.createInterceptor(jo, args[2], server);
                interceptors.put(args[2], a);
                ConsoleColor.magicLog(a);
            }
            case "S" -> {
                S1 s1 = JvnInterceptor.createInterceptor(jo, args[2], server);
                s1_interceptors.put(args[2], s1);
                ConsoleColor.magicLog(s1);
            }
            case "S3" -> {
                S3<?> s = JvnInterceptor.createInterceptor(jo, args[2], server);
                s_interceptors.put(args[2], s);
                ConsoleColor.magicLog(s);
            }
            default -> {}
        }
    }
    public static void create(String[] args) throws JvnException {
        if (args.length != 3 && args.length != 4) return;
        int value = 0;
        if(args.length==4) {
            try {
                value = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                value = (int)(Math.random()*200-100);
                ConsoleColor.magicLog("Nombre non reconnu : "+args[2]+", valeur par défaut pour l'objet \""+args[1]+"\": "+value);
            }
        }
        if(args[1].equals("A")) {
            A a = JvnInterceptor.createInterceptor(new A_Impl(value), args[2], server);
            interceptors.put(args[2], a);
            ConsoleColor.magicLog(a);
            
        } else if(args[1].equals("S")) {
            S1 s = JvnInterceptor.createInterceptor(new S1_Impl(value), args[2], server);
            s1_interceptors.put(args[2], s);
            ConsoleColor.magicLog(s);
        }
    }
    
    public static void ww(String[] args) throws JvnException {
        if (args.length != 1) return;
        
        A wwrite = interceptors.get("wwrite");
        
        if (wwrite == null) {
            JvnObject jo = server.jvnLookupObject("wwrite");
            wwrite = JvnInterceptor.createInterceptor(Objects.requireNonNullElseGet(jo, () -> new A_Impl(0)), "wwrite", server);
            interceptors.put("wwrite", wwrite);
        }
        
        wwrite.waitWrite(20);
    }
    
    public static void cpt(String[] args) throws JvnException {
        A cpt = interceptors.get("cpt");
        
        if (cpt == null) {
            JvnObject jo = server.jvnLookupObject("cpt");
            
            if (jo == null) {
                cpt = JvnInterceptor.createInterceptor(new A_Impl(0), "cpt", server);
                for (int i = 5; i > 0; i--) {
                    ConsoleColor.magicLog(i);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        ConsoleColor.magicError("error : " + e.getMessage());
                    }
                }
            } else {
                cpt = JvnInterceptor.createInterceptor(jo, "cpt", server);
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
    
    private static MultiRepartisObject mro;
    private static void mro(String[] args) throws JvnException {
        /*
        * add <jon>
        * remove <jon>
        * <jon> <action>
        */
        int lg = args.length;
        if(lg==1 || (lg==2 && args[1].equals("help"))) {
            ConsoleColor.magicLog("mro init          : Créé / Récupère le MRO");
            ConsoleColor.magicLog("mro add <new jon> : Créé et ajoute un JvnObject");
            ConsoleColor.magicLog("mro remove <jon>  : Remove un JvnObject");
            ConsoleColor.magicLog("mro <jon> r       : Lit la valeur de l'objet");
            ConsoleColor.magicLog("mro <jon> s <nb>  : Set la valeur de l'objet");
            ConsoleColor.magicLog("mro <jon> a <nb>  : Add la valeur à l'objet");
        } else if(lg==2 && args[1].equals("init")) {
            if(mro != null) return;
            JvnObject jo = server.jvnLookupObject("mro");
            mro = JvnInterceptor.createInterceptor(jo==null?new MultiRepartisObject_Impl():jo, "mro", server);
            ConsoleColor.magicLog("MRO init");
        } else if(lg==3) {
            if(args[1].equals("add")) {
                
                // Créer un JvnObject + le register
                JvnObject jo = server.jvnCreateObject(new A_Impl());
                server.jvnRegisterObject(args[2], jo);
                
                mro.addJvnObject(args[2], jo);
                
                ConsoleColor.magicLog("MRO : "+mro.toString());
            } else if(args[1].equals("remove")) {
                
                JvnObject jo = mro.removeJvnObject(args[2]);
                if(jo==null) ConsoleColor.magicLog("L'objet suivant n'existe pas dans le MRO : "+args[2]);
                else ConsoleColor.magicLog("L'objet suivant a été remove du MRO : "+args[2]);
                
            } else if(args[2].equals("r")) {
                /*
                * mro <jon> r => ReadValue
                */
                JvnObject jo = mro.getJvnObject(args[1]);
                if(jo == null) { 
                    // Objet qui n'est pas / plus dans le MRO
                    ConsoleColor.magicLog("Le JvnObject suiavnt n'a pas été trouvé dans le MRO : "+args[1]);
                    return;
                }
                // Récupération de l'interseptor local
                A a = interceptors.get(args[1]);
                if(a == null) { 
                    // Ou le créer si il n'en a pas
                    a = JvnInterceptor.createInterceptor(jo, args[1], server);
                    interceptors.put(args[1], a);
                }
                ConsoleColor.magicLog("MRO : value of "+args[1]+" = "+a.getValue());
            }
        } else if(args.length == 4) {
            /*
            * mro <jon> s <nb> => SetValue
            * mro <jon> a <nb> => AddValue
            */
            int v;
            try {
                v = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) { ConsoleColor.magicError("Nombre non reconnu : "+args[3]); return; }
            
            JvnObject jo = mro.getJvnObject(args[1]);
            if(jo == null) { 
                // Objet qui n'est pas / plus dans le MRO
                ConsoleColor.magicLog("Le JvnObject suiavnt n'a pas été trouvé dans le MRO : "+args[1]);
                return;
            }
            
            // Récupération de l'interseptor local
            A a = interceptors.get(args[1]);
            if(a == null) { 
                // Ou le créer si il n'en a pas
                a = JvnInterceptor.createInterceptor(jo, args[1], server);
                interceptors.put(args[1], a);
            }
            
            switch (args[2]) {
                case "s" -> {
                    a.setValue(v);
                    ConsoleColor.magicLog("MRO : value of "+args[1]+" set to "+v);
                }
                case "a" -> {
                    a.addValue(v);
                    ConsoleColor.magicLog("MRO : value of "+args[1]+" increas of "+v);
                }
                default -> { ConsoleColor.magicLog("MRO : unknown function "+args[2]); }
            }
        }
    }
}