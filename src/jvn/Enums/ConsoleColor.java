package jvn.Enums;

public class ConsoleColor {
    
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
        
        public String apply(Object str) {
            return code + str + GRAY.code;
        }
    }
    
    public static String toGray(Object str)   { return Color.GRAY.apply(str); }
    public static String toWhite(Object str)  { return Color.WHITE.apply(str); }
    public static String toRed(Object str)    { return Color.RED.apply(str); }
    public static String toGreen(Object str)  { return Color.GREEN.apply(str); }
    public static String toYellow(Object str) { return Color.YELLOW.apply(str); }
    public static String toBlue(Object str)   { return Color.BLUE.apply(str); }
    public static String toPurple(Object str) { return Color.PURPLE.apply(str); }
    public static String toCyan(Object str)   { return Color.CYAN.apply(str); }
    
    public static void magicLog(Object msg) {
        magicLog(msg, true);
    }
    public static void magicLog(Object msg, boolean ln) {
        String time = toPurple("[ "+System.currentTimeMillis()+" ] ");
        if(msg == null) {
            System.out.println(time+"null");
            return;
        }
        
        String[] lines = msg.toString().split("\n");
        int end = lines.length-1;
        for(int i = 0; i < end; i++){
            System.out.println(time+lines[i]);
        }
        
        if(ln) System.out.println(time+lines[end]);
        else System.out.print(time+lines[end]);
    }
    
    public static void magicError(Object msg) {
        magicLog(msg, true);
    }
    public static void magicError(Object msg, boolean ln) {
        String time = toPurple("[ "+System.currentTimeMillis()+" ] ");
        if(msg == null) {
            System.out.println(time+toRed("null"));
            return;
        }

        String[] lines = msg.toString().split("\n");
        int end = lines.length-1;
        for(int i = 0; i < end; i++){
            System.out.println(time+toRed(lines[i]));
        }
        
        if(ln) System.out.println(time+toRed(lines[end]));
        else System.out.print(time+toRed(lines[end]));
    }
}