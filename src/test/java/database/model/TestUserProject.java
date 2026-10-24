package database.model;

/**
 * test_user_projects 中间表的一条用户与项目关系数据。
 */
public record TestUserProject(
        long userId,
        long projectId
) {
}
