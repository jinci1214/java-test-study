package database;

import database.model.TestUser;
import database.model.TestUserProfile;
import database.model.TestUserWithProfile;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

@Epic("数据库测试")
@Feature("用户资料关联")
public class TestUserProfileRepositoryTest extends DatabaseTestBase {

    @Test(groups = "database")
    @Story("新增并查询用户资料")
    @Description("先创建临时用户，再创建并查询该用户的资料")
    public void shouldCreateAndFindProfileForExistingUser() {
        TestUser user = TestUserTestDataFactory.createTemporaryUser();
        TestUserRepository.create(user);
        registerTemporaryUserForCleanup(user.id());

        TestUserProfile expectedProfile = new TestUserProfile(
                user.id(),
                "资料-" + user.id(),
                "profile-" + user.id() + "@example.test"
        );

        TestUserProfileRepository.create(expectedProfile);
        registerTemporaryUserProfileForCleanup(user.id());

        TestUserProfile actualProfile = TestUserProfileRepository
                .findByUserId(user.id())
                .orElseThrow(() -> new AssertionError(
                        "新增后未查询到用户资料：userId=" + user.id()
                ));

        assertEquals(actualProfile, expectedProfile);
    }

    @Test(groups = "database")
    @Story("拒绝不存在用户的资料")
    @Description("逻辑外键校验应在写入前拒绝关联不存在用户的资料")
    public void shouldRejectProfileForNonexistentUser() {
        TestUser nonexistentUser =
                TestUserTestDataFactory.createTemporaryUser();

        assertTrue(
                TestUserRepository.findById(nonexistentUser.id()).isEmpty(),
                "测试前提：随机生成的用户ID不应存在"
        );

        TestUserProfile profile = new TestUserProfile(
                nonexistentUser.id(),
                "不存在用户的资料",
                "missing-" + nonexistentUser.id() + "@example.test"
        );

        IllegalStateException exception = expectThrows(
                IllegalStateException.class,
                () -> TestUserProfileRepository.create(profile)
        );

        assertTrue(
                exception.getMessage().contains("关联用户不存在"),
                "异常信息应明确说明关联用户不存在"
        );
        assertTrue(
                TestUserProfileRepository
                        .findByUserId(nonexistentUser.id())
                        .isEmpty(),
                "逻辑外键校验失败后不应写入用户资料"
        );
    }

    @Test(groups = "database")
    @Story("关联查询用户及其资料")
    @Description("通过INNER JOIN一次查询用户主表和资料表的数据")
    public void shouldFindUserWithProfileByUserId() {
        TestUser user = TestUserTestDataFactory.createTemporaryUser(
                "tester"
        );
        TestUserRepository.create(user);
        registerTemporaryUserForCleanup(user.id());

        TestUserProfile profile = new TestUserProfile(
                user.id(),
                "关联查询资料-" + user.id(),
                "joined-" + user.id() + "@example.test"
        );
        TestUserProfileRepository.create(profile);
        registerTemporaryUserProfileForCleanup(user.id());

        TestUserWithProfile actual = TestUserProfileRepository
                .findUserWithProfileByUserId(user.id())
                .orElseThrow(() -> new AssertionError(
                        "未查询到用户及其资料：userId=" + user.id()
                ));

        TestUserWithProfile expected = new TestUserWithProfile(
                user.id(),
                user.username(),
                user.role(),
                profile.nickname(),
                profile.email()
        );

        assertEquals(actual, expected);
    }

    @Test(groups = "database")
    @Story("限制一个用户只有一份资料")
    @Description("同一userId第二次新增资料时，应被主键唯一性约束拒绝")
    public void shouldRejectSecondProfileForSameUser() {
        TestUser user = TestUserTestDataFactory.createTemporaryUser();
        TestUserRepository.create(user);
        registerTemporaryUserForCleanup(user.id());

        TestUserProfile firstProfile = new TestUserProfile(
                user.id(),
                "第一份资料",
                "first-" + user.id() + "@example.test"
        );
        TestUserProfileRepository.create(firstProfile);
        registerTemporaryUserProfileForCleanup(user.id());

        TestUserProfile secondProfile = new TestUserProfile(
                user.id(),
                "第二份资料",
                "second-" + user.id() + "@example.test"
        );

        IllegalStateException exception = expectThrows(
                IllegalStateException.class,
                () -> TestUserProfileRepository.create(secondProfile)
        );

        assertTrue(
                exception.getCause() instanceof SQLException,
                "底层应是数据库抛出的SQL异常"
        );

        SQLException sqlException = (SQLException) exception.getCause();
        assertEquals(
                sqlException.getSQLState(),
                "23000",
                "重复userId应触发完整性约束冲突"
        );

        TestUserProfile savedProfile = TestUserProfileRepository
                .findByUserId(user.id())
                .orElseThrow(() -> new AssertionError(
                        "第一次创建的用户资料不应丢失"
                ));

        assertEquals(savedProfile, firstProfile);
    }

    @Test(groups = "database")
    @Story("用户没有资料时关联查询为空")
    @Description("用户主表有数据但资料表无匹配数据时，INNER JOIN应返回空结果")
    public void shouldReturnEmptyWhenUserHasNoProfile() {
        TestUser user = TestUserTestDataFactory.createTemporaryUser();
        TestUserRepository.create(user);
        registerTemporaryUserForCleanup(user.id());

        assertTrue(
                TestUserProfileRepository
                        .findUserWithProfileByUserId(user.id())
                        .isEmpty(),
                "用户没有资料时，INNER JOIN不应返回关联数据"
        );
    }
}
