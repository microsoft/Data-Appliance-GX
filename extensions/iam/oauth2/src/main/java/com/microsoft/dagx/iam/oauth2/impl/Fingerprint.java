/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

package com.microsoft.dagx.iam.oauth2.impl;

import com.microsoft.dagx.spi.DagxException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Produces SHA-1 fingerprints.
 */
public class Fingerprint {
    private static final char[] HEX_CODES = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Produces a SHA1 fingerprint of the given bytes using HEX encoding. Used for the x5t claim in a JWT.
     */
    public static String sha1HexFingerprint(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] data = md.digest(bytes);
            char[] encoded = new char[data.length << 1];
            for (int i = 0, encodedPos = 0; i < data.length; i++) {
                encoded[encodedPos++] = HEX_CODES[(0xF0 & data[i]) >>> 4];
                encoded[encodedPos++] = HEX_CODES[0x0F & data[i]];
            }
            return new String(encoded);
        } catch (NoSuchAlgorithmException e) {
            throw new DagxException(e);
        }
    }

    /**
     * Produces a SHA1 fingerprint of the given bytes using Base 64 encoding. Used for the x5t claim in a JWT.
     */
    public static String sha1Base64Fingerprint(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(bytes);
            return Base64.getEncoder().encodeToString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new DagxException(e);
        }
    }

    private Fingerprint() {
    }
}
