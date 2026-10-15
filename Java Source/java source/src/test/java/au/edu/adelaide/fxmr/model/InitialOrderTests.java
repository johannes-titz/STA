package au.edu.adelaide.fxmr.model;

import static org.junit.Assert.*;
import java.util.HashSet;
import org.junit.Test;
import au.edu.adelaide.fxmr.joptimizer.functions.SimpleLinearConstraint;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

public class InitialOrderTests {
    @Test public void binomialEndpointLikelihoodIsExact() {
        au.edu.adelaide.fxmr.data.BinElement data = new au.edu.adelaide.fxmr.data.BinElement(
            new int[]{10,0}, new int[]{0,10});
        assertEquals(0, au.edu.adelaide.fxmr.model.bin.BinTrial.mleBN(data, new double[]{1,0}), 0);
        assertEquals(Double.POSITIVE_INFINITY,
            au.edu.adelaide.fxmr.model.bin.BinTrial.mleBN(data, new double[]{0,1}), 0);
    }
    @SuppressWarnings("unchecked")
    private CMRxProblem problem(double[][] means, boolean constrained, boolean cyclic,
                                 double[][] model) {
        DoubleMatrix2D[] weights = new DoubleMatrix2D[means.length];
        HashSet<SimpleLinearConstraint>[] adj = new HashSet[means.length];
        for (int v = 0; v < means.length; v++) {
            weights[v] = new DenseDoubleMatrix2D(new double[][]{{8, 0}, {0, 8}});
            adj[v] = new HashSet<SimpleLinearConstraint>();
            if (constrained) adj[v].add(new SimpleLinearConstraint(0, 1));
            if (cyclic) adj[v].add(new SimpleLinearConstraint(1, 0));
        }
        return new CMRxProblem(means, weights, adj, new DenseDoubleMatrix2D(model));
    }
    private void check(CMRSolution solution, double expected) {
        assertNotNull(solution);
        assertEquals(expected, solution.getFStar(), 1e-7);
        for (double[] row : solution.getXStar()) assertTrue(row[0] <= row[1] + 1e-8);
    }
    @Test public void serialHonorsInitialOrderBeforeZeroReturn() {
        check(new CMRxSolver().solve(problem(new double[][]{{2,1},{2,0}}, true, false,
            new double[][]{{1},{1}})), 20);
    }
    @Test public void parallelHonorsInitialOrderBeforeZeroReturn() {
        check(new ParCMRxSolver().solve(problem(new double[][]{{2,1},{2,0}}, true, false,
            new double[][]{{1},{1}}), false, 2), 20);
    }
    @Test public void approximateInitializerFitsItsRoot() {
        CMRxSolver solver = new CMRxSolver();
        solver.setOnlyFeas(true);
        check(solver.solve(problem(new double[][]{{2,1},{2,0}}, true, false,
            new double[][]{{1},{1}})), 20);
    }
    @Test public void higherRankGreedyInitializerFitsItsRoot() {
        CMRxSolver solver = new CMRxSolver();
        solver.setOnlyFeas(true);
        CMRxProblem p = problem(new double[][]{{2,1},{2,1},{2,1}}, true, false,
            new double[][]{{1,0},{0,1},{1,1}});
        assertTrue(p.getCv().length > 2);
        check(solver.solve(p), 12);
    }
    @Test public void cyclicOrdersStillPermitPooling() {
        check(new CMRxSolver().solve(problem(new double[][]{{2,1},{2,0}}, true, true,
            new double[][]{{1},{1}})), 20);
    }
    @Test public void feasibleRawMeansRetainZeroShortcut() {
        check(new CMRxSolver().solve(problem(new double[][]{{1,2},{0,2}}, true, false,
            new double[][]{{1},{1}})), 0);
    }
    @Test public void unconstrainedCommonOrderRetainsZeroShortcut() {
        CMRSolution solution = new CMRxSolver().solve(problem(new double[][]{{2,1},{2,0}},
            false, false, new double[][]{{1},{1}}));
        assertEquals(0, solution.getFStar(), 0);
        assertArrayEquals(new double[]{2,1}, solution.getXStar()[0], 0);
    }
    @Test public void zeroModelRowUsesFullDenseGLS() {
        DoubleMatrix2D[] w = {
            new DenseDoubleMatrix2D(new double[][]{{4,1},{1,2}}),
            new DenseDoubleMatrix2D(new double[][]{{1,0},{0,1}})
        };
        CMRxProblem p = new CMRxProblem(new double[][]{{2,5},{1,2}}, w, null,
            new DenseDoubleMatrix2D(new double[][]{{0},{1}}));
        CMRSolution[] solutions = {new CMRxSolver().solve(p),
            new ParCMRxSolver().solve(p, false, 2)};
        for (CMRSolution solution : solutions) {
            assertEquals(7.875, solution.getFStar(), 1e-8);
            assertArrayEquals(new double[]{3.125,3.125}, solution.getXStar()[0], 1e-8);
            assertArrayEquals(new double[]{1,2}, solution.getXStar()[1], 1e-8);
        }
    }
}
