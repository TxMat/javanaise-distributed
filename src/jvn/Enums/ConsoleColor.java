package jvn.Enums;

public class ConsoleColor {
    
    private enum Color {
        GRAY("\033[0m"),
        WHITE("\033[1m"),
        RED("\033[31m"),
        GREEN("\033[32m"),
        YELLOW("\033[33m"),
        BLUE("\033[34m"),
        PURPLE("\033[35m"),
        CYAN("\033[36m");
        
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
        magicLog(msg, true);
    }
    public static void magicLog(Object msg, boolean ln) {
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
        magicLog(msg, true);
    }
    public static void magicError(Object msg, boolean ln) {
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