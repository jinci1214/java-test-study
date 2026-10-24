package database;

import database.model.TestUserProject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class TestUserProjectRepositoryValidationTest {

    @DataProvider(name = "invalidRelations")
    public Object[][] invalidRelations() {
        return new Object[][]{
                {null, "用户项目关系不能为空"},
                {
                        new TestUserProject(0L, 1L),
                        "用户项目关系的userId必须大于0"
                },
                {
                        new TestUserProject(1L, 0L),
                        "用户项目关系的projectId必须大于0"
                }
        };
    }

    @Test(dataProvider = "invalidRelations", groups = "database")
    public void shouldRejectInvalidRelationBeforeAccessingDatabase(
            TestUserProject relation,
            String expectedMessage
    ) {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> TestUserProjectRepository.create(relation)
        );

        assertEquals(exception.getMessage(), expectedMessage);
    }

    @Test(groups = "database")
    public void shouldRejectInvalidUserIdWhenQueryingProjects() {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> TestUserProjectRepository.findProjectsByUserId(0L)
        );

        assertEquals(
                exception.getMessage(),
                "查询用户项目的userId必须大于0"
        );
    }

    @Test(groups = "database")
    public void shouldRejectInvalidProjectIdWhenQueryingUsers() {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> TestUserProjectRepository.findUsersByProjectId(0L)
        );

        assertEquals(
                exception.getMessage(),
                "查询项目用户的projectId必须大于0"
        );
    }
}
