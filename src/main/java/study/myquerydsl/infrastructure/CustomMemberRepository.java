package study.myquerydsl.infrastructure;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.myquerydsl.dto.MemberSearchCondition;
import study.myquerydsl.dto.MemberTeamDto;

public interface CustomMemberRepository {
    List<MemberTeamDto> search(MemberSearchCondition condition);
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
}
