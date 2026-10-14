# Correctness maintenance fork

This fork retains the original Java optimizer. It does not include the
experimental active-set replacement from the `stacmr` benchmarks.

Changes:

- Honor initial order constraints before serial/parallel zero-fit CMR returns.
- Fit the MR root in both greedy initializers, including approximate fits.
- Honor initial constraints in the R full-rank model shortcut and use full
  matrix weights for constant outcome rows.
- Use population variance for single-condition R cells, matching Java.
- Return MR bootstrap parameter estimates, correct raw-list/general indexing,
  and select the MR solver for parametric MR bootstrap.
- Preserve singleton matrix dimensions in conversions and parameter outputs.
- Align binomial count rows to condition IDs and accept raw/statistics forms.
- Evaluate binomial likelihood correctly at probability zero and one.
- Permit requested status windows to be disabled in a headless environment.
- Build with Java 8-compatible bytecode using a modern compiler plugin.

Normal build, from `Java Source/java source`:

```sh
mvn test package
cp target/fxMR-0.3.45-jar-with-dependencies.jar ../../STACMR-R/java/
cp target/fxMR-0.3.45-jar-with-dependencies.jar ../../STACMR-matlab/java/
```

When Maven downloads are unavailable, the dependency JAR from the original
distribution can support compilation and a selected regression suite:

```sh
python3 "Java Source/build-offline.py" /path/to/original-fxMR-fat.jar
```

That command compiles the source and runs 65 tests, including the new
initial-order and boundary-likelihood cases. It is not a complete Maven build
or a run of every historical simulation test. From `STACMR-R`, run
`Rscript tests/test-correctness.R` to check the standalone R interface.

The rebuilt JAR has version 0.3.45 and is selected by the original R setup
script. Original notices and authorship are preserved. This file does not add
or infer a new license grant. No new issue or PR is submitted upstream.
