# JUnit5 Test Runner Setup

This directory now contains a properly configured JUnit5 test runner for executing targeted tests efficiently.

## Files Added

1. **`lib/build/junit-platform-console-standalone-1.8.2.jar`** - JUnit Platform Console launcher
2. **`run-junit5-test.sh`** - Shell script to run individual tests
3. **`test-runner.xml`** - Ant build file (alternative method)

## Quick Start

### Running a Single Test Class

```bash
./run-junit5-test.sh org.jajuk.services.mpris.TestMprisService
```

### Running a Specific Test Method

To run a single method, you can modify the script or run it directly:

```bash
java -cp "bin:$(ls lib/*.jar lib/build/*.jar | tr '\n' ':')" \
  org.junit.platform.console.ConsoleLauncher \
  --select-class org.jajuk.services.mpris.TestMprisService \
  --fail-if-no-tests
```

## How It Works

The `run-junit5-test.sh` script:
1. Builds a complete classpath from all JARs in `lib/` and `lib/build/`
2. Includes compiled classes from `bin/`
3. Calls JUnit Platform Console Launcher with the specified test class
4. Reports results with colored output

## Dependencies

- **JUnit Platform 1.8.2** (ConsoleLauncher)
- All existing library dependencies in `lib/`
- Compiled project in `bin/`

## Troubleshooting

### Test class not found
Ensure the class is fully compiled in the `bin/` directory.

### Missing resource files
Some tests may require resource files (e.g., `jajuk_fr.properties`). Make sure `src/main/resources` is in the classpath.

### Mockito/Bytecode instrumentation errors
These are test-specific issues, not framework issues. See the test output for details.

## Notes

- The original `test-runner.xml` is kept as an alternative Ant-based approach
- ConsoleLauncher cannot mix `--scan-classpath` with explicit selectors (`--select-class`)
- The explicit classpath approach is more reliable and faster for targeted tests

