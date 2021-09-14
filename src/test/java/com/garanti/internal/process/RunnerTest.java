package com.garanti.internal.process;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions( //
    features = "classpath:insurance-quote.feature", //
    monochrome = true,
    plugin = {"pretty", "junit:target/cucumber-report.xml",
							"json:target/cucumber-report.json",
							"html:target/cucumber"} //
)
public class RunnerTest {

}