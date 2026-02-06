package io.hashchain.utils;

import io.hashchain.core.FingerprintDot;
import io.hashchain.core.FingerprintType;
import io.hashchain.core.HashChain;
import io.hashchain.core.HashRecord;
import io.hashchain.core.PersonData;
import io.hashchain.core.SecurityConfig;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ChainStorage {

    private static final Path DEFAULT_CHAIN_FILE = Path.of(System.getProperty("user.dir"), "hashchain.json");
    private static final Path BACKUP_CHAIN_FILE = Path.of(System.getProperty("user.dir"), "hashchain_backup.json");

    public static class LoadResult {
        public final boolean success;
        public final String errorMessage;

        private LoadResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public static LoadResult ok() {
            return new LoadResult(true, null);
        }

        public static LoadResult error(String message) {
            return new LoadResult(false, message);
        }
    }

    public static void save(HashChain hashChain) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(DEFAULT_CHAIN_FILE, StandardCharsets.UTF_8)) {
            String chainHash = hashChain.computeChainHash();
            String hmac = HashUtils.calculateHmacSHA256(SecurityConfig.getHmacSecret(), chainHash);

            writer.write("{\n");
            writer.write("  \"records\": [\n");

            for (int i = 0; i < hashChain.getChain().size(); i++) {
                HashRecord record = hashChain.getChain().get(i);
                PersonData data = record.getData();

                writer.write("    {\n");
                writer.write("      \"hash\": \"" + escapeJson(record.getHash()) + "\",\n");

                String prev = record.getPreviousHash();
                if (prev == null) {
                    writer.write("      \"previousHash\": null,\n");
                } else {
                    writer.write("      \"previousHash\": \"" + escapeJson(prev) + "\",\n");
                }

                writer.write("      \"person\": \"" + escapeJson(data.toString()) + "\"\n");
                writer.write("    }");

                if (i < hashChain.getChain().size() - 1) {
                    writer.write(",");
                }
                writer.newLine();
            }

            writer.write("  ],\n");
            writer.write("  \"chainHash\": \"" + escapeJson(chainHash) + "\",\n");
            writer.write("  \"hmac\": \"" + escapeJson(hmac) + "\"\n");
            writer.write("}\n");
        }

        Files.copy(DEFAULT_CHAIN_FILE, BACKUP_CHAIN_FILE, StandardCopyOption.REPLACE_EXISTING);
    }

    public static LoadResult load(HashChain hashChain) {
        if (!Files.exists(DEFAULT_CHAIN_FILE)) {
            return LoadResult.ok();
        }

        try {
            List<String> lines = Files.readAllLines(DEFAULT_CHAIN_FILE, StandardCharsets.UTF_8);
            hashChain.getChain().clear();

            String chainHashFromFile = null;
            String hmacFromFile = null;

            for (String line : lines) {
                String trimmed = line.trim();

                if (trimmed.startsWith("\"person\"")) {
                    String value = extractJsonValue(trimmed);
                    if (value != null) {
                        PersonData person = parsePersonString(value);
                        if (person != null) {
                            hashChain.addRecord(person);
                        }
                    }
                } else if (trimmed.startsWith("\"chainHash\"")) {
                    chainHashFromFile = extractJsonValue(trimmed);
                } else if (trimmed.startsWith("\"hmac\"")) {
                    hmacFromFile = extractJsonValue(trimmed);
                }
            }

            if (chainHashFromFile != null && hmacFromFile != null) {
                String expectedHmac = HashUtils.calculateHmacSHA256(SecurityConfig.getHmacSecret(), chainHashFromFile);
                if (!expectedHmac.equalsIgnoreCase(hmacFromFile)) {
                    hashChain.getChain().clear();
                    return LoadResult.error("Подпись HMAC не совпадает. Файл цепочки повреждён или подделан.");
                }

                String actualChainHash = hashChain.computeChainHash();
                if (!actualChainHash.equals(chainHashFromFile)) {
                    hashChain.getChain().clear();
                    return LoadResult.error("Хеш цепочки не соответствует данным в файле. Файл повреждён или подделан.");
                }
            }

            return LoadResult.ok();
        } catch (IOException e) {
            return LoadResult.error("Не удалось загрузить цепочку из файла: " + e.getMessage());
        }
    }

    private static String extractJsonValue(String line) {
        int colonIndex = line.indexOf(':');
        int firstQuote = line.indexOf('"', colonIndex + 1);
        int lastQuote = line.lastIndexOf('"');
        if (firstQuote >= 0 && lastQuote > firstQuote) {
            return line.substring(firstQuote + 1, lastQuote);
        }
        return null;
    }

    private static PersonData parsePersonString(String s) {
        try {
            int fpIndex = s.indexOf(" Fingerprint[");
            if (fpIndex < 0) {
                return null;
            }

            String personPart = s.substring(0, fpIndex);
            String fingerprintPart = s.substring(fpIndex + " Fingerprint[".length(), s.length() - 1);

            String[] personTokens = personPart.split(" ");
            if (personTokens.length < 4) {
                return null;
            }

            String firstName = personTokens[0];
            String patronymic = personTokens[1];
            String lastName = personTokens[2];
            String birthDateStr = personTokens[3];

            LocalDate birthDate = LocalDate.parse(birthDateStr);

            String[] fpTokens = fingerprintPart.split(",");
            int x = 0;
            int y = 0;
            int quality = 0;
            FingerprintType type = FingerprintType.DOT;

            for (String token : fpTokens) {
                String[] kv = token.trim().split("=");
                if (kv.length != 2) {
                    continue;
                }
                String key = kv[0].trim();
                String value = kv[1].trim();
                switch (key) {
                    case "x" -> x = Integer.parseInt(value);
                    case "y" -> y = Integer.parseInt(value);
                    case "type" -> type = FingerprintType.valueOf(value);
                    case "quality" -> quality = Integer.parseInt(value);
                }
            }

            FingerprintDot dot = new FingerprintDot(x, y, type, quality);
            return new PersonData(firstName, lastName, patronymic, birthDate, dot);
        } catch (DateTimeParseException | IllegalArgumentException e) {
            return null;
        }
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
