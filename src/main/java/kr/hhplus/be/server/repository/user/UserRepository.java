package kr.hhplus.be.server.repository.user;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("select u from User u where u.userId = :userId")
//    Optional<User> findByUserId(String userId);
}
