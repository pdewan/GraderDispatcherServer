package edu.unc.cs.niograderserver.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import javax.net.ssl.HttpsURLConnection;

public class URLConnectionHelper {

    public static String getResponse(HttpsURLConnection connection) throws IOException {
        byte[] bytes = new byte[512];
        try (BufferedInputStream bis = new BufferedInputStream(connection.getInputStream())) {
            StringBuilder response = new StringBuilder(30);
            int in;
            while ((in = bis.read(bytes)) != -1) {
                response.append(new String(bytes, 0, in));
            }
            return response.toString();
        }
    }

    private URLConnectionHelper() {
    }
}
