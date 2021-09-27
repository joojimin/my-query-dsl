package study.myquerydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static study.myquerydsl.entity.QMember.*;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import study.myquerydsl.entity.Member;
import study.myquerydsl.entity.Team;

@ActiveProfiles("dev")
@SpringBootTest
@Transactional
public class SortTest {

    @Autowired
    private EntityManager em;

    private JPAQueryFactory jpaQueryFactory;

    @BeforeEach
    void setUp() {
        jpaQueryFactory = new JPAQueryFactory(em);

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
    }

    @Test
    void sortTest() {
        // given
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        // when
        List<Member> members = jpaQueryFactory
            .selectFrom(member)
            .where(member.age.eq(100))
            .orderBy(member.age.desc(),
                     member.userName.asc().nullsLast())
            .fetch();

        // then
        assertThat(members).hasSize(3);
        assertThat(members)
            .extracting("userName")
            .containsSequence("member5", "member6", null);
    }
}
