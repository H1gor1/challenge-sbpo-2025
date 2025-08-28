package org.sbpo2025.challenge;

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

    /**
     * Create a GAParameters instance from environment variables.
     * If any variable is missing or invalid, uses fallback default values.
     */
    public static GAParameters fromEnv() {
        int ngen = parseEnvInt("ngen", DEFAULT_NGEN);
        int qGenWithoutImprovement = parseEnvInt("qGenWithoutImprovement", DEFAULT_QGEN);
        int psize = parseEnvInt("psize", DEFAULT_PSIZE);
        double pbetterParent = parseEnvDouble("pbetterParent", DEFAULT_PBETTER_PARENT);
        double eliteFraction = parseEnvDouble("eliteFraction", DEFAULT_ELITE_FRACTION);
        double mutationFraction = parseEnvDouble("mutationFraction", DEFAULT_MUTATION_FRACTION);

        return new GAParameters(ngen, qGenWithoutImprovement, psize,
                pbetterParent, eliteFraction, mutationFraction);
    }

    private static int parseEnvInt(String var, int fallback) {
        try {
            String val = System.getenv(var);
            return (val != null) ? Integer.parseInt(val) : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double parseEnvDouble(String var, double fallback) {
        try {
            String val = System.getenv(var);
            return (val != null) ? Double.parseDouble(val) : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
