package database;

import database.model.TestUserAddress;
import report.AllureAttachmentUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * test_user_addresses 表的数据访问层。
 */
public final class TestUserAddressRepository {

    private static final String INSERT_SQL = """
            INSERT INTO test_user_addresses (
                id,
                user_id,
                label,
                address
            ) VALUES (?, ?, ?, ?)
            """;

    private static final String FIND_BY_USER_ID_SQL = """
            SELECT id, user_id, label, address
            FROM test_user_addresses
            WHERE user_id = ?
            ORDER BY id
            """;

    private static final String DELETE_BY_ID_SQL = """
            DELETE FROM test_user_addresses
            WHERE id = ?
            """;

    private TestUserAddressRepository() {
    }

    public static void create(TestUserAddress address) {
        validateAddress(address);

        if (TestUserRepository.findById(address.userId()).isEmpty()) {
            throw new IllegalStateException(
                    "新增用户地址失败：关联用户不存在，userId="
                            + address.userId()
            );
        }

        String parameters = "id=%d, userId=%d, label=%s, address=***"
                .formatted(
                        address.id(),
                        address.userId(),
                        address.label()
                );

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(INSERT_SQL)
        ) {
            statement.setLong(1, address.id());
            statement.setLong(2, address.userId());
            statement.setString(3, address.label());
            statement.setString(4, address.address());

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new IllegalStateException(
                        "新增用户地址失败，影响行数：" + affectedRows
                );
            }

            AllureAttachmentUtil.attachDatabaseOperation(
                    "新增用户地址",
                    INSERT_SQL,
                    parameters,
                    "影响行数=" + affectedRows
            );
        } catch (SQLException exception) {
            AllureAttachmentUtil.attachDatabaseOperation(
                    "新增用户地址（失败）",
                    INSERT_SQL,
                    parameters,
                    "异常类型=%s%nSQLState=%s%n错误信息=%s".formatted(
                            exception.getClass().getSimpleName(),
                            exception.getSQLState(),
                            exception.getMessage()
                    )
            );

            throw new IllegalStateException(
                    "新增用户地址失败：id=" + address.id(),
                    exception
            );
        }
    }

    public static List<TestUserAddress> findByUserId(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "查询用户地址的userId必须大于0"
            );
        }

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(FIND_BY_USER_ID_SQL)
        ) {
            statement.setLong(1, userId);

            List<TestUserAddress> addresses = new ArrayList<>();

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    addresses.add(new TestUserAddress(
                            resultSet.getLong("id"),
                            resultSet.getLong("user_id"),
                            resultSet.getString("label"),
                            resultSet.getString("address")
                    ));
                }
            }

            AllureAttachmentUtil.attachDatabaseOperation(
                    "按用户ID查询地址",
                    FIND_BY_USER_ID_SQL,
                    "userId=" + userId,
                    "查询到地址数量=" + addresses.size()
            );

            return List.copyOf(addresses);
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "查询用户地址失败：userId=" + userId,
                    exception
            );
        }
    }

    public static int deleteById(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "删除用户地址的id必须大于0"
            );
        }

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(DELETE_BY_ID_SQL)
        ) {
            statement.setLong(1, id);
            int affectedRows = statement.executeUpdate();

            AllureAttachmentUtil.attachDatabaseOperation(
                    "删除用户地址",
                    DELETE_BY_ID_SQL,
                    "id=" + id,
                    "影响行数=" + affectedRows
            );

            return affectedRows;
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "删除用户地址失败：id=" + id,
                    exception
            );
        }
    }

    private static void validateAddress(TestUserAddress address) {
        if (address == null) {
            throw new IllegalArgumentException("用户地址不能为空");
        }

        if (address.id() <= 0) {
            throw new IllegalArgumentException("用户地址id必须大于0");
        }

        if (address.userId() <= 0) {
            throw new IllegalArgumentException(
                    "用户地址的userId必须大于0"
            );
        }

        if (address.label() == null || address.label().isBlank()) {
            throw new IllegalArgumentException("用户地址标签不能为空");
        }

        if (address.address() == null || address.address().isBlank()) {
            throw new IllegalArgumentException("用户地址内容不能为空");
        }
    }
}
