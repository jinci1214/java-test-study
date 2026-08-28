package model.response;

import java.util.List;

public class UserPageData {

    private int page;
    private int pageSize;
    private int total;
    private List<UserData> items;

    public UserPageData() {
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<UserData> getItems() {
        return items;
    }

    public void setItems(List<UserData> items) {
        this.items = items;
    }
}
