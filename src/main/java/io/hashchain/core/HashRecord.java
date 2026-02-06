package io.hashchain.core;

import io.hashchain.utils.HashUtils;

public class HashRecord {
    private final PersonData data;
    private final String hash;
    private final String previousHash;

    public HashRecord(PersonData data, String previousHash) {
        this.data = data;
        this.previousHash = previousHash;
        this.hash = HashUtils.calculateSHA256(
                data.toString() + (previousHash == null ? "" : previousHash)
        );
    }

    public String getHash() { return hash; }
    public String getPreviousHash() { return previousHash; }
    public PersonData getData() { return data; }
}
