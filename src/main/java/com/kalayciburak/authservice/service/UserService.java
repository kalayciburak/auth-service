package com.kalayciburak.authservice.service;

import com.kalayciburak.authservice.advice.exception.AdminCannotBeDeletedException;
import com.kalayciburak.authservice.advice.exception.InvalidRoleIdsException;
import com.kalayciburak.authservice.advice.exception.UserNotFoundException;
import com.kalayciburak.authservice.model.dto.request.ChangePasswordRequest;
import com.kalayciburak.authservice.model.dto.request.PasswordRequest;
import com.kalayciburak.authservice.model.dto.request.RegisterRequest;
import com.kalayciburak.authservice.model.dto.response.UserResponse;
import com.kalayciburak.authservice.model.entity.User;
import com.kalayciburak.authservice.repository.UserRepository;
import com.kalayciburak.authservice.security.audit.SecurityAuditorProvider;
import com.kalayciburak.authservice.service.helper.UserHelper;
import com.kalayciburak.authservice.service.validator.UserValidator;
import com.kalayciburak.commonpackage.core.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.kalayciburak.authservice.service.helper.UserHelper.hasAdminRole;
import static com.kalayciburak.commonpackage.core.constant.Messages.User.*;
import static com.kalayciburak.commonpackage.core.response.builder.ResponseBuilder.createNotFoundResponse;
import static com.kalayciburak.commonpackage.core.response.builder.ResponseBuilder.createSuccessResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserHelper helper;
    private final UserValidator validator;
    private final RoleService roleService;
    private final UserRepository repository;
    private final SecurityAuditorProvider auditorProvider;

    /**
     * Tüm kullanıcıları getirir.
     *
     * @return Kullanıcı listesi
     */
    @Transactional(readOnly = true)
    public SuccessResponse<List<UserResponse>> getAllUsers() {
        var response = repository.findAll().stream().map(UserResponse::from).toList();
        if (response.isEmpty()) return createNotFoundResponse(NOT_FOUND);

        return createSuccessResponse(response, LISTED);
    }

    /**
     * Kullanıcı adına göre kullanıcıyı getirir.
     * <p>
     * Eğer kullanıcı bulunamazsa {@link UserNotFoundException} fırlatır.
     *
     * @param username Kullanıcı adı
     * @return Kullanıcı bilgileri
     */
    @Transactional(readOnly = true)
    public SuccessResponse<UserResponse> getUserByUsername(String username) {
        var user = repository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        var response = UserResponse.from(user);

        return createSuccessResponse(response, FOUND);
    }

    /**
     * Kullanıcı kaydı oluşturur.
     * <p>
     * <ol>
     *       <li>Kullanıcı adının eşsiz olup olmadığını kontrol eder.</li>
     *       <li>Parolanın veri ihlallerine karşı durumunu kontrol eder.</li>
     *       <li>Varsayılan rolleri kullanıcıya atar.</li>
     *       <li>Yeni kullanıcıyı kaydeder.</li>
     *       <li>Kaydedilen kullanıcı bilgilerini döndürür.</li>
     * </ol>
     *
     * @param request Kullanıcı bilgileri
     * @return Kaydedilen kullanıcı bilgileri
     */
    public SuccessResponse<UserResponse> registerUser(RegisterRequest request) {
        validator.validateUniqueUsername(request.username());
        validator.validatePasswordDataBreachStatus(request.password());
        var roles = roleService.assignDefaultRoles();
        var user = helper.buildUser(request, roles);
        var newUser = repository.save(user);
        var response = UserResponse.from(newUser);

        return createSuccessResponse(response, REGISTER_SUCCESS);
    }

    /**
     * Kullanıcının rollerini günceller.
     * <p>
     * <ol>
     *     <li>Kullanıcının mevcut bilgileri veritabanından alınır.</li>
     *     <li>Seçilen rollerin geçerli olup olmadığı kontrol edilir.</li>
     *     <li>Eğer roller değişmemişse, işlem yapılmaz.</li>
     *     <li>Yeni roller kullanıcıya atanır ve veritabanına kaydedilir.</li>
     *     <li>Güncellenmiş kullanıcı bilgileri döndürülür.</li>
     * </ol>
     *
     * @param id      Kullanıcı ID'si
     * @param roleIds Kullanıcıya atanacak rollerin ID'leri
     * @return Güncellenmiş kullanıcı bilgileri
     * @throws InvalidRoleIdsException Eğer herhangi bir rol ID'si geçersizse
     */
    public SuccessResponse<UserResponse> updateUserRoles(Long id, Set<Long> roleIds) {
        var user = findUserById(id);
        var newRoles = roleService.findRolesByIds(roleIds);
        if (user.getRoles().equals(newRoles)) createSuccessResponse(ROLES_NOT_CHANGED);

        user.setRoles(newRoles);
        var updatedUser = repository.save(user);
        var response = UserResponse.from(updatedUser);

        return createSuccessResponse(response, ROLES_UPDATED);
    }

    /**
     * Kullanıcının parolasını günceller.
     * <p>
     * <ol>
     *     <li>Yeni parolanın veri ihlallerine karşı durumu kontrol edilir.</li>
     *     <li>Parola güncelleme işlemi gerçekleştirilir.</li>
     *     <li>Güncellenmiş kullanıcı bilgileri döndürülür.</li>
     * </ol>
     *
     * @param id      Kullanıcı ID'si
     * @param request Parola güncelleme isteği
     * @return Güncellenmiş kullanıcı bilgileri
     */
    public SuccessResponse<UserResponse> updatePassword(Long id, PasswordRequest request) {
        validator.validatePasswordDataBreachStatus(request.password());
        var user = findUserById(id);
        user.setPassword(helper.encodePassword(request.password()));
        var updatedUser = repository.save(user);
        var response = UserResponse.from(updatedUser);

        return createSuccessResponse(response, PASSWORD_UPDATED);
    }

    /**
     * Kullanıcının parolasını yeni bir parola ile değiştirir.
     * <p>
     * <ol>
     *     <li>Eski parola doğruluğu kontrol edilir.</li>
     *     <li>Yeni parolanın veri ihlallerine karşı durumu kontrol edilir.</li>
     *     <li>Parola değiştirme işlemi gerçekleştirilir.</li>
     *     <li>Değiştirilen kullanıcı bilgileri döndürülür.</li>
     * </ol>
     *
     * @param id      Kullanıcı ID'si
     * @param request Parola değiştirme isteği
     * @return Güncellenmiş kullanıcı bilgileri
     */
    public SuccessResponse<UserResponse> changePassword(Long id, ChangePasswordRequest request) {
        var user = findUserById(id);
        validator.validateOldPassword(request, user);
        validator.validatePasswordDataBreachStatus(request.newPassword());
        user.setPassword(helper.encodePassword(request.newPassword()));
        var updatedUser = repository.save(user);
        var response = UserResponse.from(updatedUser);

        return createSuccessResponse(response, PASSWORD_CHANGED);
    }

    /**
     * Kullanıcıyı (soft) siler.
     * <p>
     * <ol>
     *     <li>Silinecek kullanıcıyı veritabanından getirir.</li>
     *     <li>Kullanıcının ADMIN rolüne sahip olup olmadığını kontrol eder.</li>
     *     <li>Eğer kullanıcı ADMIN rolüne sahipse, {@link AdminCannotBeDeletedException} fırlatılır.</li>
     *     <li>Eğer kullanıcı ADMIN değilse, (soft) silme işlemi gerçekleştirilir.</li>
     * </ol>
     *
     * @param id Silinecek kullanıcının ID'si
     * @throws AdminCannotBeDeletedException Eğer kullanıcı ADMIN ise
     */
    public void deleteUser(Long id) {
        var user = findUserById(id);
        if (hasAdminRole(user)) throw new AdminCannotBeDeletedException();
        repository.softDeleteById(auditorProvider.getCurrentAuditor(), id);
    }

    /**
     * ID'ye göre kullanıcıyı bulur.
     * <p>
     * Eğer kullanıcı bulunamazsa {@link UserNotFoundException} fırlatır.
     *
     * @param id Kullanıcı ID'si
     * @return Kullanıcı
     */
    protected User findUserById(Long id) {
        return repository.findById(id).orElseThrow(UserNotFoundException::new);
    }
}
