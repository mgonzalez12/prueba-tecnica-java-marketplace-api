package com.marketplace.domain.service;

import com.marketplace.domain.model.Product;
import org.springframework.stereotype.Service;

@Service
public class ProductDomainService {

    /**
     * Simulates a complex matrix calculation for product similarity or
     * classification.
     * Multiplies the product feature vector by a weight matrix.
     * 
     * @param product The product to process.
     * @param weights The weight matrix (must match feature dimension).
     * @return The resulting score vector.
     */
    public double[] calculateProductScore(Product product, double[][] weights) {
        double[] features = product.getFeatures();
        if (features == null || weights == null) {
            throw new IllegalArgumentException("Features and weights cannot be null");
        }

        int featureLength = features.length;
        int rows = weights.length;
        int cols = weights[0].length;

        if (cols != featureLength) {
            throw new IllegalArgumentException("Matrix columns must match feature vector length");
        }

        double[] result = new double[rows];

        // Matrix-Vector Multiplication
        for (int i = 0; i < rows; i++) {
            double sum = 0;
            for (int j = 0; j < cols; j++) {
                sum += weights[i][j] * features[j];
            }
            result[i] = sum;
        }

        return result;
    }
}
