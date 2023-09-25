package com.atypon.node.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectIdGenerator {
    private static final int TIMESTAMP_BYTES = 4;
    private static final int RANDOM_BYTES = 5;
    private static final int MACHINE_BYTES = 3;

    private static final int RANDOM_OFFSET = TIMESTAMP_BYTES;
    private static final int MACHINE_OFFSET = RANDOM_OFFSET + RANDOM_BYTES;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final byte[] machineIdentifier = createMachineIdentifier();


    private static byte[] createMachineIdentifier() {
        byte[] machineIdentifier = new byte[MACHINE_BYTES];
        secureRandom.nextBytes(machineIdentifier);
        return machineIdentifier;
    }

    private static byte[] generateObjectId() {
        byte[] objectId = new byte[12]; // ObjectId is 12 bytes

        int timestamp = (int) (System.currentTimeMillis() / 1000L);
        byte[] timestampBytes = intToByteArray(timestamp);
        System.arraycopy(timestampBytes, 0, objectId, 0, TIMESTAMP_BYTES);

        System.arraycopy(machineIdentifier, 0, objectId, MACHINE_OFFSET, MACHINE_BYTES);

        byte[] randomBytes = new byte[RANDOM_BYTES];
        secureRandom.nextBytes(randomBytes);
        System.arraycopy(randomBytes, 0, objectId, RANDOM_OFFSET, RANDOM_BYTES);

        return objectId;
    }

    private static byte[] intToByteArray(int value) {
        byte[] bytes = new byte[4];
        for (int i = 3; i >= 0; i--) {
            bytes[i] = (byte) (value & 0xFF);
            value >>>= 8;
        }
        return bytes;
    }

    public static String getNewID(){
        StringBuilder builder = new StringBuilder();
        byte[] objectId = generateObjectId();
        for (byte b : objectId) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }
}
