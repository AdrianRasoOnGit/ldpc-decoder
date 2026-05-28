package ldpc.simulation;

import ldpc.matrix.CsrMatrix;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BerSimulationTest {
    @Test
    void computesSigmaFromEbN0() {
        float sigma = BerSimulation.sigmaFromEbN0(0.0, 0.5);

        assertEquals(1.0f, sigma, 1e-6f);
    }

    @Test
    void runsSmallSimulation() {
        CsrMatrix h = CsrMatrix.fromRows(3, 6, new int[][] {
                {0, 1, 3},
                {1, 2, 4},
                {0, 2, 5}
        });

        SimulationConfig config = new SimulationConfig(
                0.5,
                10,
                10,
                new double[] {2.0},
                42L
        );

        BerSimulation simulation = new BerSimulation(h, config);
        List<SimulationResult> results = simulation.run();

        assertEquals(1, results.size());
        assertEquals(10, results.get(0).trials());
        assertEquals(60, results.get(0).totalBits());
    }
}
