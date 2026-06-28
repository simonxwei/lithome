package io.github.simonxwei.lithome.world.level.levelgen.synth;

/**
 * @author simonxwei
 */
public final class NoiseFractions {

    private static final double NORMAL_NOISE_TARGET_DEVIATION = 1.0D / 3.0D;
    private static final double LOWER_REGION = 0.02425D;
    private static final double UPPER_REGION = 1.0D - LOWER_REGION;

    private NoiseFractions() {}

    // public

    public static double upperTailThreshold(final float targetFraction) {
        if (targetFraction <= 0.0F) {
            return Double.POSITIVE_INFINITY;
        }
        if (targetFraction >= 1.0F) {
            return Double.NEGATIVE_INFINITY;
        }
        return NORMAL_NOISE_TARGET_DEVIATION * inverseStandardNormal(1.0D - targetFraction);
    }

    // core

    private static double inverseStandardNormal(final double probability) {
        final double a1 = -3.969683028665376E+01D;
        final double a2 = 2.209460984245205E+02D;
        final double a3 = -2.759285104469687E+02D;
        final double a4 = 1.383577518672690E+02D;
        final double a5 = -3.066479806614716E+01D;
        final double a6 = 2.506628277459239E+00D;
        final double b1 = -5.447609879822406E+01D;
        final double b2 = 1.615858368580409E+02D;
        final double b3 = -1.556989798598866E+02D;
        final double b4 = 6.680131188771972E+01D;
        final double b5 = -1.328068155288572E+01D;
        final double c1 = -7.784894002430293E-03D;
        final double c2 = -3.223964580411365E-01D;
        final double c3 = -2.400758277161838E+00D;
        final double c4 = -2.549732539343734E+00D;
        final double c5 = 4.374664141464968E+00D;
        final double c6 = 2.938163982698783E+00D;
        final double d1 = 7.784695709041462E-03D;
        final double d2 = 3.224671290700398E-01D;
        final double d3 = 2.445134137142996E+00D;
        final double d4 = 3.754408661907416E+00D;

        if (probability < LOWER_REGION) {
            final double q = Math.sqrt(-2.0D * Math.log(probability));
            return (((((c1 * q + c2) * q + c3) * q + c4) * q + c5) * q + c6) / ((((d1 * q + d2) * q + d3) * q + d4) * q + 1.0D);
        }

        if (probability > UPPER_REGION) {
            final double q = Math.sqrt(-2.0D * Math.log(1.0D - probability));
            return -(((((c1 * q + c2) * q + c3) * q + c4) * q + c5) * q + c6) / ((((d1 * q + d2) * q + d3) * q + d4) * q + 1.0D);
        }

        final double q = probability - 0.5D;
        final double r = q * q;
        return (((((a1 * r + a2) * r + a3) * r + a4) * r + a5) * r + a6) * q / (((((b1 * r + b2) * r + b3) * r + b4) * r + b5) * r + 1.0D);
    }
}
