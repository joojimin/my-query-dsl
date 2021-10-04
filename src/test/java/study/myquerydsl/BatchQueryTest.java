package study.myquerydsl;


import static org.assertj.core.api.Assertions.*;
import static study.myquerydsl.entity.QMember.*;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import study.myquerydsl.entity.Member;
import study.myquerydsl.entity.QMember;
import study.myquerydsl.entity.Team;

@ActiveProfiles("dev")
@SpringBootTest
@Transactional
public class BatchQueryTest {

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

        Member memberC = new Member("memberC", 30, teamB);
        Member memberD = new Member("memberD", 40, teamB);

        // when
        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);
        em.persist(memberD);
    }

    @Test
    @Commit
    void bulkUpdateTest() {

        // given
        // 3 memberA 10
        // 4 memberB 20
        // 5 memberC 30
        // 6 memberD 40


        // when
        long lowCount = jpaQueryFactory
            .update(member)
            .set(member.userName, "비회원")
            .where(member.age.lt(28))
            .execute();

        // then
        assertThat(lowCount).isEqualTo(2);

        // 3 비회원 10
        // 4 비회원 20
        // 5 memberC 30
        // 6 memberD 40

        // when
        List<Member> members = jpaQueryFactory
            .selectFrom(member)
            .fetch();

        members.forEach(System.out::println);
    }

    @Test
    void bulkAddTest() {
        long execute = jpaQueryFactory
            .update(member)
            .set(member.age, member.age.add(1))
            .execute();
    }

    @Test
    void bulkDeleteTest() {
        long execute = jpaQueryFactory
            .delete(member)
            .where(member.age.gt(18))
            .execute();
    }
}
