package report;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SensitiveDataMaskerTest {

    @Test
    public void shouldMaskPasswordAndToken() {

        String original = """
                  {
                    "username": "admin",
                    "password": "123456",
                    "token": "test-token-123456"
                  }
                  """;

        String masked =
                SensitiveDataMasker.mask(original);

        assertFalse(masked.contains("123456"));
        assertFalse(masked.contains("test-token-123456"));

        assertTrue(
                masked.contains("\"password\": \"***\"")
        );

        assertTrue(
                masked.contains("\"token\": \"***\"")
        );
    }

    @Test
    public void shouldMaskAuthorizationHeader() {

        String original =
                "Authorization: Bearer test-token-123456";

        String masked =
                SensitiveDataMasker.mask(original);

        assertEquals(
                masked,
                "Authorization: ***"
        );
    }

    @Test
    public void shouldAcceptNull() {

        assertNull(
                SensitiveDataMasker.mask(null)
        );
    }
}
