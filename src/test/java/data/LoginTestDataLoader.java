package data;

import model.request.LoginRequest;
import model.testcase.LoginFailureCase;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class LoginTestDataLoader {

    private static final String DATA_FILE =
            "testdata/login-failure-cases.yaml";

    private LoginTestDataLoader() {
    }

    public static List<LoginFailureCase>
    loadFailureCases() {

        try (InputStream inputStream =
                     LoginTestDataLoader.class
                             .getClassLoader()
                             .getResourceAsStream(DATA_FILE)) {

            if (inputStream == null) {
                throw new IllegalStateException(
                        "找不到测试数据文件：" + DATA_FILE
                );
            }

            LoaderOptions loaderOptions =
                    new LoaderOptions();

            Yaml yaml = new Yaml(
                    new SafeConstructor(loaderOptions)
            );

            Object loadedData = yaml.load(inputStream);

            if (!(loadedData instanceof Map<?, ?> root)) {
                throw new IllegalStateException(
                        "YAML根节点必须是对象：" + DATA_FILE
                );
            }

            Object casesValue =
                    root.get("loginFailureCases");

            if (!(casesValue instanceof List<?> rawCases)) {
                throw new IllegalStateException(
                        "YAML中缺少loginFailureCases列表"
                );
            }

            if (rawCases.isEmpty()) {
                throw new IllegalStateException(
                        "loginFailureCases不能为空"
                );
            }

            List<LoginFailureCase> result =
                    new ArrayList<>();

            for (Object rawCaseValue : rawCases) {

                if (!(rawCaseValue
                        instanceof Map<?, ?> rawCase)) {
                    throw new IllegalStateException(
                            "每条登录测试数据必须是对象"
                    );
                }

                result.add(toLoginFailureCase(rawCase));
            }

            return List.copyOf(result);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "读取测试数据文件失败：" + DATA_FILE,
                    exception
            );
        }
    }

    private static LoginFailureCase
    toLoginFailureCase(Map<?, ?> rawCase) {

        Map<?, ?> request =
                getMap(rawCase, "request");

        Map<?, ?> expected =
                getMap(rawCase, "expected");

        return new LoginFailureCase(
                new LoginRequest(
                        getString(request, "username"),
                        getString(request, "password")
                ),
                getInt(expected, "httpStatus"),
                getInt(expected, "businessCode"),
                getString(expected, "message"),
                getString(rawCase, "scenario")
        );
    }

    private static Map<?, ?> getMap(
            Map<?, ?> source,
            String key
    ) {

        Object value = source.get(key);

        if (value instanceof Map<?, ?> map) {
            return map;
        }

        throw new IllegalStateException(
                "字段必须是对象：" + key
        );
    }

    private static String getString(
            Map<?, ?> source,
            String key
    ) {

        Object value = source.get(key);

        if (value instanceof String stringValue) {
            return stringValue;
        }

        throw new IllegalStateException(
                "字段必须是字符串：" + key
        );
    }

    private static int getInt(
            Map<?, ?> source,
            String key
    ) {

        Object value = source.get(key);

        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }

        throw new IllegalStateException(
                "字段必须是数字：" + key
        );
    }
}
