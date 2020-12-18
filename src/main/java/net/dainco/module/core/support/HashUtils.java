package net.dainco.module.core.support;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;

public final class HashUtils {
  private static final String ALGORITHM_SHA = "SHA-224";

  public static String getHash(String... values) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance(ALGORITHM_SHA);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException(e);
    }
    for (String value : values) {
      if (value != null) {
        digest.update(value.getBytes());
      }
    }
    return Hex.encodeHexString(digest.digest());
  }
}
