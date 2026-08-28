package tests;

import api.UserApi;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class UserApiValidationTest {

    @Test(groups = "regression")
    public void shouldRejectNonPositivePage() {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> UserApi.getUsers(0, 1)
        );

        assertEquals(exception.getMessage(), "页码必须大于0");
    }

    @Test(groups = "regression")
    public void shouldRejectNonPositiveUserId() {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> UserApi.getUserById(0)
        );

        assertEquals(exception.getMessage(), "用户ID必须大于0");
    }

    @Test(groups = "regression")
    public void shouldRejectNonPositivePageSize() {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> UserApi.getUsers(1, 0)
        );

        assertEquals(exception.getMessage(), "每页数量必须大于0");
    }

    @Test(groups = "regression")
    public void shouldRejectBlankRoleWhenFilteringUsers() {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> UserApi.getUsersByRole(1, 2, " ")
        );

        assertEquals(exception.getMessage(), "筛选角色不能为空");
    }

    @Test(groups = "regression")
    public void shouldRejectUnsupportedUsernameSortOrder() {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> UserApi.getUsersSortedByUsername(1, 2, "ascending")
        );

        assertEquals(
                exception.getMessage(),
                "用户名排序方向只能是 asc 或 desc"
        );
    }
}
