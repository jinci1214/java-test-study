package validation;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class RoleValidatorTest {

    @Test
    public void acceptsNonBlankRoleTest() {
        RoleValidator.validate("tester");
    }

    @Test(dataProvider = "invalidRoles")
    public void rejectsBlankRoleTest(String role) {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> RoleValidator.validate(role)
        );

        assertEquals(exception.getMessage(), "筛选角色不能为空");
    }

    @DataProvider
    public Object[][] invalidRoles() {
        return new Object[][]{
                {null},
                {"   "}
        };
    }
}
