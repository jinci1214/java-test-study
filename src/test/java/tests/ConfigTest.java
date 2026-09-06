package tests;

import config.Config;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class ConfigTest {

    @Test(groups = "performance")
    public void maxResponseTimeUsesYamlDefaultWithoutJvmPropertyTest() {
        String propertyName = "maxResponseTimeMillis";
        String originalValue = System.getProperty(propertyName);

        try {
            System.clearProperty(propertyName);

            assertEquals(Config.getMaxResponseTimeMillis(), 1000L);
        } finally {
            if (originalValue != null) {
                System.setProperty(propertyName, originalValue);
            }
        }
    }

    @Test(groups = "performance")
    public void maxResponseTimeCanBeOverriddenByJvmPropertyTest() {
        String propertyName = "maxResponseTimeMillis";
        String originalValue = System.getProperty(propertyName);

        try {
            System.setProperty(propertyName, "2000");

            assertEquals(Config.getMaxResponseTimeMillis(), 2000L);
        } finally {
            if (originalValue == null) {
                System.clearProperty(propertyName);
            } else {
                System.setProperty(propertyName, originalValue);
            }
        }
    }

    @DataProvider(name = "invalidMaxResponseTimeData")
    public Object[][] invalidMaxResponseTimeData() {
        return new Object[][]{
                {"fast"},
                {"0"},
                {"-1"}
        };
    }

    @Test(
            dataProvider = "invalidMaxResponseTimeData",
            groups = "performance"
    )
    public void invalidMaxResponseTimeJvmPropertyTest(
            String invalidValue
    ) {
        String propertyName = "maxResponseTimeMillis";
        String originalValue = System.getProperty(propertyName);

        try {
            System.setProperty(propertyName, invalidValue);

            IllegalArgumentException exception = expectThrows(
                    IllegalArgumentException.class,
                    Config::getMaxResponseTimeMillis
            );
            assertEquals(
                    exception.getMessage(),
                    "JVM 属性 maxResponseTimeMillis 必须是正整数："
                            + invalidValue
            );
        } finally {
            if (originalValue == null) {
                System.clearProperty(propertyName);
            } else {
                System.setProperty(propertyName, originalValue);
            }
        }
    }
}
