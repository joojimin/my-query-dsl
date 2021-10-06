package study.myquerydsl.infrastructure;

import static study.myquerydsl.entity.QMember.member;
import static study.myquerydsl.entity.QTeam.team;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;
import study.myquerydsl.dto.MemberSearchCondition;
import study.myquerydsl.dto.MemberTeamDto;
import study.myquerydsl.dto.QMemberTeamDto;

public class MemberRepositoryImpl implements CustomMemberRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public MemberRepositoryImpl(EntityManager entityManager) {
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
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
        return StringUtils.hasText(userName) ? member.userName.eq(userName) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return Objects.nonNull(ageGoe) ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return Objects.nonNull(ageLoe) ? member.age.loe(ageLoe) : null;
    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition,
                                                Pageable pageable) {
        QueryResults<MemberTeamDto> queryResults = jpaQueryFactory
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
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetchResults();

        return new PageImpl<>(queryResults.getResults(),
                              pageable,
                              queryResults.getTotal());
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition,
                                                 Pageable pageable) {

        // get TotalCount
        JPAQuery<MemberTeamDto> totalCountQuery = jpaQueryFactory
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
            );

        // get Contents
        List<MemberTeamDto> contents = jpaQueryFactory
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
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return PageableExecutionUtils.getPage(contents,
                                              pageable,
                                              () -> totalCountQuery.fetchCount());
    }
}
