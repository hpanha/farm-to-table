package farm_to_table.farm_to_table.Service;

import farm_to_table.farm_to_table.DTO.UserTermAgreement.AgreementCheckDto;
import farm_to_table.farm_to_table.DTO.UserTermAgreement.UserTermAgreementRequestDto;
import farm_to_table.farm_to_table.DTO.UserTermAgreement.UserTermAgreementResponseDto;
import farm_to_table.farm_to_table.DTO.UserTermAgreement.UserTermAgreementStatusDto;
import farm_to_table.farm_to_table.Repository.CustomerRepository;
import farm_to_table.farm_to_table.Repository.TermsConditionRepository;
import farm_to_table.farm_to_table.Repository.UserRepository;
import farm_to_table.farm_to_table.Repository.UserTermAgreementRepository;
import farm_to_table.farm_to_table.model.AgreementStatus;
import farm_to_table.farm_to_table.model.TermsConditionEntity;
import farm_to_table.farm_to_table.model.TermsStatus;
import farm_to_table.farm_to_table.model.UserTermAgreementsEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class UserTermAgreementServiceImpl implements UserTermAgreementService {

    private static final int MAX_USER_AGENT = 500;

    private final UserTermAgreementRepository repository;
    private final TermsConditionRepository termRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Override
    public UserTermAgreementResponseDto create(UserTermAgreementRequestDto dto,
                                               String ipAddress, String userAgent) {
        Long userId = parseId(dto.getUserId(), "userId");
        Long termId = parseId(dto.getTermId(), "termId");

        if (!customerRepository.existsById(userId) && !userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User/Customer with id " + userId + " not found");
        }

        TermsConditionEntity term = termRepository.findById(termId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Terms with id " + termId + " not found"));

        if (!TermsStatus.ACTIVE.name().equals(term.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only ACTIVE terms can be agreed (current: " + term.getStatus() + ")");
        }

        if (repository.existsByUserIdAndTermIdAndStatus(
                String.valueOf(userId), String.valueOf(termId), AgreementStatus.AGREED.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "User " + userId + " already agreed to terms " + termId);
        }

        UserTermAgreementsEntity entity = new UserTermAgreementsEntity();
        entity.setUserId(String.valueOf(userId));
        entity.setTermId(String.valueOf(termId));
        entity.setAgreeAt(new Date());
        entity.setIpAddress(ipAddress);
        entity.setUserAgent(truncate(userAgent, MAX_USER_AGENT));
        entity.setStatus(AgreementStatus.AGREED.name());
        return toDto(repository.save(entity));
    }

    @Override
    public UserTermAgreementResponseDto updateStatus(Long id, UserTermAgreementStatusDto dto) {
        UserTermAgreementsEntity entity = findOrThrow(id);

        AgreementStatus next;
        try {
            next = AgreementStatus.valueOf(dto.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status. Allowed: AGREED, REVOKED");
        }

        AgreementStatus current = currentStatus(entity);
        if (!current.canChangeTo(next)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot change agreement status from " + current + " to " + next
                            + ". Allowed: " + current.allowedNext());
        }

        entity.setStatus(next.name());
        return toDto(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        UserTermAgreementsEntity entity = findOrThrow(id);

        if (currentStatus(entity) != AgreementStatus.REVOKED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only REVOKED agreements can be deleted (current: " + entity.getStatus() + ")");
        }
        repository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserTermAgreementResponseDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public AgreementCheckDto check(String userId, String type) {
        Long uid = parseId(userId, "userId");
        String t = type.trim().toUpperCase();

        TermsConditionEntity term = termRepository
                .findByTypeAndStatus(t, TermsStatus.ACTIVE.name())
                .stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "No ACTIVE terms found for type " + t));

        boolean agreed = repository.existsByUserIdAndTermIdAndStatus(
                String.valueOf(uid), String.valueOf(term.getId()), AgreementStatus.AGREED.name());

        return new AgreementCheckDto(String.valueOf(uid), t,
                String.valueOf(term.getId()), term.getVersion(), agreed);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserTermAgreementResponseDto> search(String userId, String termId,
                                                     String status, Pageable pageable) {
        String u = userId == null ? "" : userId.trim();
        String t = termId == null ? "" : termId.trim();
        String s = status == null ? "" : status.trim().toUpperCase();
        return repository.search(u, t, s, pageable).map(this::toDto);
    }

    // ---------- helpers ----------

    private UserTermAgreementsEntity findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Agreement with id " + id + " not found"));
    }

    private AgreementStatus currentStatus(UserTermAgreementsEntity entity) {
        return AgreementStatus.valueOf(entity.getStatus());
    }

    private Long parseId(String value, String field) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " must be a number");
        }
    }

    private String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }

    private UserTermAgreementResponseDto toDto(UserTermAgreementsEntity e) {
        TermsConditionEntity term = termRepository.findById(parseLongOrNull(e.getTermId()))
                .orElse(null);

        return new UserTermAgreementResponseDto(
                e.getId(), e.getUserId(), e.getTermId(),
                term == null ? null : term.getType(),
                term == null ? null : term.getVersion(),
                e.getAgreeAt(), e.getIpAddress(), e.getUserAgent(), e.getStatus());
    }

    private Long parseLongOrNull(String v) {
        try {
            return v == null ? -1L : Long.parseLong(v.trim());
        } catch (NumberFormatException ex) {
            return -1L;
        }
    }
}