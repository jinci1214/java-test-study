package tests;


import org.testng.annotations.Test;

import java.net.SocketTimeoutException;

public class RetryDemo {

    private int executionCount = 0;

    @Test
    public void retryDemoTest() throws SocketTimeoutException {

        executionCount++;

        System.out.println(
                "retryDemoTest 第 " + executionCount + " 次执行"
        );

        if (executionCount == 1) {
            throw new SocketTimeoutException(
                    "模拟第一次请求超时"
            );
        }
    }
}