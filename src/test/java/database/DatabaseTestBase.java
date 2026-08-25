package database;

import config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * 真实数据库测试的公共生命周期。
 */
public abstract class DatabaseTestBase {

    private static final Logger log =
            LoggerFactory.getLogger(DatabaseTestBase.class);

    private final List<Long> temporaryUserIds = new ArrayList<>();

    @BeforeMethod(alwaysRun = true)
    public void requireDatabasePassword() {
        if (!DatabaseConfig.hasPassword()) {
            throw new SkipException(
                    "未配置 DB_PASSWORD，跳过真实数据库测试"
            );
        }
    }

    protected final void registerTemporaryUserForCleanup(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "登记清理的临时用户id必须大于0"
            );
        }

        temporaryUserIds.add(id);
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUpTemporaryUsers() {
        try {
            for (Long id : temporaryUserIds) {
                int deletedRows = TestUserRepository.deleteById(id);

                if (deletedRows != 1) {
                    throw new IllegalStateException(
                            "临时用户清理失败，影响行数："
                                    + deletedRows
                                    + "，id=" + id
                    );
                }

                log.info("已清理临时测试用户：id={}", id);
            }
        } finally {
            temporaryUserIds.clear();
        }
    }
}
