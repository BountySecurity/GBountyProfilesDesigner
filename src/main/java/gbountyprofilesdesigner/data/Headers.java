package gbountyprofilesdesigner.data;

public class Headers {
    public final String type;
    public final String match;
    public final String replace;
    public final String regex;

    public Headers(String type, String match, String replace, String regex) {
        this.type = type;
        this.match = match;
        this.replace = replace;
        this.regex = regex;
    }
}
