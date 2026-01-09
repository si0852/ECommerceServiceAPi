package kr.hhplus.be.server.service;

import kr.hhplus.be.server.domain.point.PointHistory;
import kr.hhplus.be.server.repository.point.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;

    public void savePointHistory(PointHistory pointHistory) {
        pointHistoryRepository.save(pointHistory);
    }
}
