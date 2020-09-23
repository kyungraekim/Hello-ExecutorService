package org.kyungrae.practice.threadpool;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class Result {
    @Getter
    @Setter
    private double first;
    @Getter
    @Setter
    private double second;
    @Getter
    @Setter
    private double third;

    @Override
    public String toString() {
        return String.format("%f,%f,%f", first, second, third);
    }
}
