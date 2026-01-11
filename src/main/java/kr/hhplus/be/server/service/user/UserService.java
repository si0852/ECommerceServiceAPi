package kr.hhplus.be.server.service.user;

import jakarta.persistence.EntityNotFoundException;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUserInfo(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));
    }

    @Transactional
    public User usePointWithOptimisticLock(String userId, BigDecimal amount) {
        User user = getUserInfo(userId);
        user.usePoint(amount);
        return user;
    }

    @Transactional
    public User getUserInfoWithLock(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));
    }

}
