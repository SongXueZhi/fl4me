# Gzoltar Cli Tool Tutorial

Usage: java -jar gzoltarcli.jar --help | \<command>

Attention: java -jar may fail because of classpath not set, you can use java -cp.

|            |                                                              |
| ---------- | ------------------------------------------------------------ |
| \<command> | version \| listTestMethods \| instrument \| runTestMethods \| faultLocalizationReport |
| --help     | show help (default: true)                                    |
| --quiet    | suppress all output on stdout (default: false)               |

All of the commands share the same main class: `com.gzoltar.cli.Main`

## listTestMethods

List all (JUnit/TestNG) unit test cases in a provided classpath.



Usage: **java -jar gzoltarcli.jar listTestMethods**  \<path> ... [--help] [--includes <expression(s)>] [--outputFile \<file>] [--quiet]

|                            |                                                              |
| -------------------------- | ------------------------------------------------------------ |
| \<path>                    | list of folders that contain test classes                    |
| --help                     | show help (default: true)                                    |
| --quiet                    | suppress all output on stdout (default: false)               |
| --includes <expression(s)> | expression to identify which test methods to consider, may use wildcard characters (* and ?) and ':' to define more than one expression (default: *) |
| --outputFile \<file>       | file to which the name of all (JUnit/TestNG) unit test cases in the classpath will be written  (default: tests.txt) |



command sample: 

```shell
java -cp gzoltarcli.jar:gzoltarant.jar:gzoltaragent.jar:xxx/junit.jar:xxx/test-classes com.gzoltar.cli.Main listTestMethods xxx/test-classes --includes "org.jsoup.nodes.TextNodeTest#*"
```



output sample:

```
JUNIT,org.jsoup.nodes.TextNodeTest#testBlank
JUNIT,org.jsoup.nodes.TextNodeTest#testTextBean
```





## instrument

Off-line instrumentation of Java class files and jar files. (extract those files to output dir)

(This command will be called in the given `run.sh` if you set the parameter: `--instrumentation offline`)



Usage: **java -jar gzoltarcli.jar instrument** \<sourcefiles> ... [--help] --outputDirectory \<path> [--quiet]

|                            |                                                        |
| -------------------------- | ------------------------------------------------------ |
| --help                     | show help (default: true)                              |
| --quiet                    | suppress all output on stdout (default: false)         |
| \<sourcefiles>             | list of folders or files to instrument recursively     |
| --outputDirectory  \<path> | path to which the instrumented classes will be written |



## runTestMethods

Run test methods in isolation.



Usage: **java -jar gzoltarcli.jar runTestMethods** [--collectCoverage \<boolean>] [--help] [--initTestClass \<boolean>] [--offline \<boolean>] [--quiet] --testMethods \<path>

|                              |                                                              |
| ---------------------------- | ------------------------------------------------------------ |
| --help                       | show help (default: true)                                    |
| --quiet                      | suppress all output on stdout (default: false)               |
| --collectCoverage \<boolean> | collect coverage of each test method (default: false)        |
| --initTestClass \<boolean>   | initialize test class with thread classloader (default: false) |
| --offline \<boolean>         | inform GZoltar that classes have been instrumented using offline instrumentation (default: false) |
| --testMethods \<path>        | file with list of test methods to run (i.e. the outputFile generated in `listTestMethods` command) |



## faultLocalizationReport

Create a fault localization report based on previously collected data.



Usage: **java -jar gzoltarcli.jar faultLocalizationReport** --buildLocation \<path> --dataFile \<path> [--excludes <expression(s)>] [--family \<family>] [--formatter \<formatter>] [--formula \<formula>] [--granularity <line|method|class>] [--help] [--inclDeprecatedMethods \<boolean>] [--inclPublicMethods \<boolean>] [--inclStaticConstructors \<boolean>] [--includes <expression(s)>] [--metric \<metric>] --outputDirectory \<path> [--quiet]



|                                         |                                                              |
| --------------------------------------- | ------------------------------------------------------------ |
| --help                                  | show help (default: true)                                    |
| --quiet                                 | suppress all output on stdout (default: false)               |
| --buildLocation \<path>                 | location of Java class files                                 |
| --dataFile \<path>                      | GZoltar *.ser file to process                                |
| --excludes <expression(s)>              | expression to identify which classes to not report on, may use wildcard characters (* and ?) and ':' to define more than one expression (default: ) |
| --family \<family>                      | fault localization family (default: SFL)                     |
| --formatter \<formatter>                | fault localization report formatter (use  ':' to define more than one formatter) (default: TXT) |
| --formula \<formula>                    | fault localization formula (use ':' to define more than one formula) (default: OCHIAI) |
| --granularity <line \| method \| class> | source code granularity level of report (default: LINE)      |
| --inclDeprecatedMethods \<boolean>      | specifies whether public methods of each class should be reported (default: true) |
| --inclPublicMethods \<boolean>          | specifies whether public methods of each class should be reported (default: true) |
| --inclStaticConstructors \<boolean>     | specifies whether public static constructors of each class should be reported (default: false) |
| --includes \<expression(s)>             | expression to identify which classes to report on, may use wildcard characters (* and ?) and ':' to define more than one expression (default: *) |
| --metric \<metric>                      | fault localization ranking metric (use ':' to define more than one metric) (default: AMBIGUITY) |
| --outputDirectory \<path>               | output directory for the report                              |



## integreted in shell script

The following script shows the online mode(do not instrument src/test files). You should copy those class files to specific dir manually.

This script is based on the given script in [gzoltar/com.gzoltar.cli.examples/run.sh at master · GZoltar/gzoltar (github.com)](https://github.com/GZoltar/gzoltar/blob/master/com.gzoltar.cli.examples/run.sh) 

usage: ./run.sh --instrumentation online

```shell
#!/usr/bin/env bash
#
# ------------------------------------------------------------------------------
# This script performs fault-localization on a Java project using the GZoltar
# command line interface either using instrumentation 'at runtime' or 'offline'.
#
# Usage:
# ./run.sh
#     --instrumentation <online|offline>
#     [--help]
#
# Requirements:
# - `java` and `javac` needs to be set and must point to the Java installation.
#
# ------------------------------------------------------------------------------

SCRIPT_DIR=$(cd `dirname ${BASH_SOURCE[0]}` && pwd)

#
# Print error message and exit
#
die() {
  echo "$@" >&2
  exit 1
}

# ------------------------------------------------------------------ Envs & Args

GZOLTAR_VERSION="1.7.4-SNAPSHOT" # (0) change to your version of jar file

# Check whether GZOLTAR_CLI_JAR is set
export GZOLTAR_CLI_JAR="$SCRIPT_DIR/../com.gzoltar.cli/target/com.gzoltar.cli-$GZOLTAR_VERSION-jar-with-dependencies.jar"
[ "$GZOLTAR_CLI_JAR" != "" ] || die "GZOLTAR_CLI is not set!"
[ -s "$GZOLTAR_CLI_JAR" ] || die "$GZOLTAR_CLI_JAR does not exist or it is empty! Please go to '$SCRIPT_DIR/..' and run 'mvn clean install'."

# Check whether GZOLTAR_AGENT_RT_JAR is set
export GZOLTAR_AGENT_RT_JAR="$SCRIPT_DIR/../com.gzoltar.agent.rt/target/com.gzoltar.agent.rt-$GZOLTAR_VERSION-all.jar"
[ "$GZOLTAR_AGENT_RT_JAR" != "" ] || die "GZOLTAR_AGENT_RT_JAR is not set!"
[ -s "$GZOLTAR_AGENT_RT_JAR" ] || die "$GZOLTAR_AGENT_RT_JAR does not exist or it is empty! Please go to '$SCRIPT_DIR/..' and run 'mvn clean install'."

USAGE="Usage: ${BASH_SOURCE[0]} --instrumentation <online|offline> [--help]"
if [ "$#" -eq "0" ]; then
  die "$USAGE"
fi
mod_of_two=$(expr $# % 2)
if [ "$#" -ne "1" ] && [ "$mod_of_two" -ne "0" ]; then
  die "$USAGE"
fi

INSTRUMENTATION=""

while [[ "$1" = --* ]]; do
  OPTION=$1; shift
  case $OPTION in
    (--instrumentation)
      INSTRUMENTATION=$1;
      shift;;
    (--help)
      echo "$USAGE";
      exit 0;;
    (*)
      die "$USAGE";;
  esac
done

[ "$INSTRUMENTATION" != "" ] || die "$USAGE"
if [ "$INSTRUMENTATION" != "online" ] && [ "$INSTRUMENTATION" != "offline" ]; then
  die "$USAGE"
fi

#
# Prepare runtime dependencies
#
LIB_DIR="$SCRIPT_DIR/lib"
mkdir -p "$LIB_DIR" || die "Failed to create $LIB_DIR!"
[ -d "$LIB_DIR" ] || die "$LIB_DIR does not exist!"

JUNIT_JAR="$LIB_DIR/junit.jar"
if [ ! -s "$JUNIT_JAR" ]; then
  wget "https://repo1.maven.org/maven2/junit/junit/4.12/junit-4.12.jar" -O "$JUNIT_JAR" || die "Failed to get junit-4.12.jar from https://repo1.maven.org!"
fi
[ -s "$JUNIT_JAR" ] || die "$JUNIT_JAR does not exist or it is empty!"

HAMCREST_JAR="$LIB_DIR/hamcrest-core.jar"
if [ ! -s "$HAMCREST_JAR" ]; then
  wget -np -nv "https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar" -O "$HAMCREST_JAR" || die "Failed to get hamcrest-core-1.3.jar from https://repo1.maven.org!"
fi
[ -s "$HAMCREST_JAR" ] || die "$HAMCREST_JAR does not exist or it is empty!"

BUILD_DIR="$SCRIPT_DIR/build"

# ------------------------------------------------------------------------- Main

#
# Collect list of unit test cases to run
#

echo "Collect list of unit test cases to run ..."

UNIT_TESTS_FILE="$BUILD_DIR/tests.txt"
# invoke listTestMethods command, set the included test, write the output to build/tests.txt

java -cp $BUILD_DIR:$JUNIT_JAR:$HAMCREST_JAR:$GZOLTAR_CLI_JAR \
  com.gzoltar.cli.Main listTestMethods $BUILD_DIR \
    --outputFile "$UNIT_TESTS_FILE" \
    --includes "org.jsoup.nodes.TextNodeTest#*" || die "Collection of unit test cases has failed!"
# (1)Modify --includes as the test file/cases you want to analyze.    
[ -s "$UNIT_TESTS_FILE" ] || die "$UNIT_TESTS_FILE does not exist or it is empty!"
# assign the test file that included in your fault localization task. 

# Collect coverage
#

SER_FILE="$BUILD_DIR/gzoltar.ser"

if [ "$INSTRUMENTATION" == "online" ]; then
  echo "Perform instrumentation at runtime and run each unit test case in isolation ..."

  # Perform instrumentation at runtime and run each unit test case in isolation
  # Generate .ser file(will be used later) and runTestMethods
  # (2) Modify the includes
  java -javaagent:$GZOLTAR_AGENT_RT_JAR=destfile=$SER_FILE,buildlocation=$BUILD_DIR,includes="org.jsoup.nodes.TextNode:org.jsoup.nodes.TextNode\$*",excludes="",inclnolocationclasses=false,output="file" \
    -cp $BUILD_DIR:$JUNIT_JAR:$HAMCREST_JAR:$GZOLTAR_CLI_JAR \
    com.gzoltar.cli.Main runTestMethods \
      --testMethods "$UNIT_TESTS_FILE" \
      --collectCoverage || die "Coverage collection has failed!"
fi

[ -s "$SER_FILE" ] || die "$SER_FILE does not exist or it is empty!"

#
# Create fault localization report
#

echo "Create fault localization report ..."

SPECTRA_FILE="$BUILD_DIR/sfl/txt/spectra.csv"
MATRIX_FILE="$BUILD_DIR/sfl/txt/matrix.txt"
TESTS_FILE="$BUILD_DIR/sfl/txt/tests.csv"

java -cp $BUILD_DIR:$JUNIT_JAR:$HAMCREST_JAR:$GZOLTAR_CLI_JAR \
  com.gzoltar.cli.Main faultLocalizationReport \
    --buildLocation "$BUILD_DIR" \
    --granularity "line" \
    --inclPublicMethods \
    --inclStaticConstructors \
    --inclDeprecatedMethods \
    --dataFile "$SER_FILE" \
    --outputDirectory "$BUILD_DIR" \
    --family "sfl" \
    --formula "ochiai" \
    --metric "entropy" \
    --formatter "txt" || die "Generation of fault-localization report has failed!"

[ -s "$SPECTRA_FILE" ] || die "$SPECTRA_FILE does not exist or it is empty!"
[ -s "$MATRIX_FILE" ] || die "$MATRIX_FILE does not exist or it is empty!"
[ -s "$TESTS_FILE" ] || die "$TESTS_FILE does not exist or it is empty!"

echo "DONE!"
exit 0
```



The demo script given by gzoltar uses javac to compile single java file. However, it doesn't work in a real-work project.  The script show one way to overcome this (Now suppose the project uses maven build system): 

- Modify the `GZOLTAR_VERSION` (marked as (0) ) to your own,follow the gzoltar source project version or jar file version.
- Go to the root path of your project, use `mvn compile test-compile` to compile the whole project. Then you will get class files in target/classes and target/test-classes.
- Copy all of the class file to the `build` directory, which stays in the same directory with run.sh. Note that  put source class files and test class files together.
- Modify the include tests in run.sh(marked as (1) ), modify the source file you want to localize(marked as (2) )

To reproduce the steps above, you can clone the project https://github.com/GZoltar/gzoltar.git, compile the whole project(to get gzotar jar file), modify the **run.sh** in com.gzoltar.cli.examples and prepare the class files.



You can also download the released jar file, but you may modify the jar file name in the script.



You will get FL report in sfl directory.



The directory structure will be like: 

<img src="/Users/zhjlu/Desktop/Screenshot 2024-01-09 at 15.34.13.png" style="zoom:67%;" />
