package study.myquerydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static study.myquerydsl.entity.QMember.*;

import com.querydsl.core.QueryResults;
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
public class PagingTest {

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
    void pagingTest() {
        // when
        long totalCount = jpaQueryFactory
            .selectFrom(member)
            .fetchCount();

        List<Member> members = jpaQueryFactory
            .selectFrom(member)
            .orderBy(member.userName.desc())
            .offset(1) // 얼마나 스킵할 것인지
            .limit(2) // 몇개나 가져올 것인지
            .fetch();

        // then
        assertThat(totalCount).isEqualTo(4);
        assertThat(members).hasSize(2);
        assertThat(members)
            .extracting("userName")
            .containsSequence("memberC", "memberB");
    }

    @Test
    void pagingTestWithFetchResults() {
        // when
        QueryResults<Member> queryResults = jpaQueryFactory
            .selectFrom(member)
            .orderBy(member.userName.desc())
            .offset(1)
            .limit(2)
            .fetchResults();

        // then
        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getResults())
            .hasSize(2)
            .extracting("userName")
            .containsSequence("memberC", "memberB");
    }
}
