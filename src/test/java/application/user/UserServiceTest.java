package application.user;

import org.springframework.dao.DuplicateKeyException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.expectThrows;

public class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test(groups = {"spring", "regression"})
    public void shouldReturnUserWhenRepositoryFindsUser() {
        UserResponse expectedUser =
                new UserResponse(1L, "admin", "administrator");
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(expectedUser));

        UserResponse actualUser = userService.findById(1L);

        assertEquals(actualUser, expectedUser);
        verify(userRepository).findById(1L);
    }

    @Test(groups = {"spring", "regression"})
    public void shouldThrowWhenRepositoryCannotFindUser() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = expectThrows(
                UserNotFoundException.class,
                () -> userService.findById(999L)
        );

        assertEquals(exception.getMessage(), "用户不存在，id=999");
        verify(userRepository).findById(999L);
    }

    @Test(groups = {"spring", "regression"})
    public void shouldCreateUserWhenUsernameIsAvailable() {
        UserResponse createdUser =
                new UserResponse(3L, "new-user", "tester");
        when(userRepository.findByUsername("new-user"))
                .thenReturn(Optional.empty());
        when(userRepository.create("new-user", "tester"))
                .thenReturn(createdUser);

        UserResponse actualUser = userService.create(
                "new-user",
                "tester"
        );

        assertEquals(actualUser, createdUser);
        verify(userRepository).findByUsername("new-user");
        verify(userRepository).create("new-user", "tester");
    }

    @Test(groups = {"spring", "regression"})
    public void shouldRejectDuplicateUsernameBeforeCreate() {
        UserResponse existingUser =
                new UserResponse(1L, "admin", "administrator");
        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(existingUser));

        UserAlreadyExistsException exception = expectThrows(
                UserAlreadyExistsException.class,
                () -> userService.create("admin", "tester")
        );

        assertEquals(exception.getMessage(), "用户名已存在：admin");
        verify(userRepository).findByUsername("admin");
        verify(userRepository, never()).create("admin", "tester");
    }

    @Test(groups = {"spring", "regression"})
    public void shouldConvertDuplicateKeyDuringCreate() {
        DuplicateKeyException databaseException =
                new DuplicateKeyException("duplicate username");
        when(userRepository.findByUsername("racing-user"))
                .thenReturn(Optional.empty());
        when(userRepository.create("racing-user", "tester"))
                .thenThrow(databaseException);

        UserAlreadyExistsException exception = expectThrows(
                UserAlreadyExistsException.class,
                () -> userService.create("racing-user", "tester")
        );

        assertEquals(
                exception.getMessage(),
                "用户名已存在：racing-user"
        );
        assertSame(exception.getCause(), databaseException);
    }

    @Test(groups = {"spring", "regression"})
    public void shouldUpdateUserWhenUsernameIsAvailable() {
        UserResponse existingUser =
                new UserResponse(3L, "old-user", "tester");
        UserResponse updatedUser =
                new UserResponse(3L, "updated-user", "administrator");
        when(userRepository.findById(3L))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("updated-user"))
                .thenReturn(Optional.empty());
        when(
                userRepository.update(
                        3L,
                        "updated-user",
                        "administrator"
                )
        ).thenReturn(Optional.of(updatedUser));

        UserResponse actualUser = userService.update(
                3L,
                "updated-user",
                "administrator"
        );

        assertEquals(actualUser, updatedUser);
        verify(userRepository).findById(3L);
        verify(userRepository).findByUsername("updated-user");
        verify(userRepository).update(
                3L,
                "updated-user",
                "administrator"
        );
    }

    @Test(groups = {"spring", "regression"})
    public void shouldStopUpdateWhenUserDoesNotExist() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = expectThrows(
                UserNotFoundException.class,
                () -> userService.update(
                        999L,
                        "updated-user",
                        "tester"
                )
        );

        assertEquals(exception.getMessage(), "用户不存在，id=999");
        verify(userRepository).findById(999L);
        verify(userRepository, never())
                .findByUsername("updated-user");
        verify(userRepository, never())
                .update(999L, "updated-user", "tester");
    }

    @Test(groups = {"spring", "regression"})
    public void shouldRejectDuplicateUsernameBeforeUpdate() {
        UserResponse userToUpdate =
                new UserResponse(3L, "old-user", "tester");
        UserResponse userWithDuplicateName =
                new UserResponse(1L, "admin", "administrator");
        when(userRepository.findById(3L))
                .thenReturn(Optional.of(userToUpdate));
        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(userWithDuplicateName));

        UserAlreadyExistsException exception = expectThrows(
                UserAlreadyExistsException.class,
                () -> userService.update(3L, "admin", "tester")
        );

        assertEquals(exception.getMessage(), "用户名已存在：admin");
        verify(userRepository).findById(3L);
        verify(userRepository).findByUsername("admin");
        verify(userRepository, never())
                .update(3L, "admin", "tester");
    }

    @Test(groups = {"spring", "regression"})
    public void shouldConvertDuplicateKeyDuringUpdate() {
        UserResponse existingUser =
                new UserResponse(3L, "old-user", "tester");
        DuplicateKeyException databaseException =
                new DuplicateKeyException("duplicate username");
        when(userRepository.findById(3L))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("racing-user"))
                .thenReturn(Optional.empty());
        when(
                userRepository.update(
                        3L,
                        "racing-user",
                        "administrator"
                )
        ).thenThrow(databaseException);

        UserAlreadyExistsException exception = expectThrows(
                UserAlreadyExistsException.class,
                () -> userService.update(
                        3L,
                        "racing-user",
                        "administrator"
                )
        );

        assertEquals(
                exception.getMessage(),
                "用户名已存在：racing-user"
        );
        assertSame(exception.getCause(), databaseException);
    }

    @Test(groups = {"spring", "regression"})
    public void shouldDeleteUserWhenRepositoryDeletesUser() {
        when(userRepository.delete(3L)).thenReturn(true);

        userService.delete(3L);

        verify(userRepository).delete(3L);
    }

    @Test(groups = {"spring", "regression"})
    public void shouldThrowWhenRepositoryCannotDeleteUser() {
        when(userRepository.delete(999L)).thenReturn(false);

        UserNotFoundException exception = expectThrows(
                UserNotFoundException.class,
                () -> userService.delete(999L)
        );

        assertEquals(exception.getMessage(), "用户不存在，id=999");
        verify(userRepository).delete(999L);
    }
}
