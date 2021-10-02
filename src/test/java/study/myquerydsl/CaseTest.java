package study.myquerydsl;

import static study.myquerydsl.entity.QMember.*;

import com.querydsl.core.types.dsl.CaseBuilder;
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
import study.myquerydsl.entity.QMember;
import study.myquerydsl.entity.Team;

@ActiveProfiles("dev")
@SpringBootTest
@Transactional
public class CaseTest {

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

        Member memberC = new Member("memberC", 40, teamB);
        Member memberD = new Member("memberD", 30, teamB);

        // when
        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);
        em.persist(memberD);
    }

    @Test
    void caseTest() {

        // when
        List<String> actual = jpaQueryFactory
            .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
            .from(member)
            .fetch();

        actual.forEach(System.out::println);
    }

    @Test
    void usingCaseBuilderTest() {
        // when
        List<String> actual = jpaQueryFactory
            .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("10살에서 20살")
                        .when(member.age.between(21, 30)).then("21살에서 30살")
                        .otherwise("기타"))
            .from(member)
            .fetch();

        actual.forEach(System.out::println);
    }
}
