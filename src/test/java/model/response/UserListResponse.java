package model.response;

public class UserListResponse {

    private int code;
    private String message;
    private UserPageData data;

    public UserListResponse() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserPageData getData() {
        return data;
    }

    public void setData(UserPageData data) {
        this.data = data;
    }
}
