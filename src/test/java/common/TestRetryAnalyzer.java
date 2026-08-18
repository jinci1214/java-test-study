package common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpConnectTimeoutException;

public class TestRetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log =
            LoggerFactory.getLogger(TestRetryAnalyzer.class);

    private static final int MAX_RETRY_COUNT = 1;
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {

        Throwable throwable = result.getThrowable();

         if(!isRetryableException(throwable)){
             log.warn(
                     "测试失败但不重试：test:{},reason={}",
                     result.getMethod().getMethodName(),
                     throwable == null
                     ? "未知异常"
                             : throwable.getClass().getSimpleName()
             );
             return false;

         }
         if(retryCount < MAX_RETRY_COUNT){
             retryCount++;
             log.warn(
                     "检测到临时技术故障，准备重试：test={}, retry={}/{}",
                     result.getMethod().getMethodName(),
                     retryCount,
                     MAX_RETRY_COUNT
             );
             return true;
         }

        return false;
    }
    private boolean isRetryableException(Throwable throwable){
        Throwable current = throwable;

        while(current != null){
            if(current instanceof ConnectException
            || current instanceof SocketTimeoutException
            || current instanceof HttpConnectTimeoutException){
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
