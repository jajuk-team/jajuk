# Jajuk

Jajuk is a cross-platform (Java/Swing) music organizer and player designed for large
music collections. This repository holds the **full Jajuk sources**, including the
packaging scripts and the vendored third-party libraries.

* Project website: <http://jajuk.info>
* Repository: <https://github.com/jajuk-team/jajuk>
* Current development version: `12.0dev`

## Repository layout

| Path | Content |
| --- | --- |
| `src/main/java` | Application sources (`org.jajuk.*` and the bundled `ext.*` helpers) |
| `src/main/resources` | Translations, icons, images, XSLT, default perspectives |
| `src/test/java` | Unit tests |
| `src/packaging/ant` | Ant build scripts (`build_full.xml`, `build_enduser.xml`) |
| `src/packaging/{linux,windows,OSX}` | Per-OS launchers, installers and icons |
| `src/legals` | Licences, authors and dependency list |
| `src/tools` | Maintenance shell scripts (dead label detection...) |
| `lib` | Runtime dependencies (jars), plus `lib/linux`, `lib/native`, `lib/mplayer` |
| `lib/build` | Build-time only dependencies |
| `build`, `bin`, `out`, `test-reports` | Generated, not versioned |

## Requirements

* **JDK 17+** — the Ant script compiles with `source`/`target` 17.
* **Apache Ant** — <https://ant.apache.org/>.
* No Maven/Gradle: every dependency is a jar vendored under `lib/`.

## Build

The packaging entry point is `src/packaging/ant/build_full.xml`. It first assembles a
source distribution under `build/sources_dist/jajuk-src-<version>/`, then builds and
packages from there.

```bash
# Everything (source zip, jar, Windows installer, macOS bundle, Linux tarball) + tests
ant -f src/packaging/ant/build_full.xml package_all

# Same without running the tests
ant -f src/packaging/ant/build_full.xml package_all_no_tests

# Just the jar
ant -f src/packaging/ant/build_full.xml build_jajuk
```

Artifacts land in `build/packages/` (`jajuk.jar`, `jajuk-sources-<version>.zip`, ...).

Note that `package_all` also builds the Windows and macOS packages, which need
[launch4j](https://launch4j.sourceforge.net/) in `/opt/launch4j` and a JRE in `/opt/jre`;
use `build_jajuk` if you only want the jar.

Inside a **source distribution** (the zip produced above, where `build_enduser.xml` has
been renamed to `build.xml`), a plain `ant` is enough — see `src/packaging/ant/BUILD.txt`.

## Run

```bash
java -cp "lib/*:bin/jajuk.jar" org.jajuk.Main
```

On Linux, `src/packaging/linux/jajuk` is the launcher script shipped with the packages;
it sets the heap and `java.library.path` for the native libraries in `lib/`.

Useful command line options:

| Option | Effect |
| --- | --- |
| `-test` | Use a `.jajuk_test` workspace instead of the regular one (also forces debug logs) |
| `-notest` | Not parsed: the regular workspace is the default. Passed explicitly by the shipped launchers |
| `-workspace=<path>` | Force the workspace location |

## Tests

The test suite lives in `src/test/java`. Every Jajuk test **must** extend
`org.jajuk.JajukTestCase` (except those in the `ext` package): it wipes and recreates a
throw-away workspace, resets the collection and forces a dummy MPlayer before each test.

Tests must run **headless**, otherwise the Swing code will try to open windows:

```bash
unset DISPLAY
ant -f src/packaging/ant/build_full.xml test_jajuk
```

JUnit XML reports are written to `test-reports/` of the generated source distribution.

The following files match the `**/*Test*.java` pattern but are helpers, not tests, and are
excluded by the Ant script: `ConstTest.java`, `JajukTestCase.java`, `TestHelpers.java`,
`ThreadTestHelper.java`. `TestDBusSupportImpl.java` is excluded as system-specific.

### Running the suite without Ant

The suite mixes JUnit 3/4 (`junit.framework.TestCase`) and JUnit 5 tests
(`org.jajuk.services.mpris.TestMprisService`, `org.jajuk.services.lastfm.TestLastFmClient`
and `org.jajuk.util.log.TestLog`, which also use Mockito). The JUnit Platform console
launcher runs both families in one go through its Jupiter and Vintage engines.

Put `junit-platform-console-standalone`, `junit` 4.13+, `mockito-core` and
`mockito-junit-jupiter` in a directory (`$TESTLIBS` below), then:

```bash
unset DISPLAY

CP="$(ls lib/*.jar lib/linux/*.jar "$TESTLIBS"/*.jar | tr '\n' ':')"

# Compile the application and the tests
mkdir -p bin/main bin/test
find src/main/java -name '*.java' > /tmp/main-sources.txt
javac -encoding UTF-8 -d bin/main -cp "$CP" @/tmp/main-sources.txt
find src/test/java -name '*.java' > /tmp/test-sources.txt
javac -encoding UTF-8 -d bin/test -cp "bin/main:$CP" @/tmp/test-sources.txt

# Run everything (~6 min)
java -Djava.awt.headless=true -Dfile.encoding=UTF-8 \
  -cp "bin/test:bin/main:src/main/resources:$CP" \
  org.junit.platform.console.ConsoleLauncher \
  --scan-classpath bin/test --details=summary --reports-dir=test-reports
```

To run a single class, replace `--scan-classpath bin/test` with
`--select-class org.jajuk.services.mpris.TestMprisService`.

Beware that a lot of state is static in Jajuk (`QueueModel`, `Conf`, the item managers...).
The Ant `<junit>` task forks one JVM per test class, so a test class that leaves a
configuration property modified can make another one fail when the whole suite is run in a
single JVM. Reset what you change in `specificSetUp()`.

### Known limitation

`lib/build/` only contains JUnit 4.8.1, so the Ant `tests` target cannot compile nor run
the three JUnit 5 test classes listed above. Add the JUnit 5 and Mockito jars to
`lib/build/` (and switch the target to the JUnit Platform launcher) to get the whole suite
under Ant.

## Logs

Jajuk logs through SLF4J/Logback (`src/main/resources/logback.xml`); when run from the
repository, logs go to `logs/jajuk.log`.

## Licence

Jajuk is released under the **GNU General Public License v2 or later** — see
`src/legals/LICENSE-GPL.txt`. Third-party licences and authors are listed in
`src/legals/DEPENDENCIES.txt` and `src/legals/AUTHORS.txt`.
