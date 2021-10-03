package study.myquerydsl;

import static org.assertj.core.api.Assertions.*;
import static study.myquerydsl.entity.QMember.*;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
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
public class DynamicTest {

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
    void booleanBuilderTest() {
        String usernameParam = "memberA";
        Integer ageParam = 10;

        List<Member> result = selectMember1(usernameParam, ageParam);
        assertThat(result)
            .hasSize(1);
    }

    private List<Member> selectMember1(String usernameParam, Integer ageParam) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (Objects.nonNull(usernameParam)) {
            booleanBuilder.and(member.userName.eq(usernameParam));
        }

        if (Objects.nonNull(ageParam)) {
            booleanBuilder.and(member.age.eq(ageParam));
        }

        return jpaQueryFactory
            .selectFrom(member)
            .where(booleanBuilder)
            .fetch();
    }

    @Test
    void whereTest() {
        String usernameParam = "memberA";
        Integer ageParam = null;

        List<Member> result = selectMember2(usernameParam, ageParam);
        assertThat(result).hasSize(1);
    }

    private List<Member> selectMember2(String usernameParam, Integer ageParam) {
        return jpaQueryFactory
            .selectFrom(member)
            .where(usernameEq(usernameParam), ageEq(ageParam)) // 기본적으로 and조건, null은 무시
            .fetch();
    }

    private BooleanExpression usernameEq(String usernameParam) {
        if (Objects.isNull(usernameParam)) {
            return null;
        }
        return member.userName.eq(usernameParam);
    }

    private BooleanExpression ageEq(Integer ageParam) {
        if (Objects.isNull(ageParam)) {
            return null;
        }
        return member.age.eq(ageParam);
    }
}
