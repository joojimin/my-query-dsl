package study.myquerydsl;

import static org.assertj.core.api.Assertions.*;

import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.myquerydsl.entity.Member;
import study.myquerydsl.entity.QMember;
import study.myquerydsl.entity.Team;

@SpringBootTest
@Transactional
class QueryDslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory jpaQueryFactory;

    @BeforeEach
    void setUp() {

        // field parameter로 설정해도 괜찮다
        // 재사용 가능 ( 동시성 문제 고민 ㄴㄴ, SpringBoot에서 제공해주는 EntityManager 자체가 멀티쓰레드에 문제없음 )
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
    void startJPQL() {
        // given
        String expected = "memberA";
        String qlString = "SELECT m FROM Member m WHERE m.userName = :userName";

        // when
        Member actual = em.createQuery(qlString, Member.class)
                          .setParameter("userName", expected)
                          .getSingleResult();

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.getUserName()).isEqualTo(expected);
    }

    @Test
    void startQueryDsl() {

        QMember m = new QMember("m"); // QMember를 구분하기 위한 variable "m"

        // JPQL과 다르게 문자열을 사용하지 않고 자바 메서드 호출로 쿼리를 만들어냄 ( 오류를 잡아낼 수 있는 타임이 다름 )
        Member actual = jpaQueryFactory
            .select(m)
            .from(m)
            .where(m.userName.eq("memberA")) // eq를 이용하면 JPQL의 파라미터 바인딩을 안해도 JDBC의 prepareStatement
            .fetchOne();

        assertThat(actual).isNotNull();
        assertThat(actual.getUserName()).isEqualTo("memberA");

        /**
         * QueryDSL은 기본적으로 파라미터를 ?로 바인딩
         * ( prepareStatement의 파라미터 바인딩 방식을 사용 )
         * 문자열을 더하기하거나 하는 방식으로 쿼리를 만들게되면 SQL Injection 공격을 받을 수 있음
         */
    }
}
