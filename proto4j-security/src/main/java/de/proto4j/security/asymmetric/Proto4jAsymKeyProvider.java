package de.proto4j.security.asymmetric; //@date 08.02.2022

import de.proto4j.security.cert.CertificateSpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

public final class Proto4jAsymKeyProvider {

    private Proto4jAsymKeyProvider() {}

    public static synchronized KeyPair newProto4jKeyPair() throws KeyException, NoSuchAlgorithmException {
        return createKeyPair(2048*2, "RSA");
    }

    public static KeyPair createKeyPair(String algorithm) throws KeyException, NoSuchAlgorithmException {
        return createKeyPair(-1, algorithm);
    }

    public static KeyPair createKeyPair(String algorithm, KeyPairGeneratorFactory factory) throws KeyException,
            NoSuchAlgorithmException {
        return createKeyPair(-1, algorithm, factory);
    }

    public static KeyPair createKeyPair(int length, String algorithm) throws KeyException, NoSuchAlgorithmException {
        return createKeyPair(length, algorithm, KeyPairGenerator::getInstance);
    }

    public static KeyPair createKeyPair(int length, String algorithm, KeyPairGeneratorFactory factory)
            throws KeyException, NoSuchAlgorithmException {
        if (length == 0 || algorithm == null || algorithm.length() == 0) {
            throw new KeyException("invalid key-length or algorithm");
        }

        KeyPairGenerator gen = factory.newKeyPairGenerator(algorithm);
        if (length != -1) {
            gen.initialize(length);
        }

        KeyPair kp = gen.generateKeyPair();
        if (kp == null) {
            throw new IllegalArgumentException("could not create keys");
        }
        return kp;
    }

    public static Cipher getCipherInstance() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance("RSA");
    }

    public static CertificateSpec getInstance(PublicKey key) {
        return new CertificateSpecImpl(key, "PROTO4J/CERT", "RSA");
    }

    private static class CertificateSpecImpl implements CertificateSpec {

        private final PublicKey key;
        private final String name;
        private final String format;

        public CertificateSpecImpl(PublicKey key, String name, String format) {
            this.key    = key;
            this.name   = name;
            this.format = format;
        }

        @Override
        public PublicKey getPublicKey() {
            return key;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getFormat() {
            return format;
        }
    }
}
