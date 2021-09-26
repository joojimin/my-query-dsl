package study.myquerydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static study.myquerydsl.entity.QMember.*;
import static study.myquerydsl.entity.QTeam.*;

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
public class ReturnResultTest {

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
    void fetchTest() {
        // when
        List<Member> members = jpaQueryFactory
            .selectFrom(member)
            .fetch();

        // then
        assertThat(members).isNotEmpty();
        assertThat(members).hasSize(4);
    }

    @Test
    void fetchOneTest() {
        // when
        Team actual = jpaQueryFactory
            .selectFrom(team)
            .where(team.name.eq("teamA"))
            .fetchOne();

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo("teamA");
    }

    @Test
    void fetchFirstTest() {
        // when
        Member actual = jpaQueryFactory
            .selectFrom(member)
            .fetchFirst();

        // then
        assertThat(actual).isNotNull();
    }

    @Test
    void fetchResultsTest() {
        // when
        QueryResults<Member> actual = jpaQueryFactory
            .selectFrom(member)
            .offset(2)
            .limit(2)
            .fetchResults();

        // then
        assertThat(actual.getTotal()).isEqualTo(4);
        assertThat(actual.getResults().size()).isEqualTo(2);
    }

    @Test
    void fetchCountTest() {
        // when
        long count = jpaQueryFactory
            .selectFrom(member)
            .fetchCount();

        // then
        assertThat(count).isEqualTo(4l);
    }
}
