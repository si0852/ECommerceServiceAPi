package kr.hhplus.be.server.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "pw")
    private String password;

    @Column(name = "user_name", unique = true)
    private String userName;

    @Column(name = "point", precision = 10, scale = 2)
    private BigDecimal point;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public User(String userId, String password, String userName, BigDecimal point) {
        this.userId = userId;
        this.password = password;
        this.userName = userName;
        this.point = point != null ? point : BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void chargePoint(BigDecimal amount) {
        this.point = this.point.add(amount);
    }

    public void usePoint(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("사용 금액이 올바르지 않습니다.");
        }

        if (this.point.compareTo(amount) < 0) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }
        this.point = this.point.subtract(amount);
    }
}
