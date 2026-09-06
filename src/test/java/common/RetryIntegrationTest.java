package common;

import io.qameta.allure.Allure;
import org.testng.annotations.Test;

import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;

public class RetryIntegrationTest {

    private static final AtomicInteger ATTEMPT_COUNT = new AtomicInteger();

    @Test(groups = {"stability", "retryable"})
    public void shouldRetryTransientSocketTimeoutAndThenPass() {
        int currentAttempt = ATTEMPT_COUNT.incrementAndGet();
        Allure.parameter("当前执行次数", currentAttempt);

        if (currentAttempt == 1) {
            throw new RuntimeException(
                    new SocketTimeoutException("模拟一次临时读取超时")
            );
        }

        try {
            assertEquals(currentAttempt, 2);
        } finally {
            ATTEMPT_COUNT.set(0);
        }
    }
}
