package com.eventhub.repository;

import com.eventhub.entity.User;
import com.eventhub.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    long countByRole(Role role);

    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN'")
    List<User> findAllAdmins();

    List<User> findByEnabledTrue();

    List<User> findByEnabledFalse();

    List<User> findByAccountNonLockedFalse();

    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.accountNonLocked = true")
    List<User> findActiveUsers();

    List<User> findByUsernameContainingIgnoreCase(String username);

    List<User> findByEmailContainingIgnoreCase(String email);

    @Query("""
            SELECT u FROM User u 
            WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            """)
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    @Query(
            value = """
                    SELECT 
                        COUNT(*) as total_users,
                        SUM(CASE WHEN enabled = true AND account_non_locked = true THEN 1 ELSE 0 END) as active_users,
                        SUM(CASE WHEN role = 'ADMIN' THEN 1 ELSE 0 END) as admin_count,
                        SUM(CASE WHEN role = 'USER' THEN 1 ELSE 0 END) as user_count
                    FROM users
                    """,
            nativeQuery = true
    )
    Object[] getUserStatistics();
}
