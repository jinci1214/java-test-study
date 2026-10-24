package database;

import database.model.TestProject;
import database.model.TestUser;
import database.model.TestUserProject;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

@Epic("数据库测试")
@Feature("用户与项目多对多关联")
public class TestUserProjectRepositoryTest extends DatabaseTestBase {

    @Test(groups = "database")
    @Story("双向查询用户与项目关系")
    @Description("建立两个用户与两个项目的关系，并从两个方向查询验证")
    public void shouldQueryManyToManyRelationshipInBothDirections() {
        TestUser firstUser = TestUserTestDataFactory
                .createTemporaryUser();
        TestUser secondUser = TestUserTestDataFactory
                .createTemporaryUser();
        TestUserRepository.create(firstUser);
        registerTemporaryUserForCleanup(firstUser.id());
        TestUserRepository.create(secondUser);
        registerTemporaryUserForCleanup(secondUser.id());

        TestProject firstProject = TestProjectTestDataFactory
                .createTemporaryProject();
        TestProject secondProject = TestProjectTestDataFactory
                .createTemporaryProject();
        TestProjectRepository.create(firstProject);
        registerTemporaryProjectForCleanup(firstProject.id());
        TestProjectRepository.create(secondProject);
        registerTemporaryProjectForCleanup(secondProject.id());

        createAndRegisterRelation(firstUser.id(), firstProject.id());
        createAndRegisterRelation(firstUser.id(), secondProject.id());
        createAndRegisterRelation(secondUser.id(), firstProject.id());

        List<TestProject> expectedFirstUserProjects = List
                .of(firstProject, secondProject)
                .stream()
                .sorted(Comparator.comparingLong(TestProject::id))
                .toList();
        List<TestUser> expectedFirstProjectUsers = List
                .of(firstUser, secondUser)
                .stream()
                .sorted(Comparator.comparingLong(TestUser::id))
                .toList();

        assertEquals(
                TestUserProjectRepository
                        .findProjectsByUserId(firstUser.id()),
                expectedFirstUserProjects
        );
        assertEquals(
                TestUserProjectRepository
                        .findProjectsByUserId(secondUser.id()),
                List.of(firstProject)
        );
        assertEquals(
                TestUserProjectRepository
                        .findUsersByProjectId(firstProject.id()),
                expectedFirstProjectUsers
        );
        assertEquals(
                TestUserProjectRepository
                        .findUsersByProjectId(secondProject.id()),
                List.of(firstUser)
        );
    }

    @Test(groups = "database")
    @Story("拒绝不存在用户的项目关系")
    @Description("用户不存在时，逻辑外键校验应拒绝写入中间表")
    public void shouldRejectRelationForNonexistentUser() {
        TestUser nonexistentUser =
                TestUserTestDataFactory.createTemporaryUser();
        TestProject project = TestProjectTestDataFactory
                .createTemporaryProject();
        TestProjectRepository.create(project);
        registerTemporaryProjectForCleanup(project.id());

        IllegalStateException exception = expectThrows(
                IllegalStateException.class,
                () -> TestUserProjectRepository.create(
                        new TestUserProject(
                                nonexistentUser.id(),
                                project.id()
                        )
                )
        );

        assertTrue(
                exception.getMessage().contains("关联用户不存在"),
                "异常信息应明确说明关联用户不存在"
        );
        assertTrue(
                TestUserProjectRepository
                        .findUsersByProjectId(project.id())
                        .isEmpty(),
                "校验失败后不应写入用户项目关系"
        );
    }

    @Test(groups = "database")
    @Story("拒绝不存在项目的用户关系")
    @Description("项目不存在时，逻辑外键校验应拒绝写入中间表")
    public void shouldRejectRelationForNonexistentProject() {
        TestUser user = TestUserTestDataFactory.createTemporaryUser();
        TestProject nonexistentProject = TestProjectTestDataFactory
                .createTemporaryProject();
        TestUserRepository.create(user);
        registerTemporaryUserForCleanup(user.id());

        IllegalStateException exception = expectThrows(
                IllegalStateException.class,
                () -> TestUserProjectRepository.create(
                        new TestUserProject(
                                user.id(),
                                nonexistentProject.id()
                        )
                )
        );

        assertTrue(
                exception.getMessage().contains("关联项目不存在"),
                "异常信息应明确说明关联项目不存在"
        );
        assertTrue(
                TestUserProjectRepository
                        .findProjectsByUserId(user.id())
                        .isEmpty(),
                "校验失败后不应写入用户项目关系"
        );
    }

    @Test(groups = "database")
    @Story("拒绝重复的用户项目关系")
    @Description("同一用户重复加入同一项目时，联合主键应拒绝第二次插入")
    public void shouldRejectDuplicateUserProjectRelation() {
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
        TestUserProjectRepository.create(relation);
        registerTemporaryUserProjectForCleanup(
                relation.userId(),
                relation.projectId()
        );

        IllegalStateException exception = expectThrows(
                IllegalStateException.class,
                () -> TestUserProjectRepository.create(relation)
        );

        assertTrue(
                exception.getCause() instanceof SQLException,
                "底层应是数据库抛出的SQL异常"
        );
        SQLException sqlException = (SQLException) exception.getCause();
        assertEquals(
                sqlException.getSQLState(),
                "23000",
                "重复联合主键应触发完整性约束冲突"
        );
        assertEquals(
                TestUserProjectRepository
                        .findProjectsByUserId(user.id()),
                List.of(project),
                "重复插入失败后应只保留一条关系"
        );
    }

    private void createAndRegisterRelation(long userId, long projectId) {
        TestUserProjectRepository.create(
                new TestUserProject(userId, projectId)
        );
        registerTemporaryUserProjectForCleanup(userId, projectId);
    }
}
