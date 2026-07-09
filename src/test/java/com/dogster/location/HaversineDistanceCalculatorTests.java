package com.dogster.location;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HaversineDistanceCalculatorTests {

    private final HaversineDistanceCalculator calculator = new HaversineDistanceCalculator();

    @Test
    void returnsZeroForSameLocation() {
        double distanceKm = calculator.calculateKm(41.0082, 28.9784, 41.0082, 28.9784);

        assertThat(distanceKm).isZero();
    }

    @Test
    void calculatesDistanceBetweenIstanbulAndAnkara() {
        double distanceKm = calculator.calculateKm(41.0082, 28.9784, 39.9208, 32.8541);

        assertThat(distanceKm).isBetween(349.0, 360.0);
    }
}
