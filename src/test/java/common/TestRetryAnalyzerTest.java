package common;

import org.testng.annotations.Test;

import java.net.SocketTimeoutException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TestRetryAnalyzerTest {

    @Test(groups = "stability")
    public void shouldRetrySocketTimeoutWrappedByAnotherException() {
        Throwable exception = new RuntimeException(
                new SocketTimeoutException("读取响应超时")
        );

        assertTrue(TestRetryAnalyzer.isRetryableException(exception));
    }

    @Test(groups = "stability")
    public void shouldNotRetryAssertionFailure() {
        Throwable exception = new AssertionError("业务断言失败");

        assertFalse(TestRetryAnalyzer.isRetryableException(exception));
    }

    @Test(groups = "stability")
    public void shouldRetrySocketTimeoutOnlyOnce() {
        TestRetryAnalyzer retryAnalyzer = new TestRetryAnalyzer();
        Throwable exception = new SocketTimeoutException("读取响应超时");

        assertTrue(retryAnalyzer.shouldRetry(exception));
        assertFalse(retryAnalyzer.shouldRetry(exception));
    }
}
