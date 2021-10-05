package study.myquerydsl.infrastructure;

import static study.myquerydsl.entity.QMember.*;
import static study.myquerydsl.entity.QTeam.*;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.myquerydsl.dto.MemberSearchCondition;
import study.myquerydsl.dto.MemberTeamDto;
import study.myquerydsl.dto.QMemberTeamDto;
import study.myquerydsl.entity.Member;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory jpaQueryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        jpaQueryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(final Long id) {
        return Optional.ofNullable(em.find(Member.class, id));
    }

    public List<Member> findAll() {
        return em.createQuery("SELECT m FROM Member m", Member.class)
                 .getResultList();
    }

    public List<Member> findAllUsingQueryDSL() {
        return jpaQueryFactory
            .selectFrom(member)
            .fetch();
    }

    public List<Member> findByUsername(final String username) {
        return em.createQuery("SELECT m FROM Member m WHERE m.userName = :username", Member.class)
                 .setParameter("username", username)
                 .getResultList();
    }

    public List<Member> findByUsernameUsingQueryDSL(final String username) {
        return jpaQueryFactory
            .selectFrom(member)
            .where(member.userName.eq(username))
            .fetch();
    }

    public List<MemberTeamDto> searchByBuilder(final MemberSearchCondition condition) {

        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(condition.getUserName())) {
            builder.and(member.userName.eq(condition.getUserName()));
        }

        if (StringUtils.hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }

        if (Objects.nonNull(condition.getAgeGoe())) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }

        if (Objects.nonNull(condition.getAgeLoe())) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return jpaQueryFactory
            .select(new QMemberTeamDto(
                member.id.as("memberId"),
                member.userName,
                member.age,
                team.id.as("teamId"),
                team.name
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(builder)
            .fetch();
    }

    public List<MemberTeamDto> search(final MemberSearchCondition condition) {

        return jpaQueryFactory
            .select(new QMemberTeamDto(
                member.id.as("memberId"),
                member.userName,
                member.age,
                team.id.as("teamId"),
                team.name
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                userNameEq(condition.getUserName()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            )
            .fetch();
    }

    private BooleanExpression userNameEq(String userName) {
        return StringUtils.hasText(userName)? member.userName.eq(userName) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName)? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return Objects.nonNull(ageGoe)? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return Objects.nonNull(ageLoe)? member.age.loe(ageLoe) : null;
    }
}
