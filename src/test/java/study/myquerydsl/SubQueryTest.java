package study.myquerydsl;

import static org.assertj.core.api.Assertions.*;
import static study.myquerydsl.entity.QMember.*;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import org.assertj.core.groups.Tuple;
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
public class SubQueryTest {

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
        Member memberB = new Member("memberB", 30, teamA);

        Member memberC = new Member("memberC", 30, teamB);
        Member memberD = new Member("memberD", 20, teamB);

        // when
        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);
        em.persist(memberD);
    }


    @Test
    void subQueryTest() {
        // given
        // 서브쿼리에서 쓸 Q-Type과 본 쿼리 Q-Type이 겹치면 안된다 ( static Q-Type 쓰면 alias가 같음 )
        // 서로의 alias가 달라야한다
        QMember memberSub = new QMember("memberSub");

        // when
        List<Member> actual = jpaQueryFactory
            .selectFrom(member)
            .where(member.age.eq(
                JPAExpressions
                    .select(memberSub.age.max())
                    .from(memberSub)
            ))
            .fetch();

        // then
        assertThat(actual)
            .hasSize(2)
            .extracting("userName", "age")
            .containsExactly(Tuple.tuple("memberB", 30), Tuple.tuple("memberC", 30));
    }


    @Test
    void selectSubQueryTest() {
        // given
        QMember memberSub = new QMember("memberSub");

        // when
        List<com.querydsl.core.Tuple> tuples = jpaQueryFactory
            .select(member.userName,
                    JPAExpressions
                        .select(memberSub.age.avg())
                        .from(memberSub))
            .from(member)
            .fetch();

        // then
        tuples.forEach(System.out::println);
    }
}
