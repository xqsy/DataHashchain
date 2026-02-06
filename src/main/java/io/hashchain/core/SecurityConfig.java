package io.hashchain.core;

// import io.github.cdimascio.dotenv.Dotenv;

public class SecurityConfig {

    private static final String DEFAULT_HMAC_SECRET = "keyyyyyy1231232123141521452";

/**    private static final Dotenv DOTENV = Dotenv
            .configure()
            .ignoreIfMissing()
            .load(); */

    public static String getHmacSecret() {
      /**   String fromEnvFile = DOTENV.get("HASHCHAIN_HMAC_SECRET");
        if (fromEnvFile != null && !fromEnvFile.isBlank()) {
            return fromEnvFile;
        } 
           */ 

        return DEFAULT_HMAC_SECRET;
    }
}
