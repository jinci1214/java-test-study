package database;

import database.model.TestProject;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Epic("数据库测试")
@Feature("项目数据管理")
public class TestProjectRepositoryTest extends DatabaseTestBase {

    @Test(groups = "database")
    @Story("新增并查询临时项目")
    @Description("创建唯一临时项目，按ID查询验证后自动清理")
    public void shouldCreateAndFindTemporaryProject() {
        TestProject expectedProject =
                TestProjectTestDataFactory.createTemporaryProject();

        TestProjectRepository.create(expectedProject);
        registerTemporaryProjectForCleanup(expectedProject.id());

        TestProject actualProject = TestProjectRepository
                .findById(expectedProject.id())
                .orElseThrow(() -> new AssertionError(
                        "新增后未查询到临时项目：id="
                                + expectedProject.id()
                ));

        assertEquals(actualProject, expectedProject);
    }
}
