package common;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class TokenUtilTest {

    @AfterMethod
    public void clearTokenAfterTest() {
        TokenUtil.clear();
    }

    @Test
    public void storesAndClearsTokenTest() {
        assertFalse(TokenUtil.hasToken());

        TokenUtil.setToken("test-token");

        assertTrue(TokenUtil.hasToken());
        assertEquals(TokenUtil.getToken(), "test-token");

        TokenUtil.clear();
        assertFalse(TokenUtil.hasToken());
    }

    @Test(dataProvider = "invalidTokens")
    public void rejectsBlankTokenTest(String token) {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> TokenUtil.setToken(token)
        );

        assertEquals(exception.getMessage(), "Token不能为空");
    }

    @DataProvider
    public Object[][] invalidTokens() {
        return new Object[][]{
                {null},
                {"   "}
        };
    }
}
