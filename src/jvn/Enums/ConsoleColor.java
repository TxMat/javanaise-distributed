package jvn.Enums;

public class ConsoleColor {
    
    private static boolean activeLogs = false;
    public static void activeLogsOn() {
        activeLogs = true;
    } 
    public static void activeLogsOff() {
        activeLogs = false;
    }
    
    private enum Color {
        GRAY("\033[0m"),
        WHITE("\033[38;5;15m"),
        RED("\033[38;5;1m"),
        GREEN("\033[38;5;2m"),
        YELLOW("\033[38;5;3m"),
        BLUE("\033[38;5;4m"),
        PURPLE("\033[38;5;5m"),
        CYAN("\033[38;5;14m");
        
        private final String code;
        
        Color(String code) {
            this.code = code;
        }
        
        public String apply(String str) {
            return code + str + GRAY.code;
        }
    }
    
    public static String toGray(String str)   { return Color.GRAY.apply(str); }
    public static String toWhite(String str)  { return Color.WHITE.apply(str); }
    public static String toRed(String str)    { return Color.RED.apply(str); }
    public static String toGreen(String str)  { return Color.GREEN.apply(str); }
    public static String toYellow(String str) { return Color.YELLOW.apply(str); }
    public static String toBlue(String str)   { return Color.BLUE.apply(str); }
    public static String toPurple(String str) { return Color.PURPLE.apply(str); }
    public static String toCyan(String str)   { return Color.CYAN.apply(str); }
    
    public static void magicLog(Object msg) {
        magicLog(msg, true, false);
    }    
    public static void magicLog(Object msg, boolean constantLog) {
        magicLog(msg, true, constantLog);
    }
    public static void magicLogNoLn(Object msg) {
        magicLog(msg, false, false);
    }
    public static void magicLogNoLn(Object msg, boolean constantLog) {
        magicLog(msg, false, constantLog);
    }
    public static void magicLog(Object msg, boolean ln, boolean constantLog) {
        if(!constantLog && !activeLogs) return;
        
        String[] lines = msg.toString().split("\n");
        
        String time = toPurple("[ "+System.currentTimeMillis()+" ] ");
        int end = lines.length-1;
        for(int i = 0; i < end; i++){
            System.out.println(time+lines[i]);
        }
        
        if(ln) System.out.println(time+lines[end]);
        else System.out.print(time+lines[end]);
    }
    
    public static void magicError(Object msg) {
        magicError(msg, true, false);
    }
    public static void magicError(Object msg, boolean constantLog) {
        magicError(msg, true, constantLog);
    }
    public static void magicErrorNoLn(Object msg) {
        magicError(msg, false, false);
    }
    public static void magicErrorNoLn(Object msg, boolean constantLog) {
        magicError(msg, false, constantLog);
    }
    public static void magicError(Object msg, boolean ln, boolean  constantLog) {
        if(!constantLog && !activeLogs) return;
        
        String[] lines = msg.toString().split("\n");
        
        String time = toPurple("[ "+System.currentTimeMillis()+" ] ");
        int end = lines.length-1;
        for(int i = 0; i < end; i++){
            System.out.println(time+toRed(lines[i]));
        }
        
        if(ln) System.out.println(time+toRed(lines[end]));
        else System.out.print(time+toRed(lines[end]));
    }
}