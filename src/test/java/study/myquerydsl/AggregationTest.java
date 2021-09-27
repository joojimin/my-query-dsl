package study.myquerydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static study.myquerydsl.entity.QMember.*;
import static study.myquerydsl.entity.QTeam.*;

import com.querydsl.core.Tuple;
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
public class AggregationTest {

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
    void aggregationTest() {
        // when
        List<Tuple> list = jpaQueryFactory
            .select(
                member.count(),
                member.age.sum(),
                member.age.avg(),
                member.age.max(),
                member.age.min()
            )
            .from(member)
            .fetch();

        // then
        Tuple actual = list.get(0);
        assertThat(actual.get(member.count())).isEqualTo(4);
        assertThat(actual.get(member.age.sum())).isEqualTo(60);
        assertThat(actual.get(member.age.avg())).isEqualTo(15);
        assertThat(actual.get(member.age.max())).isEqualTo(20);
        assertThat(actual.get(member.age.min())).isEqualTo(10);
    }

    @Test
    void groupByTest() {
        // when
        List<Tuple> actual = jpaQueryFactory
            .select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team)
            .groupBy(team.name)
            .having(team.name.eq("teamA"))
            .fetch();

        // then
        Tuple teamA = actual.get(0);
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
    }

}
