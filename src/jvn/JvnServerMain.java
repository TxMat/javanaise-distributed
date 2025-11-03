package jvn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import Objects.A;
import Objects.A_Impl;
import Objects.MultiRepartisObject;
import Objects.MultiRepartisObject_Impl;
import Objects.SerializableMap;
import Objects.SerializableMap_Impl;
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
        
        ConsoleColor.magicLog("Local Server created !", true);
        
        new Thread(JvnServerMain::runConsole).start();
    }
    
    private static final Map<String, A> interceptors = new HashMap<>();
    private static final Map<String, S1> s1_interceptors = new HashMap<>();
    private static final Map<String, S3<?>> s_interceptors = new HashMap<>();
    private static final Map<String, SerializableMap<?>> sm = new HashMap<>();
    
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
                    case "cycle" -> cycle();
                    case "sm" -> sm(args);
                    case "help" -> help();
                    case "print_all" -> printAll(args);
                    default -> ConsoleColor.magicLog("Commande inconnue.", true);
                }
            } catch (NumberFormatException | JvnException e) {
                ConsoleColor.magicError("Erreur : " + e.getMessage(), true);
            }
        }
    }
    
    public static void help() {
        ConsoleColor.magicLog("create <A/S> <jon> [value] : Créer un JvnObject de type A ou S1", true);
        ConsoleColor.magicLog("lookup <A/S/S3> <jon>      : Récupérer un objet déjà créé et sur le Coord (de type A, S, S3 ou SM)", true);
        ConsoleColor.magicLog("list                       : liste des objets locaux", true);
        ConsoleColor.magicLog("mro                        : mro help", true);
        ConsoleColor.magicLog("test                       : test des truc", true);
        ConsoleColor.magicLog("cpt <nb>                   : Pour un stress test de compteur", true);
        ConsoleColor.magicLog("print_all <y/n>            : Affiche toutes les logs (yes / no)", true);
        ConsoleColor.magicLog("==------===", true);
        ConsoleColor.magicLog("cycle", true);
        ConsoleColor.magicLog("sm                         : sm help", true);
        ConsoleColor.magicLog("test2                      : test2 help", true);
        ConsoleColor.magicLog("waitwrite                  : ", true);
    }
    
    public static void cycle() throws JvnException {

        S3<S3> s3_1 = JvnInterceptor.createInterceptor(new S3_Impl(), "cycle", server);
        S3<S3> s3_2 = JvnInterceptor.createInterceptor(new S3_Impl(), "_s3_2", server);
        S3<S3> s3_3 = JvnInterceptor.createInterceptor(new S3_Impl(), "_s3_3", server);
        S3<S3> s3_4 = JvnInterceptor.createInterceptor(new S3_Impl(), "_s3_4", server);

        s_interceptors.put("cycle", s3_1);
        s_interceptors.put("_s3_2", s3_2);
        s_interceptors.put("_s3_3", s3_3);
        s_interceptors.put("_s3_4", s3_4);

        s3_1.setObj(s3_2);
        s3_2.setObj(s3_3);
        s3_3.setObj(s3_4);

        ls();
        s3_4.setObj(s3_1);
        ConsoleColor.magicLog("CYCLE DONE - "+ConsoleColor.toRed("NE PAS ls OU AFFICHER !!!"));
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
        * test2 create <name> <A/S>
        * test2 set <jon> in <s3 name>
        * test2 meth <meth name> under <s3 name>
        */
        
        if(args.length < 2 || args[1].equals("help")) {
            ConsoleColor.magicLog("test2 HELP : \ntest2 auto\ntest2 create <name> <A/S>\ntest2 set <jon> in <s3 name>\ntest2 meth <meth name> under <s3 name>", true);
            return;
        }
        
        switch (args[1]) {
            case "auto" -> {
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("create A a0 10"), true);
                create(new String[]{"c", "A", "a0", "10"});
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("create S s0 20"), true);
                create(new String[]{"c", "S", "s0", "20"});
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("ls();"), true);
                ls();
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("test2 create s3_a A"), true);
                test2(new String[]{"test2", "create", "s3_a", "A"});
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("test2 create s3_s S"), true);
                test2(new String[]{"test2", "create", "s3_s", "S"});
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("ls();"), true);
                ls();
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("test2 set a0 in s3_a"), true);
                test2(new String[]{"test2", "set", "a0", "in", "s3_a"});
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("test2 set s0 in s3_s"), true);
                test2(new String[]{"test2", "set", "s0", "in", "s3_s"});
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("ls();"), true);
                ls();
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("test2 meth add under s3_a"), true);
                test2(new String[]{"test2", "meth", "add", "under", "s3_a"});
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("ls();"), true);
                ls();
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("test2 meth add under s3_s"), true);
                test2(new String[]{"test2", "meth", "add", "under", "s3_s"});
                ConsoleColor.magicLog("\n"+ConsoleColor.toCyan("ls();"), true);
                ls();
            }
            case "auto2" -> {
                ConsoleColor.magicLog("Création de 'a'", true);
                A a = JvnInterceptor.createInterceptor(new A_Impl(1111), "_a", server);
                interceptors.put("_a", a);
                ConsoleColor.magicLog(a, true);
                
                ConsoleColor.magicLog("Création de 's3'", true);
                S3<A> s3 = JvnInterceptor.createInterceptor(new S3_Impl<A>(), "_s3", server);
                s_interceptors.put("_s3", s3);
                ConsoleColor.magicLog(s3, true);
                
                ConsoleColor.magicLog("Set de 'a' dans 's3'", true);
                s3.setObj(a);
                ConsoleColor.magicLog(s3, true);
                
                ConsoleColor.magicLog("START SERIALIZATION", true);
                
                byte[] b;
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                    oos.writeObject(s3);
                    b = baos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                
                ConsoleColor.magicLog("SERIALIZATION OK", true);
                
                Object o;
                try (ByteArrayInputStream bais = new ByteArrayInputStream(b); ObjectInputStream ois = new ObjectInputStream(bais)) {
                    ConsoleColor.magicLog("A", true);
                    o = ois.readObject();
                    ConsoleColor.magicLog("B", true);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
                ConsoleColor.magicLog("DESERIALIZATION OK", true);
                
                ConsoleColor.magicLog(o, true);
                
            }
            case "create", "c" -> {
                if(args.length !=4) {
                    ConsoleColor.magicLog("USAGE : test2 create <name> <A/S>", true);
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
                ConsoleColor.magicLog(ConsoleColor.toGreen("OK."), true);
            }
            case "set","s" -> {
                if(args.length !=5) {
                    ConsoleColor.magicLog("USAGE : test2 set <jon> in <s3 name>", true);
                    return;
                }   A a = interceptors.get(args[2]);
                S1 s1 = s1_interceptors.get(args[2]);
                if(a == null && s1 == null) {
                    ConsoleColor.magicError("Objet A/S1 non trouvé : "+args[2], true);
                    return;
                }   if(a!=null) {
                    S3<A> s = (S3<A>) s_interceptors.get(args[4]);
                    if(s==null) {
                        ConsoleColor.magicError("Objet S3 non trouvé : "+args[2], true);
                        return;
                    }
                    s.setObj(a);
                } else {
                    S3<S1> s = (S3<S1>) s_interceptors.get(args[4]);
                    if(s==null) {
                        ConsoleColor.magicError("Objet S3 non trouvé : "+args[2], true);
                        return;
                    }
                    s.setObj(s1);
                }
            }
            case "meth","m" ->{
                if(args.length !=5) {
                    ConsoleColor.magicLog("USAGE : test2 meth <meth name> under <s3 name>", true);
                    return;
                }       
                
                S3<?> s = s_interceptors.get(args[4]);
                if(s==null) {
                    ConsoleColor.magicError("Objet S3 non trouvé : "+args[4], true);
                    return;
                }
                
                Object o = s.getObj();
                int value = 0;
                if(args[2].equals("add") || args[2].equals("set")) {
                    value = (int)(Math.random()*100-50);
                    ConsoleColor.magicLog("Meth value = "+value, true);
                }
                switch (o) {
                    case A a -> {
                        switch (args[2]) {
                            case "add" -> a.addValue(value);
                            case "set" -> a.setValue(value);
                            case "get" -> ConsoleColor.magicLog("Value : "+a.getValue(), true);
                            default -> {}
                        }
                    }
                    case S1 s1 -> {
                        switch (args[2]) {
                            case "add" -> s1.addValue(value);
                            case "set" -> s1.setValue(value);
                            case "get" -> ConsoleColor.magicLog("Value : "+s1.getValue(), true);
                            default -> {}
                        }
                    }
                    default -> {}
                }
            }
            default -> {}
        }
    }


    public static void printAll(String[] args){
        if (args.length!=2) return;
        if(args[1].equals("y")) {
            ConsoleColor.activeLogsOn();
        } else if(args[1].equals("n")) {
            ConsoleColor.activeLogsOff();
        }
    }
        
    public static void test(String[] args) throws JvnException {
        if (args.length != 3) {
            ConsoleColor.magicLog("test actuel : \n> test <jon> <nb>\n- jon : Object Name\n- nb : Valeur à ajouter", true);
            return;
        }
        A a = interceptors.get(args[1]);
        
        if(a==null) {
            JvnObject jo = server.jvnLookupObject(args[1]);
            if(jo == null) return;
            a = JvnInterceptor.createInterceptor(jo, args[1], server);
        }
        
        ConsoleColor.magicLog("BF : "+a, true);
        
        int v = 0;
        try {
            v = Integer.parseInt(args[2]);
        } catch(NumberFormatException e) {}
        a.addValue(v);
        
        ConsoleColor.magicLog("AF : "+a, true);
    }
    public static void ls() {
        interceptors.forEach((k, v) -> {
            ConsoleColor.magicLog(k + " = " + v.toString(), true);
        });
        s1_interceptors.forEach((k, v) -> {
            ConsoleColor.magicLog(k + " = " + v.toString(), true);
        });
        s_interceptors.forEach((k, v) -> {
            ConsoleColor.magicLog(k + " = " + v.toString(), true);
        });
        ConsoleColor.magicLog("mro = "+(mro==null?"null":mro.toString()), true);

    }
    public static void exit(Scanner scanner){
        ConsoleColor.magicLog("EXIT...", true);
        int exit_status = 0;
        try {
            server.jvnTerminate();
        } catch (JvnException e) {
            ConsoleColor.magicError(e.getMessage(), true);
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
                ConsoleColor.magicLog(a, true);
            }
            case "S" -> {
                S1 s1 = JvnInterceptor.createInterceptor(jo, args[2], server);
                s1_interceptors.put(args[2], s1);
                ConsoleColor.magicLog(s1, true);
            }
            case "S3" -> {
                S3<?> s = JvnInterceptor.createInterceptor(jo, args[2], server);
                s_interceptors.put(args[2], s);
                ConsoleColor.magicLog(s, true);
            }
            case "SM" -> {
                SerializableMap s = JvnInterceptor.createInterceptor(jo, args[2], server);
                sm.put(args[2], s);
                ConsoleColor.magicLog(s, true);
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
                ConsoleColor.magicLog("Nombre non reconnu : "+args[2]+", valeur par défaut pour l'objet \""+args[1]+"\": "+value, true);
            }
        }
        if(args[1].equals("A")) {
            A a = JvnInterceptor.createInterceptor(new A_Impl(value), args[2], server);
            interceptors.put(args[2], a);
            ConsoleColor.magicLog(a, true);
            
        } else if(args[1].equals("S")) {
            S1 s = JvnInterceptor.createInterceptor(new S1_Impl(value), args[2], server);
            s1_interceptors.put(args[2], s);
            ConsoleColor.magicLog(s, true);
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
        if(args.length == 1) {
            ConsoleColor.magicError("Utilisation de la commande `cpt` : `cpt <nb>`", true);
            return;
        }
        A cpt = interceptors.get("cpt");
        
        if (cpt == null) {
            JvnObject jo = server.jvnLookupObject("cpt");
            
            if (jo == null) {
                cpt = JvnInterceptor.createInterceptor(new A_Impl(0), "cpt", server);
                for (int i = 5; i > 0; i--) {
                    ConsoleColor.magicLog(i, true);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        ConsoleColor.magicError("error : " + e.getMessage(), true);
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
            
            ConsoleColor.magicLog("cpt passe de " + v1 + " à " + v2);
            
            nb--;
        }
        
        long time_af = System.currentTimeMillis();
        long diff = time_af-time_bf;
        ConsoleColor.magicLog(diff+"ms pour augmenter le compteur de "+origin+" ( "+((float)origin/diff)+".ms^-1)", true);
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
            ConsoleColor.magicLog("mro est une Map<String, JvnObject> simple ou l'on peut ajouter, retirer ou obtenir des couples Key-Value", true);
            ConsoleColor.magicLog("mro init          : Créé / Récupère le MRO (1 seul MRO possible pour simplifier)", true);
            ConsoleColor.magicLog("mro add <new jon> : Créé et ajoute un JvnObject", true);
            ConsoleColor.magicLog("mro remove <jon>  : Remove un JvnObject", true);
            ConsoleColor.magicLog("mro <jon> r       : Lit la valeur de l'objet ( r =    READ   )", true);
            ConsoleColor.magicLog("mro <jon> s <nb>  : Set la valeur de l'objet ( s =    SET    )", true);
            ConsoleColor.magicLog("mro <jon> a <nb>  : Add la valeur à l'objet  ( a = ADD_VALUE )", true);
        } else if(lg==2 && args[1].equals("init")) {
            if(mro != null) return;
            JvnObject jo = server.jvnLookupObject("mro");
            mro = JvnInterceptor.createInterceptor(jo==null?new MultiRepartisObject_Impl():jo, "mro", server);
            ConsoleColor.magicLog("MRO init", true);
        } else if(lg==3) {
            if(args[1].equals("add")) {
                
                // Créer un JvnObject + le register
                JvnObject jo = server.jvnCreateObject(new A_Impl());
                server.jvnRegisterObject(args[2], jo);
                
                mro.addJvnObject(args[2], jo);
                
                ConsoleColor.magicLog("MRO : "+mro.toString(), true);
            } else if(args[1].equals("remove")) {
                
                JvnObject jo = mro.removeJvnObject(args[2]);
                if(jo==null) ConsoleColor.magicLog("L'objet suivant n'existe pas dans le MRO : "+args[2], true);
                else ConsoleColor.magicLog("L'objet suivant a été remove du MRO : "+args[2], true);
                
            } else if(args[2].equals("r")) {
                /*
                * mro <jon> r => ReadValue
                */
                JvnObject jo = mro.getJvnObject(args[1]);
                if(jo == null) { 
                    // Objet qui n'est pas / plus dans le MRO
                    ConsoleColor.magicLog("Le JvnObject suiavnt n'a pas été trouvé dans le MRO : "+args[1], true);
                    return;
                }
                // Récupération de l'interseptor local
                A a = interceptors.get(args[1]);
                if(a == null) { 
                    // Ou le créer si il n'en a pas
                    a = JvnInterceptor.createInterceptor(jo, args[1], server);
                    interceptors.put(args[1], a);
                }
                ConsoleColor.magicLog("MRO : value of "+args[1]+" = "+a.getValue(), true);
            }
        } else if(args.length == 4) {
            /*
            * mro <jon> s <nb> => SetValue
            * mro <jon> a <nb> => AddValue
            */
            int v;
            try {
                v = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) { ConsoleColor.magicError("Nombre non reconnu : "+args[3], true); return; }
            
            JvnObject jo = mro.getJvnObject(args[1]);
            if(jo == null) { 
                // Objet qui n'est pas / plus dans le MRO
                ConsoleColor.magicLog("Le JvnObject suiavnt n'a pas été trouvé dans le MRO : "+args[1], true);
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
                    ConsoleColor.magicLog("MRO : value of "+args[1]+" set to "+v, true);
                }
                case "a" -> {
                    a.addValue(v);
                    ConsoleColor.magicLog("MRO : value of "+args[1]+" increas of "+v, true);
                }
                default -> { ConsoleColor.magicLog("MRO : unknown function "+args[2], true); }
            }
        }
    }
    private static void sm(String[] args, Object o) throws JvnException {
        ConsoleColor.magicLog(ConsoleColor.toCyan(String.join(" ", args)));
        sm(args);
    }
    private static void sm(String[] args) throws JvnException {
        if (args.length == 1 || args[1].equals("help")) {
            ConsoleColor.magicLog("sm help");
            ConsoleColor.magicLog("sm auto [call]                       : Création auto de plueisurs SM, call = get value sur un 'a' profond ('auto' seul obligatoir avant)", true);
            ConsoleColor.magicLog("sm new <name> <type>                 : crée une nouvelle SerializableMap (de A, S1, S3)", true);
            ConsoleColor.magicLog("sm addto <sm name> <type> <name>     : ajoute un objet déjà existant dans une sm", true);
            ConsoleColor.magicLog("sm addto <sm name> new <type> <name> : crée puis ajoute un objet dans une sm", true);
            ConsoleColor.magicLog("sm ls                                : liste les SerializableMap existantes", true);
            ConsoleColor.magicLog("sm <sm name>                         : affiche le contenu et la taille d'une SerializableMap", true);
            ConsoleColor.magicLog("sm meth <sm name> <elem> <type> <method> [param] : appelle une méthode sur un objet dans une SerializableMap", true);
            
            return;
        }
        
        
        switch (args[1]) {
            case "auto" -> {
                if(args.length != 2) {
                    if(args.length!=3 || !args[2].equals("call")) return;

                    SerializableMap<S3> sm_s3 = (SerializableMap<S3>)sm.get("sm_s3");
                    S3<A> s3_a0 = (S3<A>)sm_s3.get("_s3_a0");
                    A a = s3_a0.getObj();
                    ConsoleColor.magicLog(ConsoleColor.toYellow("BF LOOKUP : sm_s3 => _s3_a0 => a . getValue : "+a.toString()), true);
                    ConsoleColor.magicLog(ConsoleColor.toYellow("AF LOOKUP : sm_s3 => _s3_a0 => a . getValue : "+a.getValue()), true);
                    return;
                }
                // INIT SM
                sm(new String[]{"sm","new","sm_a","A"}, null);
                sm(new String[]{"sm","new","sm_s1","S1"}, null);
                sm(new String[]{"sm","new","sm_s3","S3"}, null);
                sm(new String[]{"sm","ls"}, null);
                
                ConsoleColor.magicLog(ConsoleColor.toCyan("CREATE   _a0,    _s0,    _s3_a0,   _s3_s0"), true);
                // CREATE pour les tests
                create(new String[]{"sm","A","_a0","10"});
                create(new String[]{"sm","S","_s0","20"});
                //create <A/S> <jon> [value]
                test2 (new String[]{"test2", "create", "_s3_a0", "A"});
                test2 (new String[]{"test2", "create", "_s3_s0", "S"});
                ConsoleColor.magicLog(ConsoleColor.toCyan("ls"), true);
                ls();
                
                // ADDTO sur les SM
                sm(new String[]{"sm","addto","sm_a" ,"A" ,"_a0"}, null);
                sm(new String[]{"sm","addto","sm_s1","S1","_s0"}, null);
                sm(new String[]{"sm","addto","sm_s3","S3","_s3_a0"}, null);
                sm(new String[]{"sm","addto","sm_s3","S3","_s3_s0"}, null);
                sm(new String[]{"sm","ls"}, null);
                
                // ADDTO - NEW : sur les SM
                sm(new String[]{"sm","addto","sm_a" ,"new","A" , "_a1" }, null);
                sm(new String[]{"sm","addto","sm_s1","new","S1", "_s1" }, null);
                sm(new String[]{"sm","addto","sm_s3","new","S3","_s3_a1"}, null);
                sm(new String[]{"sm","addto","sm_s3","new","S3","_s3_s1"}, null);
                sm(new String[]{"sm","ls"}, null);
                
                ConsoleColor.magicLog(ConsoleColor.toCyan("Triple imbrication ( sm_s3 => _s3_a0 => a )"), true);
                create(new String[]{"sm","A","a","50"});
                // test2 set a in _s3_a0
                test2 (new String[]{"test2", "set", "a", "in", "_s3_a0"});
                ls();
                sm(new String[]{"sm","sm_s3"}, null);
                
            }
            case "new" -> {
                if (args.length != 4) {
                    ConsoleColor.magicError("Usage: sm new <name> <type>", true);
                    return;
                }
                
                String smn = args[2];
                String type = args[3];
                
                SerializableMap<?> map;
                
                switch (type) {
                    case "A" -> map = new SerializableMap_Impl<A>();
                    case "S1" -> map = new SerializableMap_Impl<S1>();
                    case "S3" -> map = new SerializableMap_Impl<S3<?>>();
                    default -> {
                        ConsoleColor.magicError("Type non supporté : " + type, true);
                        return;
                    }
                }
                
                sm.put(smn, JvnInterceptor.createInterceptor(map, smn, server));
                ConsoleColor.magicLog("SerializableMap '" + smn + "' de type " + type + " créée.", true);
            }
            case "addto" -> {
                if (args.length != 5 && args.length != 6) {
                    ConsoleColor.magicError("Usage: sm addto <sm name> <type> <name>", true);
                    ConsoleColor.magicError("Usage: sm addto <sm name> new <type> <name>", true);
                    return;
                }
                boolean withNew = args.length == 6;
                
                String smn = args[2];
                String type = args[withNew?4:3];
                String jon = args[withNew?5:4];
                
                SerializableMap map = sm.get(smn);
                if (map == null) {
                    ConsoleColor.magicError("SerializableMap '" + smn + "' non trouvée.", true);
                    return;
                }
                
                Serializable s;
                if(withNew) {
                    switch (type) {
                        case "A" -> {
                            s = JvnInterceptor.createInterceptor(new A_Impl((int)(Math.random()*101-50)), jon, server);
                        }
                        case "S1" -> {
                            s = JvnInterceptor.createInterceptor(new S1_Impl((int)(Math.random()*101-50)), jon, server);
                        }
                        case "S3" -> {
                            s = JvnInterceptor.createInterceptor(new S3_Impl<>(), jon, server);
                        }
                        default -> throw new AssertionError();
                    }
                    ConsoleColor.magicLog("Objet "+type+" '" + jon + "' créé et sera ajouté au sm '" + smn + "'\n"+s, true);
                } else {
                    switch (type) {
                        case "A" -> {
                            s = interceptors.get(jon);
                        }
                        case "S1" -> {
                            s = s1_interceptors.get(jon);
                        }
                        case "S3" -> {
                            s = s_interceptors.get(jon);
                        }
                        default -> throw new AssertionError();
                    }
                    ConsoleColor.magicLog("Objet "+type+" '" + jon + "' récupéré et sera ajouté au sm '" + smn + "'\n"+s, true);
                }
                map.put(jon, s);
                ConsoleColor.magicLog("Objet ajouté au sm.", true);
            }
            case "ls" -> {
                if (sm.isEmpty()) {
                    ConsoleColor.magicLog("Aucune SerializableMap.", true);
                    return;
                }
                
                sm.forEach((name, map) -> ConsoleColor.magicLog(name + " = " + map, true));
            }
            case "meth" -> {
                
                if (args.length != 6 && args.length != 7) {
                    ConsoleColor.magicError("Usage: sm meth <sm name> <elem name> <type> <method name> [arg]", true);
                    return;
                }
                
                String smn = args[2];
                String jon = args[3];
                String type = args[4];
                String meth = args[5];
                String param = args.length == 7 ? args[6] : null;
                
                SerializableMap map = sm.get(smn);
                if (map == null) {
                    ConsoleColor.magicError("SerializableMap '" + smn + "' non trouvée.", true);
                    return;
                }
                
                Object obj = map.get(jon);
                if (obj == null) {
                    ConsoleColor.magicError("Element '" + jon + "' non trouvé dans le sm '" + smn + "'", true);
                    return;
                }
                
                switch (type) {
                    case "A" -> {
                        A a = (A) obj;
                        switch (meth) {
                            case "addValue" -> a.addValue(param != null ? Integer.parseInt(param) : 10);
                            case "setValue" -> a.setValue(param != null ? Integer.parseInt(param) : 0);
                            case "getValue" -> ConsoleColor.magicLog("VALUE: " + a.getValue(), true);
                            case "waitWrite" -> ConsoleColor.magicLog("... non, pas cette methode", true);
                            default -> ConsoleColor.magicError("Méthode inconnue pour A: " + meth, true);
                        }
                    }
                    case "S1" -> {
                        S1 s1 = (S1) obj;
                        switch (meth) {
                            case "addValue" -> s1.addValue(param != null ? Integer.parseInt(param) : 10);
                            case "setValue" -> s1.setValue(param != null ? Integer.parseInt(param) : 0);
                            case "getValue" -> ConsoleColor.magicLog("Résultat: " + s1.getValue());
                            default -> ConsoleColor.magicError("Méthode inconnue pour S1: " + meth, true);
                        }
                    }
                    case "S3" -> {
                        S3 s3 = (S3) obj;
                        switch (meth) {
                            case "setObj" -> {
                                if (param == null) {
                                    ConsoleColor.magicError("setObj attend un nom d’objet à utiliser.", true);
                                    return;
                                }
                                Serializable s = server.jvnLookupObject(param);
                                if (s == null) {
                                    ConsoleColor.magicError("Objet '" + param + "' introuvable via lookup.", true);
                                    return;
                                }
                                s3.setObj(s);
                                ConsoleColor.magicLog("Objet '" + param + "' assigné à S3.", true);
                            }
                            case "getObj" -> ConsoleColor.magicLog("Résultat: " + s3.getObj(), true);
                            case "toString" -> ConsoleColor.magicLog(s3.toString(), true);
                            default -> ConsoleColor.magicError("Méthode inconnue pour S3: " + meth, true);
                        }
                    }
                    default -> ConsoleColor.magicError("Type inconnu : " + type, true);
                }
                
                ConsoleColor.magicLog("Méthode " + meth + " appelée avec succès.", true);
                
            }
            default -> {
                String smn = args[1];
                SerializableMap map = sm.get(smn);
                if (map == null) {
                    ConsoleColor.magicError("SerializableMap '" + smn + "' non trouvée.", true);
                } else {
                    ConsoleColor.magicLog("smn '" + smn + "' (size of " + map.size() + ") \n"+map.toString(), true);
                }
            }
        }
    }
}