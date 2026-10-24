package database;

import database.model.TestUser;
import database.model.TestUserAddress;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.util.Comparator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

@Epic("数据库测试")
@Feature("用户地址一对多关联")
public class TestUserAddressRepositoryTest extends DatabaseTestBase {

    @Test(groups = "database")
    @Story("一个用户拥有多条地址")
    @Description("为同一临时用户创建两条地址，查询并验证一对多结果")
    public void shouldCreateAndFindMultipleAddressesForSameUser() {
        TestUser user = TestUserTestDataFactory.createTemporaryUser();
        TestUserRepository.create(user);
        registerTemporaryUserForCleanup(user.id());

        TestUserAddress homeAddress = TestUserAddressTestDataFactory
                .createTemporaryAddress(user.id(), "家庭");
        TestUserAddress workAddress = TestUserAddressTestDataFactory
                .createTemporaryAddress(user.id(), "公司");

        TestUserAddressRepository.create(homeAddress);
        registerTemporaryUserAddressForCleanup(homeAddress.id());

        TestUserAddressRepository.create(workAddress);
        registerTemporaryUserAddressForCleanup(workAddress.id());

        List<TestUserAddress> actualAddresses =
                TestUserAddressRepository.findByUserId(user.id());

        List<TestUserAddress> expectedAddresses = List
                .of(homeAddress, workAddress)
                .stream()
                .sorted(Comparator.comparingLong(TestUserAddress::id))
                .toList();

        assertEquals(actualAddresses, expectedAddresses);
    }

    @Test(groups = "database")
    @Story("拒绝不存在用户的地址")
    @Description("逻辑外键校验应在写入前拒绝关联不存在用户的地址")
    public void shouldRejectAddressForNonexistentUser() {
        TestUser nonexistentUser =
                TestUserTestDataFactory.createTemporaryUser();

        assertTrue(
                TestUserRepository.findById(nonexistentUser.id()).isEmpty(),
                "测试前提：随机生成的用户ID不应存在"
        );

        TestUserAddress address = TestUserAddressTestDataFactory
                .createTemporaryAddress(
                        nonexistentUser.id(),
                        "不存在用户的地址"
                );

        IllegalStateException exception = expectThrows(
                IllegalStateException.class,
                () -> TestUserAddressRepository.create(address)
        );

        assertTrue(
                exception.getMessage().contains("关联用户不存在"),
                "异常信息应明确说明关联用户不存在"
        );
        assertTrue(
                TestUserAddressRepository
                        .findByUserId(nonexistentUser.id())
                        .isEmpty(),
                "逻辑外键校验失败后不应写入用户地址"
        );
    }

    @Test(groups = "database")
    @Story("隔离不同用户的地址")
    @Description("按userId查询时，只应返回该用户自己的地址")
    public void shouldKeepAddressesIsolatedBetweenUsers() {
        TestUser firstUser = TestUserTestDataFactory
                .createTemporaryUser();
        TestUser secondUser = TestUserTestDataFactory
                .createTemporaryUser();

        TestUserRepository.create(firstUser);
        registerTemporaryUserForCleanup(firstUser.id());
        TestUserRepository.create(secondUser);
        registerTemporaryUserForCleanup(secondUser.id());

        TestUserAddress firstUserAddress = TestUserAddressTestDataFactory
                .createTemporaryAddress(firstUser.id(), "用户一地址");
        TestUserAddress secondUserAddress = TestUserAddressTestDataFactory
                .createTemporaryAddress(secondUser.id(), "用户二地址");

        TestUserAddressRepository.create(firstUserAddress);
        registerTemporaryUserAddressForCleanup(firstUserAddress.id());
        TestUserAddressRepository.create(secondUserAddress);
        registerTemporaryUserAddressForCleanup(secondUserAddress.id());

        List<TestUserAddress> firstUserAddresses =
                TestUserAddressRepository.findByUserId(firstUser.id());
        List<TestUserAddress> secondUserAddresses =
                TestUserAddressRepository.findByUserId(secondUser.id());

        assertEquals(firstUserAddresses, List.of(firstUserAddress));
        assertEquals(secondUserAddresses, List.of(secondUserAddress));
    }

    @Test(groups = "database")
    @Story("用户没有地址时返回空列表")
    @Description("用户存在但没有地址时，一对多查询应返回空List而不是null")
    public void shouldReturnEmptyListWhenUserHasNoAddress() {
        TestUser user = TestUserTestDataFactory.createTemporaryUser();
        TestUserRepository.create(user);
        registerTemporaryUserForCleanup(user.id());

        List<TestUserAddress> addresses =
                TestUserAddressRepository.findByUserId(user.id());

        assertNotNull(addresses, "地址查询结果不应为null");
        assertTrue(addresses.isEmpty(), "没有地址时应返回空List");
    }
}
