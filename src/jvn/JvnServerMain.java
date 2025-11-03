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

public class JvnServerMain {
    
    public static JvnLocalServer server;
    
    public static void main(String[] args) {
        
        server = JvnServerImpl.jvnGetServer();
        
        ConsoleColor.magicLog("Local Server created !", true);
        
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
                    case "mro" -> mro(args);
                    case "test" -> test(args);
                    case "cpt" -> cpt(args);
                    case "list", "ls" -> ls();
                    case "create", "c" -> create(args);
                    case "lookup" -> lookup(args);
                    case "help" -> help();
                    case "print_all" -> printAll(args);
                    case "waitwrite", "ww" -> ww(args);
                    default -> ConsoleColor.magicLog("Commande inconnue.", true);
                }
            } catch (NumberFormatException | JvnException e) {
                ConsoleColor.magicError("Erreur : " + e.getMessage(), true);
            }
        }
    }
    
    public static void help(){
        ConsoleColor.magicLog("create <jon> [value]       : Créer un JvnObject de type A", true);
        ConsoleColor.magicLog("lookup <jon>               : Récupérer un objet déjà créé et sur le Coord (de type A)", true);
        ConsoleColor.magicLog("list                       : liste des objets locaux", true);
        ConsoleColor.magicLog("mro                        : mro help", true);
        ConsoleColor.magicLog("test                       : test des trucs", true);
        ConsoleColor.magicLog("cpt <nb>                   : Pour un stress test de compteur", true);
        ConsoleColor.magicLog("waitwrite                  : Lock un objet en écriture pendant 20s", true);
        ConsoleColor.magicLog("exit                       : Quitter le serveur proprement", true);
        ConsoleColor.magicLog("print_all <y/n>            : Affiche toutes les logs (yes / no)", true);
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
    public static void ls(){
        interceptors.forEach((k, v) -> {
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
        if (args.length != 2) return;
        
        JvnObject jo = server.jvnLookupObject(args[1]);
        A a = JvnInterceptor.createInterceptor(jo, args[1], server);
        interceptors.put(args[1], a);
        ConsoleColor.magicLog(a, true);
    }
    public static void create(String[] args) throws JvnException {
        if (args.length != 2 && args.length != 3) return;
        int value = 0;
        if(args.length==3) {
            try {
                value = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                value = (int)(Math.random()*200-100);
                ConsoleColor.magicLog("Nombre non reconnu : "+args[2]+", valeur par défaut pour l'objet \""+args[1]+"\": "+value, true);
            }
        }
        A a = JvnInterceptor.createInterceptor(new A_Impl(value), args[1], server);
        interceptors.put(args[1], a);
        ConsoleColor.magicLog(a, true);
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
}