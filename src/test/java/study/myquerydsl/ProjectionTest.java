package study.myquerydsl;


import static study.myquerydsl.entity.QMember.*;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import study.myquerydsl.dto.MemberDto;
import study.myquerydsl.dto.QMemberDto;
import study.myquerydsl.dto.UserDto;
import study.myquerydsl.entity.Member;
import study.myquerydsl.entity.QMember;
import study.myquerydsl.entity.Team;

@ActiveProfiles("dev")
@SpringBootTest
@Transactional
public class ProjectionTest {

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
    void simpleProjectionTest() {

        // when
        List<String> actual = jpaQueryFactory
            .select(member.userName)
            .from(member)
            .fetch();

        // then
        actual.forEach(System.out::println);
    }

    @Test
    void tupleTest() {
        // when
        List<Tuple> actual = jpaQueryFactory
            .select(member.userName, member.age)
            .from(member)
            .fetch();

        // then
        actual.forEach(el -> {
            String userName = el.get(member.userName);
            Integer age = el.get(member.age);
            System.out.println("userName => " + userName + ", age => " + age);
        });
    }

    @Test
    void findDtoBySetterTest() {

        // when
        List<MemberDto> actual = jpaQueryFactory
            .select(Projections.bean(MemberDto.class,
                                     member.userName,
                                     member.age))
            .from(member)
            .fetch();

        actual.forEach(System.out::println);
    }

    @Test
    void findDtoByFieldsTest() {

        // when
        List<MemberDto> actual = jpaQueryFactory
            .select(Projections.fields(MemberDto.class,
                                       member.userName,
                                       member.age))
            .from(member)
            .fetch();

        actual.forEach(System.out::println);
    }


    @Test
    void findDtoByConstructorTest() {

        // when
        List<MemberDto> actual = jpaQueryFactory
            .select(Projections.constructor(MemberDto.class,
                                            member.userName,
                                            member.age))
            .from(member)
            .fetch();

        actual.forEach(System.out::println);
    }


    @Test
    void findUserDtoTest() {

        // when
        List<UserDto> actual = jpaQueryFactory
            .select(Projections.fields(UserDto.class,
                                       member.userName.as("name"),
                                       member.age.as("fakeAge")))
            .from(member)
            .fetch();

        actual.forEach(System.out::println);
    }

    @Test
    void usingSubQueryTest() {
        QMember memberSub = new QMember("memberSub");

        List<UserDto> actual = jpaQueryFactory
            .select(Projections.fields(UserDto.class,
                                       member.userName.as("name"),
                                       ExpressionUtils.as(jpaQueryFactory
                                                              .select(memberSub.age.max())
                                                              .from(memberSub), "fakeAge")))
            .from(member)
            .fetch();

        actual.forEach(System.out::println);
    }

    @Test
    void findDtoByQueryProjectionTest() {
        List<MemberDto> actual = jpaQueryFactory
            .select(new QMemberDto(member.userName, member.age))
            .from(member)
            .fetch();

        actual.forEach(System.out::println);
    }
}
