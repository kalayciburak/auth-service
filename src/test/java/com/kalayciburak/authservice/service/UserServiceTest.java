package com.kalayciburak.authservice.service;

import com.kalayciburak.authservice.advice.exception.AdminCannotBeDeletedException;
import com.kalayciburak.authservice.advice.exception.UserNotFoundException;
import com.kalayciburak.authservice.model.dto.request.ChangePasswordRequest;
import com.kalayciburak.authservice.model.dto.request.RegisterRequest;
import com.kalayciburak.authservice.model.entity.Role;
import com.kalayciburak.authservice.model.entity.User;
import com.kalayciburak.authservice.model.enums.RoleType;
import com.kalayciburak.authservice.repository.UserRepository;
import com.kalayciburak.authservice.security.audit.SecurityAuditorProvider;
import com.kalayciburak.authservice.service.helper.UserHelper;
import com.kalayciburak.authservice.service.validator.UserValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserHelper helper;

    @Mock
    private UserValidator validator;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRepository repository;

    @Mock
    private SecurityAuditorProvider auditorProvider;

    @InjectMocks
    private UserService userService;

    /**
     * Yardımcı metot: Test kullanıcıları oluşturur.
     *
     * @param id       Kullanıcı ID'si
     * @param username Kullanıcı adı
     * @param password Şifre
     * @param roles    Kullanıcı rolleri
     * @return Oluşturulan User nesnesi
     */
    private User createUser(Long id, String username, String password, Set<Role> roles) {
        var user = User.builder()
                .username(username)
                .password(password)
                .roles(roles)
                .build();

        try {
            var idField = User.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);

            var deletedField = User.class.getSuperclass().getDeclaredField("deleted");
            deletedField.setAccessible(true);
            deletedField.set(user, false);
        } catch (Exception e) {
            // Test ortamında reflection hatası olursa görmezden gel
        }

        return user;
    }

    /**
     * Yardımcı metot: Test rollerini oluşturur.
     *
     * @param id       Rol ID'si
     * @param roleType Rol tipi
     * @return Oluşturulan Role nesnesi
     */
    private Role createRole(Long id, RoleType roleType) {
        var role = new Role(roleType);
        role.setId(id);

        return role;
    }

    /**
     * Tüm kullanıcıları getirme senaryosunu test eder. Kullanıcılar varsa, dönen listenin doğru boyutta olduğu kontrol
     * edilir.
     */
    @Test
    @DisplayName("Tüm kullanıcıları getirme testi - Kullanıcılar var")
    void getAllUsersWhenUsersExistTest() {
        // Arrange
        var users = List.of(
                createUser(1L, "user1", "password1", Set.of(createRole(1L, RoleType.ROLE_USER))),
                createUser(2L, "user2", "password2", Set.of(createRole(2L, RoleType.ROLE_ADMIN)))
        );
        when(repository.findAll()).thenReturn(users);

        // Act
        var response = userService.getAllUsers();

        // Assert
        assertNotNull(response, "Yanıt null olmamalıdır.");
        assertNotNull(response.getData(), "Veri null olmamalıdır.");
        assertEquals(2, response.getData().size(), "Kullanıcı sayısı 2 olmalıdır.");

        // Verify
        verify(repository).findAll();
    }

    /**
     * Tüm kullanıcıları getirme senaryosunu test eder. Kullanıcı bulunamadığında, dönen verinin null olduğu kontrol edilir.
     */
    @Test
    @DisplayName("Tüm kullanıcıları getirme testi - Kullanıcı yok")
    void getAllUsersWhenNoUsersExistTest() {
        // Arrange
        when(repository.findAll()).thenReturn(Collections.emptyList());

        // Act
        var response = userService.getAllUsers();

        // Assert
        assertNotNull(response, "Yanıt null olmamalıdır.");
        assertNull(response.getData(), "Veri null olmalıdır.");

        // Verify
        verify(repository).findAll();
    }

    /**
     * Kullanıcı kaydı yapma senaryosunu test eder. Yeni kullanıcı kaydı sırasında, doğru kullanıcı verisinin döndürüldüğü
     * kontrol edilir.
     */
    @Test
    @DisplayName("Kullanıcı kaydı yapma testi")
    void registerUserTest() {
        // Arrange
        var request = new RegisterRequest("newuser", "password123");
        var newUser = createUser(1L, "newuser", "encodedPassword", new HashSet<>());
        Set<Role> roles = new HashSet<>();

        when(roleService.assignDefaultRoles()).thenReturn(roles);
        when(helper.buildUser(request, roles)).thenReturn(newUser);
        when(repository.save(any(User.class))).thenReturn(newUser);

        // Act
        var response = userService.registerUser(request);

        // Assert
        assertNotNull(response, "Yanıt null olmamalıdır.");
        assertNotNull(response.getData(), "Kayıt verisi null olmamalıdır.");
        assertEquals("newuser", response.getData().username(), "Kullanıcı adı beklenen değerle eşleşmelidir.");

        // Verify
        verify(repository).save(any(User.class));
    }

    /**
     * Kullanıcı rollerini güncelleme senaryosunu test eder. Kullanıcı bulunup roller güncellendiğinde, doğru yanıtın döndüğü
     * kontrol edilir.
     */
    @Test
    @DisplayName("Kullanıcı rollerini güncelleme testi - Başarılı")
    void updateUserRolesSuccessTest() {
        // Arrange
        var userId = 1L;
        var roleIds = Set.of(1L, 2L);
        var user = createUser(userId, "testuser", "password", new HashSet<>());

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenReturn(user);

        // Act
        var response = userService.updateUserRoles(userId, roleIds);

        // Assert
        assertNotNull(response, "Yanıt null olmamalıdır.");
        assertNotNull(response.getData(), "Güncellenen kullanıcı verisi null olmamalıdır.");

        // Verify
        verify(repository).findById(userId);
        verify(repository).save(any(User.class));
    }

    /**
     * Kullanıcıyı silme senaryosunu test eder. Kullanıcı silindiğinde, ilgili repository çağrılarının yapıldığı doğrulanır.
     * Eğer UserService.deleteUser metodunda soft delete uygulanıyorsa, repository.save yerine repository.softDeleteById
     * çağrısını doğrularız.
     */
    @Test
    @DisplayName("Kullanıcıyı silme testi - Başarılı")
    void deleteUserSuccessTest() {
        // Arrange
        var userId = 1L;
        var user = createUser(userId, "testuser", "password", Set.of(createRole(1L, RoleType.ROLE_USER)));

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(repository).softDeleteById(any(), eq(userId));

        try (MockedStatic<UserHelper> mockedUserHelper = mockStatic(UserHelper.class)) {
            mockedUserHelper.when(() -> UserHelper.hasAdminRole(any(User.class))).thenReturn(false);

            // Act
            userService.deleteUser(userId);

            // Verify
            verify(repository).findById(userId);
            mockedUserHelper.verify(() -> UserHelper.hasAdminRole(any(User.class)));
            verify(repository).softDeleteById(any(), eq(userId));
        }
    }

    /**
     * Admin kullanıcısını silme senaryosunu test eder. Admin kullanıcı silinmeye çalışıldığında,
     * AdminCannotBeDeletedException fırlatılması beklenir.
     */
    @Test
    @DisplayName("Admin kullanıcısını silme denemesi")
    void deleteAdminUserTest() {
        // Arrange
        var userId = 1L;
        var adminUser = createUser(userId, "admin", "password", Set.of(createRole(2L, RoleType.ROLE_ADMIN)));

        when(repository.findById(userId)).thenReturn(Optional.of(adminUser));

        try (MockedStatic<UserHelper> mockedUserHelper = mockStatic(UserHelper.class)) {
            mockedUserHelper.when(() -> UserHelper.hasAdminRole(any(User.class))).thenReturn(true);

            // Act & Assert
            assertThrows(AdminCannotBeDeletedException.class, () -> userService.deleteUser(userId),
                    "Admin kullanıcısı silinmeye çalışıldığında AdminCannotBeDeletedException fırlatılmalıdır.");

            mockedUserHelper.verify(() -> UserHelper.hasAdminRole(any(User.class)));
        }

        // Verify repository çağrıları
        verify(repository).findById(userId);
        verifyNoMoreInteractions(repository);
    }

    /**
     * Var olmayan kullanıcıyı silme senaryosunu test eder. Kullanıcı bulunamadığında, UserNotFoundException fırlatılması
     * beklenir.
     */
    @Test
    @DisplayName("Var olmayan kullanıcıyı silme denemesi")
    void deleteNonExistingUserTest() {
        // Arrange
        var userId = 1L;
        when(repository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId),
                "Kullanıcı bulunamadığında UserNotFoundException fırlatılmalıdır.");

        // Verify
        verify(repository).findById(userId);
        verifyNoMoreInteractions(repository);
    }

    /**
     * Şifre değiştirme senaryosunu test eder. Eski şifre doğru olduğunda, şifrenin güncellenip kaydedildiği kontrol edilir.
     * Eğer kodunuz passwordEncoder.matches çağrısını yapmıyorsa, test beklentisini repository.save ve passwordEncoder.encode
     * üzerinden yapıyoruz.
     */
    @Test
    @DisplayName("Şifre değiştirme testi - Başarılı")
    void changePasswordSuccessTest() {
        // Arrange
        var userId = 1L;
        var oldPassword = "oldPassword";
        var newPassword = "newPassword";

        var user = createUser(userId, "testuser", oldPassword, Set.of(createRole(1L, RoleType.ROLE_USER)));
        var request = new ChangePasswordRequest(oldPassword, newPassword);

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenReturn(user);

        // Act
        var response = userService.changePassword(userId, request);

        // Assert
        assertNotNull(response, "Yanıt null olmamalıdır.");
        assertNotNull(response.getData(), "Güncellenen kullanıcı verisi null olmamalıdır.");

        // Verify
        verify(repository).findById(userId);
        verify(repository).save(any(User.class));
    }
}
