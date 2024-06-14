package cz.muni.ics.kypo.training.utils;

import cz.muni.ics.kypo.training.api.enums.NormalizationStrategy;
import org.apache.commons.math3.stat.clustering.Clusterable;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Util methods for feature normalization and other math functions.
 */

public class ClusterMathUtils {

    private ClusterMathUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Simple 2D distance calculation
     *
     * @param firstX  first X coordinate
     * @param secondX second X coordinate
     * @param firstY  first Y coordinate
     * @param secondY second X coordinate
     * @return distance between given points
     */
    public static Double calculateDistance2D(Double firstX, Double secondX, Double firstY, Double secondY) {
        return FastMath.sqrt(
                FastMath.pow(FastMath.abs(firstX - secondX), 2)
                        + FastMath.pow(FastMath.abs(firstY - secondY), 2));
    }

    public static <T extends Clusterable<T>> List<T> normalize(NormalizationStrategy strategy, List<T> sample, Pair<Function<T, Double>, BiConsumer<T, Double>>... suppliers) {
        return switch (strategy) {
            case MIN_MAX -> normalizeClusterable(sample, ClusterMathUtils::minMaxNormalize, suppliers);
            case Z_SCORE -> normalizeClusterable(sample, ClusterMathUtils::zScoreNormalize, suppliers);
        };
    }

    public static List<Double> normalize(NormalizationStrategy strategy, List<Double> sample) {
        return switch (strategy) {
            case MIN_MAX -> minMaxNormalize(sample);
            case Z_SCORE -> zScoreNormalize(sample);
        };
    }


    /**
     * Normalizes the given sample using given normalization.
     *
     * @param sample     list of given clusterables
     * @param normalizer function to normalize values
     * @param suppliers  functions to extract and insert values into objects
     * @param <T>        clusterable type
     * @return list of clusterables with normalized values
     */
    @SafeVarargs
    private static <T extends Clusterable<T>> List<T> normalizeClusterable(List<T> sample,
                                                                           UnaryOperator<List<Double>> normalizer,
                                                                           Pair<Function<T, Double>,
                                                                                   BiConsumer<T, Double>>... suppliers) {
        for (Pair<Function<T, Double>, BiConsumer<T, Double>> supplierConsumerPair : suppliers) {
            List<Double> values = sample.stream()
                    .map(clusterable -> supplierConsumerPair.getFirst().apply(clusterable))
                    .toList();

            List<Double> normalizedValues = normalizer.apply(values);
            for (int i = 0; i < sample.size(); i++) {
                supplierConsumerPair.getSecond().accept(sample.get(i), normalizedValues.get(i));
            }
        }
        return sample;
    }

    public static double handleNaN(Double value) {
        return Double.isNaN(value) ? 0.0 : value;
    }

    /**
     * Normalize the given array of double values using Z-score normalization.
     *
     * @param sample array of double values
     * @return normalized array
     */
    private static List<Double> zScoreNormalize(List<Double> sample) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (double value : sample) {
            stats.addValue(value);
        }

        double mean = handleNaN(stats.getMean());
        double standardDeviation = handleNaN(stats.getStandardDeviation());
        double nonZeroStandardDeviation = standardDeviation == 0.0 ? 1.0 : standardDeviation;

        return sample.stream()
                .map(value -> (value - mean) / nonZeroStandardDeviation)
                .toList();
    }


    /**
     * Normalizes the given array of double values using Min-max normalization.
     *
     * @param sample array of double values
     * @return normalized array
     */
    private static List<Double> minMaxNormalize(List<Double> sample) {
        double min = sample.stream().min(Double::compareTo).orElse(0.0);
        double max = sample.stream().max(Double::compareTo).orElse(0.0);

        if (min == max) {
            return sample;
        }

        return sample.stream()
                .map(value -> (value - min) / (max - min))
                .toList();
    }

}
