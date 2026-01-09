package kr.hhplus.be.server.service.payment;

import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.repository.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void createPayment(Payment payment) {
        paymentRepository.save(payment);
    }
}
