package database;

import database.model.TestProject;
import database.model.TestUser;
import database.model.TestUserAddress;
import database.model.TestUserProject;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Epic("数据库测试")
@Feature("幂等性与并发")
public class TestDatabaseIdempotencyAndConcurrencyTest
        extends DatabaseTestBase {

    @Test(groups = "database")
    @Story("重复删除保持最终状态一致")
    @Description("同一地址删除两次，第一次影响1行，第二次影响0行，最终数据均不存在")
    public void shouldDeleteAddressIdempotently() {
        TestUser user = TestUserTestDataFactory.createTemporaryUser();
        TestUserRepository.create(user);
        registerTemporaryUserForCleanup(user.id());

        TestUserAddress address = TestUserAddressTestDataFactory
                .createTemporaryAddress(user.id(), "幂等删除地址");

        try {
            TestUserAddressRepository.create(address);

            assertEquals(
                    TestUserAddressRepository.deleteById(address.id()),
                    1,
                    "第一次删除应删除一条地址"
            );
            assertEquals(
                    TestUserAddressRepository.deleteById(address.id()),
                    0,
                    "第二次删除不应重复删除数据"
            );
            assertTrue(
                    TestUserAddressRepository
                            .findByUserId(user.id())
                            .isEmpty(),
                    "重复删除后的最终状态应为地址不存在"
            );
        } finally {
            TestUserAddressRepository.deleteById(address.id());
        }
    }

    @Test(groups = "database")
    @Story("并发重复建立用户项目关系")
    @Description("两个线程同时写入相同联合主键时，应只有一个成功")
    public void shouldAllowOnlyOneConcurrentDuplicateRelation()
            throws Exception {
        TestUser user = TestUserTestDataFactory.createTemporaryUser();
        TestProject project = TestProjectTestDataFactory
                .createTemporaryProject();
        TestUserRepository.create(user);
        registerTemporaryUserForCleanup(user.id());
        TestProjectRepository.create(project);
        registerTemporaryProjectForCleanup(project.id());

        TestUserProject relation = new TestUserProject(
                user.id(),
                project.id()
        );

        try {
            Throwable[] results = runConcurrentCreates(relation);
            int successCount = 0;
            Throwable duplicateFailure = null;

            for (Throwable result : results) {
                if (result == null) {
                    successCount++;
                } else {
                    duplicateFailure = result;
                }
            }

            assertEquals(successCount, 1, "并发写入应只有一次成功");
            assertNotNull(duplicateFailure, "另一次写入应因重复而失败");
            assertTrue(
                    duplicateFailure instanceof IllegalStateException,
                    "重复写入应由Repository包装为IllegalStateException"
            );
            assertTrue(
                    duplicateFailure.getCause() instanceof SQLException,
                    "重复写入的底层原因应为SQLException"
            );

            SQLException sqlException =
                    (SQLException) duplicateFailure.getCause();
            assertEquals(
                    sqlException.getSQLState(),
                    "23000",
                    "重复联合主键应触发完整性约束冲突"
            );
            assertEquals(
                    TestUserProjectRepository
                            .findProjectsByUserId(user.id()),
                    java.util.List.of(project),
                    "数据库中最终应只保留一条关系"
            );
        } finally {
            TestUserProjectRepository.delete(user.id(), project.id());
        }
    }

    private Throwable[] runConcurrentCreates(TestUserProject relation)
            throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readySignal = new CountDownLatch(2);
        CountDownLatch startSignal = new CountDownLatch(1);

        Callable<Throwable> createTask = () -> {
            readySignal.countDown();

            if (!startSignal.await(5, TimeUnit.SECONDS)) {
                return new AssertionError("等待并发开始信号超时");
            }

            try {
                TestUserProjectRepository.create(relation);
                return null;
            } catch (Throwable throwable) {
                return throwable;
            }
        };

        try {
            Future<Throwable> firstResult = executor.submit(createTask);
            Future<Throwable> secondResult = executor.submit(createTask);

            assertTrue(
                    readySignal.await(5, TimeUnit.SECONDS),
                    "两个并发任务应准备完成"
            );
            startSignal.countDown();

            return new Throwable[]{
                    firstResult.get(10, TimeUnit.SECONDS),
                    secondResult.get(10, TimeUnit.SECONDS)
            };
        } finally {
            startSignal.countDown();
            executor.shutdownNow();
        }
    }
}
