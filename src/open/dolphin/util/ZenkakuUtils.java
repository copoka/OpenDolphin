/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package open.dolphin.util;

/**
 *
 * @author kazm
 */
public class ZenkakuUtils {

    private static final char[] MATCHIES = {'�O', '�P', '�Q', '�R', '�S', '�T', '�U', '�V', '�W', '�X', '�@', '��', '��', '�D', '�{', '�['};
    private static final char[] REPLACES = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ', 'm', 'g', '.', '+', '-'};

    public static String toHankuNumber(String test) {
        for (int i = 0; i < MATCHIES.length; i++) {
            test = test.replace(MATCHIES[i], REPLACES[i]);
        }
        return test;
    }
}
