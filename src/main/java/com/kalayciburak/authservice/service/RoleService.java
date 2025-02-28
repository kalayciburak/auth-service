package com.kalayciburak.authservice.service;

import com.kalayciburak.authservice.advice.exception.InvalidRoleIdsException;
import com.kalayciburak.authservice.advice.exception.RoleNotFoundException;
import com.kalayciburak.authservice.model.entity.Role;
import com.kalayciburak.authservice.model.enums.RoleType;
import com.kalayciburak.authservice.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kalayciburak.authservice.model.enums.RoleType.ROLE_USER;

@Service
@Transactional
@RequiredArgsConstructor
public class RoleService {
    private static final Logger log = LoggerFactory.getLogger(RoleService.class);
    private final RoleRepository repository;

    /**
     * Uygulama başladığında sistemde herhangi bir rol yoksa, varsayılan rollerin eklenmesini sağlar.
     */
    @PostConstruct
    protected void seedRoles() {
        if (repository.count() == 0) {
            var roles = Arrays.stream(RoleType.values()).map(Role::new).toList();
            repository.saveAll(roles);
            log.info("Roller başarıyla eklendi.");
        }
    }

    /**
     * Verilen ID'lere karşılık gelen rollerin listesini döndürür.
     * <p>
     * Eğer herhangi bir ID'ye sahip rol veritabanında bulunamazsa, hata fırlatır.
     *
     * @param roleIds Roller için ID listesi
     * @return Bulunan rollerin kümesi
     * @throws InvalidRoleIdsException Eğer herhangi bir rol bulunamazsa
     */
    protected Set<Role> findRolesByIds(Set<Long> roleIds) {
        var roles = fetchRolesByIds(roleIds);
        validateRoleExistence(roles, roleIds);

        return roles;
    }

    /**
     * Varsayılan olarak kullanıcıya atanacak rolleri belirler.
     * <p>
     * Varsayılan rol: {@link RoleType#ROLE_USER} rolü.
     *
     * @return Kullanıcı rollerini içeren Set<Role>.
     */
    protected Set<Role> assignDefaultRoles() {
        return Set.of(repository.findByName(ROLE_USER).orElseThrow(() -> new RoleNotFoundException(ROLE_USER)));
    }

    /**
     * Veritabanından verilen ID listesine karşılık gelen rollerin listesini çeker.
     *
     * @param roleIds Roller için ID listesi
     * @return Bulunan rollerin kümesi
     */
    private Set<Role> fetchRolesByIds(Set<Long> roleIds) {
        return new HashSet<>(repository.findAllById(roleIds));
    }

    /**
     * Eğer verilen ID'lerden bazıları veritabanında bulunamazsa, hata fırlatır.
     *
     * @param foundRoles       Veritabanında bulunan rollerin kümesi
     * @param requestedRoleIds Kullanıcının talep ettiği rol ID'leri
     * @throws InvalidRoleIdsException Eğer herhangi bir ID eksikse
     */
    private void validateRoleExistence(Set<Role> foundRoles, Set<Long> requestedRoleIds) {
        if (foundRoles.size() != requestedRoleIds.size()) {
            var foundRoleIds = foundRoles.stream().map(Role::getId).collect(Collectors.toSet());
            var missingRoleIds = new HashSet<>(requestedRoleIds);
            missingRoleIds.removeAll(foundRoleIds);

            throw new InvalidRoleIdsException(missingRoleIds);
        }
    }
}
