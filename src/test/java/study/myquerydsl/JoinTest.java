package study.myquerydsl;

import static org.assertj.core.api.Assertions.*;
import static study.myquerydsl.entity.QMember.*;
import static study.myquerydsl.entity.QTeam.*;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import org.assertj.core.groups.Tuple;
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
public class JoinTest {

    @Autowired
    private EntityManager em;

    private JPAQueryFactory jpaQueryFactory;

    @PersistenceUnit
    private EntityManagerFactory emf;

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
    void joinTest() {
        // when
        List<Member> members = jpaQueryFactory
            .selectFrom(member)
            .join(member.team, team)
            .where(team.name.eq("teamA"))
            .fetch();

        // then
        assertThat(members)
            .hasSize(2)
            .extracting("userName", "age") // 대소문자 구분 조심
            .containsExactly(Tuple.tuple("memberA", 10), Tuple.tuple("memberB", 20));

        assertThat(members)
            .extracting("team")
            .extracting("name")
            .containsOnly("teamA");
    }

    @Test
    void thetaJoinTest() {
        // given
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        em.flush();
        em.clear();

        // when
        List<Member> actual = jpaQueryFactory
            .select(member)
            .from(member, team)
            .where(member.userName.eq(team.name))
            .fetch();

        // then
        assertThat(actual)
            .hasSize(2)
            .extracting("userName")
            .containsExactly("teamA", "teamB");
    }

    @Test
    void joinOnFiltering() {
        // when
        List<com.querydsl.core.Tuple> actual = jpaQueryFactory
            .select(member, team)
            .from(member)
            .leftJoin(member.team, team)
            .on(team.name.eq("teamA")) // join해서 가져올지 말지 필터링
            .fetch();

        // then
        assertThat(actual).hasSize(4);
        List<Team> teams = actual.stream()
                                 .map(tuple -> tuple.get(team))
                                 .filter(Objects::nonNull)
                                 .collect(Collectors.toList());

        assertThat(teams)
            .hasSize(2)
            .extracting("name")
            .containsOnly("teamA");
    }


    @Test
    void joinWithoutRelationTest() {
        // given
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        em.flush();
        em.clear();

        // when
        List<com.querydsl.core.Tuple> actual = jpaQueryFactory
            .select(member, team)
            .from(member)
            .leftJoin(team) // 연관관계 조인이랑 문법이 다르다. member.team이 아니라 team이다
            .on(member.userName.eq(team.name))
            .fetch();

        // then
        List<Team> actualTeams = actual.stream()
                                       .map(innerActual -> innerActual.get(team))
                                       .filter(Objects::nonNull)
                                       .collect(Collectors.toList());

        assertThat(actualTeams)
            .hasSize(2)
            .extracting("name")
            .containsExactly("teamA", "teamB");
    }

    @Test
    void fetchJoinNoTest() {
        // given
        em.flush();
        em.clear();

        // when
        Member findMember = jpaQueryFactory
            .selectFrom(member)
            .where(member.userName.eq("memberA"))
            .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded)
            .as("페치 조인 미적용")
            .isFalse();
    }

    @Test
    void fetchJoinUseTest() {
        // given
        em.flush();
        em.clear();

        // when
        Member findMember = jpaQueryFactory
            .selectFrom(member)
            .join(member.team, team)
            .fetchJoin()
            .where(member.userName.eq("memberA"))
            .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded)
            .as("페치 조인 미적용")
            .isTrue();
    }
}
