package edu.unc.cs.niograderserver.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;

/**
 * @author Andrew Vitkus
 *
 */
public class KeyManagerUtil {

    public static KeyManagerFactory getKeyManagerFactory(String keystoreFileName, String keystorePassword) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, CertificateException, IOException {
        return getKeyManagerFactory(keystoreFileName, keystorePassword, keystorePassword);
    }

    public static KeyManagerFactory getKeyManagerFactory(String keystoreFileName, String keystorePassword, String keyPassword) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, CertificateException, IOException {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(setupKeyStore(keystoreFileName, keystorePassword), keyPassword.toCharArray());
        return kmf;
    }

    private static KeyStore setupKeyStore(String keystoreFileName, String keystorePassword) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] password = keystorePassword.toCharArray();

        java.io.FileInputStream fis = null;
        try {
            fis = new FileInputStream(keystoreFileName);
            ks.load(fis, password);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return ks;
    }
}
