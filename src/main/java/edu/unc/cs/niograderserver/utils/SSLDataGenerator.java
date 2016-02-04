package edu.unc.cs.niograderserver.utils;

import edu.unc.cs.httpserver.ssl.ISSLDataPackage;
import edu.unc.cs.httpserver.ssl.SSLDataPackage;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author Andrew Vitkus
 */
public class SSLDataGenerator {
    
    private static final String[] CIPHERS = {"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
        "TLS_RSA_WITH_AES_128_CBC_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
        "TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
        "TLS_ECDH_RSA_WITH_RC4_128_SHA"};
    private static final String KEYSTORE_FILENAME = "keystore.jks";
    private static final String KEYSTORE_PASSWORD = "1qaz3edc5tgb";
    private static final Logger LOG = Logger.getLogger(SSLDataGenerator.class.getName());
    private static final String[] PROTOCOLS = {"SSLv3",
        "TLSv1",
        "TLSv1.1",
        "TLSv1.2"};
    private static final String SSL_PROTOCOL = "TLSv1";
    
    public static ISSLDataPackage getDefaultSSLData() throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, CertificateException, IOException {
        KeyManagerFactory kmf = KeyManagerUtil.getKeyManagerFactory(KEYSTORE_FILENAME, KEYSTORE_PASSWORD);
        TrustManagerFactory tmf = TrustManagerUtil.getTrustManagerFactory(KEYSTORE_FILENAME, KEYSTORE_PASSWORD);
        return new SSLDataPackage(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom(), SSL_PROTOCOL);
    }
}
