package com.uonagent.mathlibrary;

import java.security.SecureRandom;
import java.util.Random;

public class RandomString {
    public static String get(int lenght) {
        SecureRandom random = new SecureRandom();
        StringBuffer out = new StringBuffer(lenght);
        int max = 126;
        int min = 33;
        for (int i = 0; i < lenght; ++i) {
            out.append((char)(random.nextInt(max - min + 1) + min));
        }
        int i = random.nextInt(max - min + 1) + min;
        return out.toString();
    }
}
