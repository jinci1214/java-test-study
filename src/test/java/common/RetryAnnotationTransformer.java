package common;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

public class RetryAnnotationTransformer
        implements IAnnotationTransformer {

    @Override
    public void transform(
            ITestAnnotation annotation,
            Class testClass,
            Constructor testConstructor,
            Method testMethod
    ) {

        boolean isRetryable = shouldEnableRetry(annotation.getGroups());

        if (isRetryable) {
            annotation.setRetryAnalyzer(
                    TestRetryAnalyzer.class
            );
        }
    }

    static boolean shouldEnableRetry(String[] groups) {
        return groups != null
                && Arrays.asList(groups).contains("retryable");
    }
}
