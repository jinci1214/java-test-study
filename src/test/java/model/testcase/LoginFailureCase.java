package model.testcase;

import model.request.LoginRequest;

public record LoginFailureCase(
        LoginRequest request,
        int expectedHttpStatus,
        int expectedCode,
        String expectedMessage,
        String scenario
) {
}
