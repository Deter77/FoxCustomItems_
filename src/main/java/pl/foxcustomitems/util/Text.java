package pl.foxcustomitems.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Text {
    private static final Pattern HEX_PATTERN = Pattern.compile("&x(&[0-9a-fA-F]){6}");

    private Text() {
    }

    public static String color(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String sequence = matcher.group();
            String hex = sequence.replace("&x", "").replace("&", "");
            matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
