package de.proto4j.security.asymmetric;//@date 08.02.2022

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public interface KeyPairGeneratorFactory {

    public KeyPairGenerator newKeyPairGenerator(String algorithm) throws NoSuchAlgorithmException;
}
