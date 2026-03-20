package com.codeshield.analysis;

public class TDICalculator {

    public static final double HIGH_RISK_THRESHOLD = 50.0;

    public static double calculate(double complexityScore, double vulnerabilityDensity) {
        return Math.round(((complexityScore * 0.5) + (vulnerabilityDensity * 0.5)) * 100.0) / 100.0;
    }

    public static String classifyRisk(double tdi) {
        if (tdi >= HIGH_RISK_THRESHOLD) {
            return "Extremely Complex / High Risk - Immediate Refactoring Recommended";
        } else if (tdi >= 30) {
            return "Moderate Risk - Refactoring Advised";
        } else if (tdi >= 10) {
            return "Low Risk - Monitor";
        } else {
            return "Minimal Risk - Acceptable";
        }
    }

    public static boolean isHighRisk(double tdi) {
        return tdi >= HIGH_RISK_THRESHOLD;
    }
}
