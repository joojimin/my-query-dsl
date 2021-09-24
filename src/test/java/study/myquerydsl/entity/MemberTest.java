package study.myquerydsl.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("dev")
@SpringBootTest
@Transactional
class MemberTest {

    @Autowired
//    @PersistenceContext
    private EntityManager em;

    @DisplayName("Member Entity 기본 테스트")
    @Test
    void entityTest() {
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        // when
        em.persist(teamA);
        em.persist(teamB);

        // given
        Member memberA = new Member("memberA", 10, teamA);
        Member memberB = new Member("memberB", 20, teamA);

        Member memberC = new Member("memberC", 10, teamB);
        Member memberD = new Member("memberD", 20, teamB);

        // when
        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);
        em.persist(memberD);

        em.flush(); // persistence context flush
        em.clear(); // clear to persistence context

        List<Member> members = em.createQuery("SELECT m FROM Member m", Member.class)
                                 .getResultList();

        members.forEach(member -> {
            System.out.println("Member => " + member);
            System.out.println("Member.team => " + member.getTeam());
        });
    }
}

