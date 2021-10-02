package study.myquerydsl;

import static org.assertj.core.api.Assertions.*;
import static study.myquerydsl.entity.QMember.*;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import org.assertj.core.api.Assertions;
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
public class ExpressionsTest {

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
    void expressionsTest() {
        List<Tuple> actual = jpaQueryFactory
            .select(member.userName, Expressions.constant("A"))
            .from(member)
            .fetch();

        actual.forEach(System.out::println);
    }

    @Test
    void concatTest() {
        // when
        List<String> actual = jpaQueryFactory
            .select(member.userName.concat("_").concat(member.age.stringValue()))
            .from(member)
            .fetch();

        // then
        actual.forEach(System.out::println);
    }
}
