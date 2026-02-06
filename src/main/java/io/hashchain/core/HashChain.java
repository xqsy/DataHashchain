package io.hashchain.core;

import io.hashchain.utils.HashUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class HashChain {
    private final ObservableList<HashRecord> chain = FXCollections.observableArrayList();

    public void addRecord(PersonData data) {
        String previousHash = chain.isEmpty() ? null : chain.get(chain.size() - 1).getHash();
        HashRecord record = new HashRecord(data, previousHash);
        chain.add(record);
    }

    public ObservableList<HashRecord> getChain() {
        return chain;
    }

    public String computeChainHash() {
        StringBuilder sb = new StringBuilder();
        for (HashRecord record : chain) {
            sb.append(record.getHash());
        }
        return HashUtils.calculateSHA256(sb.toString());
    }
}
