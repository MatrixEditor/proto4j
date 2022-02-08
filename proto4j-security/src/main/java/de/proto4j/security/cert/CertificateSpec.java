package de.proto4j.security.cert;//@date 08.02.2022

import java.io.Serializable;
import java.security.PublicKey;

public interface CertificateSpec extends Serializable {

    PublicKey getPublicKey();

    String getName();

    String getFormat();

}
