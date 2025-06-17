package com.kalayciburak.authservice.service;

import com.kalayciburak.authservice.model.entity.User;
import com.kalayciburak.authservice.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Veritabanından kullanıcıyı kullanıcı adına göre bulur.
     *
     * @param username Kullanıcı adı
     * @return Spring Security için UserDetails nesnesi
     * @throws UsernameNotFoundException Eğer kullanıcı bulunamazsa
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var message = String.format("Kullanıcı bulunamadı: %s", username);
        var user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(message));

        return buildUserDetails(user);
    }

    /**
     * Entity'den Spring Security User nesnesi oluşturur.
     *
     * @param user Entity olarak bulunan kullanıcı
     * @return UserDetails nesnesi
     */
    private UserDetails buildUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                mapRolesToAuthorities(user)
        );
    }

    /**
     * Kullanıcının rollerini, Spring Security tarafından kullanılabilir yetkilere dönüştürür.
     *
     * @param user Kullanıcı entity'si
     * @return Rol listesinin SimpleGrantedAuthority nesnelerine dönüşümü
     */
    private List<SimpleGrantedAuthority> mapRolesToAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .toList();
    }
}