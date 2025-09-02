package org.sbpo2025.challenge;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Supplier;
public record GAParameters(
        int ngen,
        int qGenWithoutImprovement,
        int psize,
        double pbetterParent,
        double eliteFraction,
        double mutationFraction
) {
    private static final int DEFAULT_NGEN = 600;
    private static final int DEFAULT_QGEN = 80;
    private static final int DEFAULT_PSIZE = 600;
    private static final double DEFAULT_PBETTER_PARENT = 0.7;
    private static final double DEFAULT_ELITE_FRACTION = 0.1;
    private static final double DEFAULT_MUTATION_FRACTION = 0.4;
    private static final String ENV_FILE_PATH = ".env";

    private static GAParameters loadFromEnvFile(String envFp) throws IOException{
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(envFp)) {
            props.load(fis);
        }
        int ngen = parseInt(() -> props.getProperty("ngen"), DEFAULT_NGEN);
        int qGenWithoutImprovement = parseInt(() -> props.getProperty("qGenWithoutImprovement"), DEFAULT_QGEN);
        int psize = parseInt(() -> props.getProperty("psize"), DEFAULT_PSIZE);
        double pbetterParent = parseDouble(() -> props.getProperty("pbetterParent"), DEFAULT_PBETTER_PARENT);
        double eliteFraction = parseDouble(() -> props.getProperty("eliteFraction"), DEFAULT_ELITE_FRACTION);
        double mutationFraction = parseDouble(() -> props.getProperty("mutationFraction"), DEFAULT_MUTATION_FRACTION);
        return new GAParameters(ngen, qGenWithoutImprovement, psize, pbetterParent, eliteFraction, mutationFraction);
    }
    /**
     * Create a GAParameters instance from environment variables.
     * If any variable is missing or invalid, uses fallback default values.
     */
    public static GAParameters fromEnv() {
        Path envFile = Path.of(ENV_FILE_PATH);
        if (Files.exists(envFile)){
            try {
                System.err.println("Trying to load GAParameters from the '.env' file");
                return loadFromEnvFile(ENV_FILE_PATH);
            } catch (IOException e) {
                System.err.println("Failed to initialize GAParameters from the env file:");
                System.err.println(e.getMessage());
            }
        }
        System.err.println("Loading GAParameters from environment variables if they are provided.");
        System.err.println("Otherwise, the default ones will be used.");
        int ngen = parseInt(() -> System.getenv("ngen"), DEFAULT_NGEN);
        int qGenWithoutImprovement = parseInt(() -> System.getenv("qGenWithoutImprovement"), DEFAULT_QGEN);
        int psize = parseInt(() -> System.getenv("psize"), DEFAULT_PSIZE);
        double pbetterParent = parseDouble(() -> System.getenv("pbetterParent"), DEFAULT_PBETTER_PARENT);
        double eliteFraction = parseDouble(() -> System.getenv("eliteFraction"), DEFAULT_ELITE_FRACTION);
        double mutationFraction = parseDouble(() -> System.getenv("mutationFraction"), DEFAULT_MUTATION_FRACTION);

        return new GAParameters(ngen, qGenWithoutImprovement, psize,
                pbetterParent, eliteFraction, mutationFraction);
    }

    private static int parseInt(Supplier<String> varGetter, int fallback) {
        try {
            String val = varGetter.get();
            return (val != null) ? Integer.parseInt(val) : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double parseDouble(Supplier<String> varGetter, double fallback) {
        try {
            String val = varGetter.get();
            return (val != null) ? Double.parseDouble(val) : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
