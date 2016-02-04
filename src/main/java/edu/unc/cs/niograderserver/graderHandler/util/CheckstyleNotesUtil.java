package edu.unc.cs.niograderserver.graderHandler.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Andrew Vitkus
 */
public class CheckstyleNotesUtil {
    
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    
    public static INoteData readCheckstyleNotes(Path checkstylePath) throws IOException {
        INoteData notes = new NoteData();
        if(Files.exists(checkstylePath)) {
            
            List<String> lines = Files.readAllLines(checkstylePath);
            lines.remove(0);
            lines.remove(lines.size() - 1);
            String[][] dataArr = lines.stream()
                    .map(CheckstyleNotesUtil::getParts)
                    .sorted((s1, s2)-> {
                        if (!s1[2].equals(s2[2])) {
                            String[] p1 = s1[2].split("\\.");
                            String[] p2 = s2[2].split("\\.");
                            
                            boolean p1Main = p1[0].equals("main");
                            boolean p2Main = p2[0].equals("main");
                            if (p1Main && !p2Main) {
                                return 1;
                            } else if (p2Main && !p1Main) {
                                return -1;
                            }
                            
                            for(int i = 0; i < Math.min(p1.length, p2.length); i ++) {
                                int c = p1[i].compareTo(p2[i]);
                                if (c != 0) {
                                    return c;
                                }
                            }
                        }
                        
                        int c = strIntComp(s1[0], s2[0]);
                        
                        if (c != 0) {
                            return c;
                        }
                        
                        c = strIntComp(s1[1], s2[1]);
                        
                        if (c != 0) {
                            return s1[4].compareTo(s2[4]);
                        }
                        return 0;
                    }).toArray(String[][]::new);
            for(String[] data : dataArr) {
                notes.addSection(data[2]);
                notes.addPart(data[3]);
                notes.addNote(data[4]);
            }
        }
        
        return notes;
    }
    
    private static String[] getParts(String line) {
        int srcIndex = line.indexOf(FILE_SEPARATOR + "src" + FILE_SEPARATOR);
        
        line = line.substring(srcIndex + 3 + FILE_SEPARATOR.length()*2);
        String[] split1 = line.split(" ", 2);
        String[] split2 = split1[0].split(":");
        System.out.println(line);
        System.out.println(Arrays.toString(split1));
        System.out.println(Arrays.toString(split2));
        
        String[] parts = new String[5];
        
        parts[2] = split2[0].split("\\.")[0].replaceAll(FILE_SEPARATOR, ".");
        parts[0] = split2[1];
        parts[3] = "Line " + split2[1];
        if (split2.length > 2 && !split2[2].isEmpty()) {
            parts[3] += " Column " + split2[2];
            parts[1] = split2[2];
        } else {
            parts[1] = "0";
        }
        parts[4] = split1[1];
        System.out.println(Arrays.toString(parts));
        
        return parts;
    }
    
    private static int strIntComp(String a, String b) {
        int ia = Integer.parseInt(a);
        int ib = Integer.parseInt(b);
        return Integer.compare(ia, ib);
    }
}
