package edu.unc.cs.niograderserver.graderHandler.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Andrew Vitkus
 */
public class PendingSubmissionManger {
    private static final Map<String, ArrayList<String[]>> submissions;
    
    static {
        submissions = Collections.synchronizedMap(new HashMap<>(10));
    }
    
    public static synchronized void addSubmission(String uid, String course, String section, String assignment) {
        System.out.println("Add: " + uid + " - " + course + "-" + section + " " + assignment);
        if (submissions.containsKey(uid)) {
            ArrayList<String[]> userSubmissions = submissions.get(uid);
            userSubmissions.add(new String[]{course, section, assignment});
        } else {
            ArrayList<String[]> userSubmissions = new ArrayList<>();
            userSubmissions.add(new String[]{course, section, assignment});
            submissions.put(uid, userSubmissions);
        }
    }
    
    public static synchronized void removeSubmission(String uid, String course, String section, String assignment) {
        System.out.println("Remove: " + uid + " - " + course + "-" + section + " " + assignment);
        if (submissions.containsKey(uid)) {
            ArrayList<String[]> userSubmissions = new ArrayList<>();
            userSubmissions.add(new String[]{course, section, assignment});
            int index = arrIndex(userSubmissions, new String[]{course, section, assignment});
            if (index >= 0) {
                userSubmissions.remove(index);
                if (userSubmissions.isEmpty()) {
                    System.out.println("Removed: " + index);
                    submissions.remove(uid);
                }
            }
        }
    }
    
    public static synchronized boolean isPending(String uid, String course, String section, String assignment) {
        System.out.println("Check Pending: " + uid + " - " + course + "-" + section + " " + assignment);
        if (submissions.containsKey(uid)) {
            ArrayList<String[]> userSubmissions = submissions.get(uid);
            System.out.println(arrIndex(userSubmissions, new String[]{course, section, assignment}) >= 0);
            return arrIndex(userSubmissions, new String[]{course, section, assignment}) >= 0;
        } else {
            System.out.println(false);
            return false;
        }
    }
    
    private static int arrIndex(ArrayList<String[]> list, String[] arr) {
        for(int i = 0; i < list.size(); i ++) {
            if (Arrays.equals(list.get(i), arr)) {
                return i;
            }
        }
        return -1;
    }
}
