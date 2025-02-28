package com.kalayciburak.authservice.security.authorization;

import com.kalayciburak.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthorizationHelper {
    private final UserService service;

    /**
     * Belirtilen kullanıcı ID'sinin mevcut oturum açmış kullanıcıya ait olup olmadığını kontrol eder.
     *
     * <p>Bu metod, mevcut kullanıcının kimliğini alarak verilen ID ile karşılaştırır.
     * Eğer ID, oturum açmış kullanıcıya aitse {@code true}, aksi takdirde {@code false} döner.</p>
     *
     * @param id Kontrol edilecek kullanıcı ID'si
     * @return Eğer işlem mevcut kullanıcıya aitse {@code true}, değilse {@code false}
     * @throws NullPointerException Eğer mevcut kullanıcı bulunamazsa veya ID null ise
     */
    public boolean isCurrentUser(Long id) {
        var context = SecurityContextHolder.getContext();
        var currentUsername = context.getAuthentication().getName();
        var user = service.getUserByUsername(currentUsername);

        return user.id().equals(id);
    }
}