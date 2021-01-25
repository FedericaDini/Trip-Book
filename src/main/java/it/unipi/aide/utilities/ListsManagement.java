package it.unipi.aide.utilities;

import java.util.HashMap;

public class ListsManagement {
    public static int showResults(HashMap<String, String> results) {

        if (results.isEmpty()) {
            System.out.println("No results found!");
            return 0;
        } else {
            int i = 0;
            for (String value : results.values()) {
                i++;
                System.out.println(i + " --> " + value);
            }
            return i;
        }
    }

    public static void showValues(HashMap<String, String> results) {

        if (results.isEmpty()) {
            System.out.println("No results found!");
        } else {
            for (String value : results.values()) {
                System.out.println(value);
            }
        }
    }

    public static String retrieveIdFromSelectedRaw(HashMap<String, String> results, int index) {

        int i = 0;
        String k = null;
        for (String key : results.keySet()) {
            i++;
            if (i == index) {
                k = key;
                break;
            }
        }
        return k;
    }
}
