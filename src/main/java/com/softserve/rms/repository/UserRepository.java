package com.softserve.rms.repository;

import com.softserve.rms.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Save user
     *
     * @param user to save.
     * @return User saved}
     */
    User save(User user);

    /**
     * Find User by email.
     *
     * @param email user email.
     * @return user {@link User}
     */
    Optional<User> findUserByEmail(String email);

    /**
     * Method that check if user with this email already exist
     *
     * @param email
     * @author Mariia Shchur
     */
    boolean existsUserByEmail(String email);

    /**
     * Method that check if user with this phone number already exist
     *
     * @param phone
     * @author Mariia Shchur
     */
    boolean existsUserByPhone(String phone);

    /**
     * Method that delete user by email
     *
     * @param email
     * @author Mariia Shchur
     */
    void deleteByEmail(String email);

    /**
     * Method that find user by entered reset token
     *
     * @param resetToken
     * @author Mariia Shchur
     */
    Optional<User> findUserByResetToken(String resetToken);
}
