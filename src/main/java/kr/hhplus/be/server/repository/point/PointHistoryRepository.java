package kr.hhplus.be.server.repository.point;

import kr.hhplus.be.server.domain.point.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}
