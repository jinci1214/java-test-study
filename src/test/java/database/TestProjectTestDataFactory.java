package database;

import database.model.TestProject;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 统一创建数据库自动化测试所需的临时项目数据。
 */
public final class TestProjectTestDataFactory {

    private TestProjectTestDataFactory() {
    }

    public static TestProject createTemporaryProject() {
        String uniqueSuffix = UUID.randomUUID()
                .toString()
                .replace("-", "");

        return createTemporaryProject(
                "db-project-" + uniqueSuffix
        );
    }

    public static TestProject createTemporaryProject(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "临时项目名称不能为空"
            );
        }

        long projectId = ThreadLocalRandom.current()
                .nextLong(1, Long.MAX_VALUE);

        return new TestProject(projectId, name);
    }
}
