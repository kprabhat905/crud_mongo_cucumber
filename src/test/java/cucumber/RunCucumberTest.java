package cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import mongo.CommonInstance;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "cucumber.stepdefinitions",
        plugin = {"pretty", "html:target/cucumber-reports"}
)
public class RunCucumberTest {

}
