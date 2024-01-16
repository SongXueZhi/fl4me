package org.example;

import fr.spoonlabs.flacoco.api.Flacoco;
import fr.spoonlabs.flacoco.api.result.FlacocoResult;
import fr.spoonlabs.flacoco.api.result.Location;
import fr.spoonlabs.flacoco.api.result.Suspiciousness;
import fr.spoonlabs.flacoco.core.config.FlacocoConfig;
import fr.spoonlabs.flacoco.core.test.method.TestMethod;
import fr.spoonlabs.flacoco.localization.spectrum.SpectrumFormula;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private static final Properties prop = new Properties();

    public static void main(String[] args) throws IOException {
        try (InputStream inStream = new FileInputStream(new File("config.properties"))) {
            prop.load(inStream);
        } catch (IOException ex) {
            System.out.println("config.properties not found!");
            System.exit(1);
        }
        String projectPath = prop.getProperty("projectPath");

        FileHandler fileHandler = new FileHandler("falcoco-demo.log", true);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);

        FlacocoConfig config = new FlacocoConfig();
        config.setProjectPath(projectPath);
        logger.info("====================================");
        logger.info("project path: " + config.getProjectPath());
        config.setTestRunnerVerbose(true);
        config.setFamily(FlacocoConfig.FaultLocalizationFamily.SPECTRUM_BASED);
        config.setTestDetectionStrategy(FlacocoConfig.TestDetectionStrategy.CLASSLOADER);//TEST_RUNNER & CLASSLOADER
        logger.info("TestDetectionStrategy: " + config.getTestDetectionStrategy());
        config.setSpectrumFormula(SpectrumFormula.OCHIAI);
        config.setComputeSpoonResults(true);
        Set<String> excludes = new HashSet<>();
//        excludes.add("org.jsoup.parser.*"); * not work!
//        excludes.add("org.jsoup.parser.TokenQueueTest");
        excludes.add("org.jsoup.integration.UrlConnectTest");//according to the document, class/method are accepted here
// Jacoco is a code coverage tool, not a fault localization tool. So this statement can not exclude tests
//        config.setJacocoExcludes(excludes);
        config.setIgnoredTests(excludes);//exclude tests
        System.out.println(config.isIncludeZeros());
//        config.setIncludeZeros(true);//if this set to true, you will see more suspicious locations with suspicious value 0.0
        Flacoco flacoco = new Flacoco(config);
        FlacocoResult result = flacoco.run();

        Set<TestMethod> failingTests = result.getFailingTests();
        Map<Location, Suspiciousness> mapping = result.getDefaultSuspiciousnessMap();
        System.out.println("failingTests size: " + failingTests.size());
        logger.info("failingTests size: " + failingTests.size());
        failingTests.forEach(e ->{
            System.out.println(e);
            logger.info(e.toString());
        });
        System.out.println("---------------------");
        logger.info("---------------------");
        System.out.println("suspicious mapping size: " + mapping.size());
        logger.info("suspicious mapping size: " + mapping.size());
        mapping.forEach((k, v) -> {//Location ==> Suspiciousness
            System.out.println("class: " + k.getClassName());
            System.out.println("line: " + k.getLineNumber());
            System.out.println("score: " + v.getScore());
            System.out.println(k + " : " + v);
            logger.info(k + " : " + v);
        });
    }
}