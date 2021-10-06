package study.myquerydsl.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import study.myquerydsl.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long>, CustomMemberRepository {

    List<Member> findByUserName(String userName);
}
