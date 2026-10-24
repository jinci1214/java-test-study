package database;

import config.DatabaseConfig;
import database.model.TestUserProject;
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
    private final List<Long> temporaryUserProfileUserIds =
            new ArrayList<>();
    private final List<Long> temporaryUserAddressIds = new ArrayList<>();
    private final List<Long> temporaryProjectIds = new ArrayList<>();
    private final List<TestUserProject> temporaryUserProjects =
            new ArrayList<>();

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

    protected final void registerTemporaryUserProfileForCleanup(
            long userId
    ) {
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "登记清理的临时用户资料userId必须大于0"
            );
        }

        temporaryUserProfileUserIds.add(userId);
    }

    protected final void registerTemporaryUserAddressForCleanup(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "登记清理的临时用户地址id必须大于0"
            );
        }

        temporaryUserAddressIds.add(id);
    }

    protected final void registerTemporaryProjectForCleanup(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "登记清理的临时项目id必须大于0"
            );
        }

        temporaryProjectIds.add(id);
    }

    protected final void registerTemporaryUserProjectForCleanup(
            long userId,
            long projectId
    ) {
        if (userId <= 0 || projectId <= 0) {
            throw new IllegalArgumentException(
                    "登记清理的用户ID和项目ID必须大于0"
            );
        }

        temporaryUserProjects.add(
                new TestUserProject(userId, projectId)
        );
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUpTemporaryUsers() {
        try {
            for (TestUserProject relation : temporaryUserProjects) {
                int deletedRows = TestUserProjectRepository.delete(
                        relation.userId(),
                        relation.projectId()
                );

                if (deletedRows != 1) {
                    throw new IllegalStateException(
                            "临时用户项目关系清理失败，影响行数："
                                    + deletedRows
                                    + "，userId=" + relation.userId()
                                    + "，projectId=" + relation.projectId()
                    );
                }

                log.info(
                        "已清理临时用户项目关系：userId={}，projectId={}",
                        relation.userId(),
                        relation.projectId()
                );
            }

            for (Long id : temporaryUserAddressIds) {
                int deletedRows = TestUserAddressRepository.deleteById(id);

                if (deletedRows != 1) {
                    throw new IllegalStateException(
                            "临时用户地址清理失败，影响行数："
                                    + deletedRows
                                    + "，id=" + id
                    );
                }

                log.info("已清理临时测试用户地址：id={}", id);
            }

            for (Long userId : temporaryUserProfileUserIds) {
                int deletedRows = TestUserProfileRepository
                        .deleteByUserId(userId);

                if (deletedRows != 1) {
                    throw new IllegalStateException(
                            "临时用户资料清理失败，影响行数："
                                    + deletedRows
                                    + "，userId=" + userId
                    );
                }

                log.info("已清理临时测试用户资料：userId={}", userId);
            }

            for (Long id : temporaryProjectIds) {
                int deletedRows = TestProjectRepository.deleteById(id);

                if (deletedRows != 1) {
                    throw new IllegalStateException(
                            "临时项目清理失败，影响行数："
                                    + deletedRows
                                    + "，id=" + id
                    );
                }

                log.info("已清理临时测试项目：id={}", id);
            }

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
            temporaryUserProjects.clear();
            temporaryUserAddressIds.clear();
            temporaryUserProfileUserIds.clear();
            temporaryProjectIds.clear();
            temporaryUserIds.clear();
        }
    }
}
