package common;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class RetryAnnotationTransformerTest {

    @Test(groups = "stability")
    public void shouldEnableRetryForRetryableGroup() {
        assertTrue(
                RetryAnnotationTransformer.shouldEnableRetry(
                        new String[]{"performance", "retryable"}
                )
        );
    }

    @Test(groups = "stability")
    public void shouldNotEnableRetryForOrdinaryRegressionGroup() {
        assertFalse(
                RetryAnnotationTransformer.shouldEnableRetry(
                        new String[]{"regression"}
                )
        );
    }
}
