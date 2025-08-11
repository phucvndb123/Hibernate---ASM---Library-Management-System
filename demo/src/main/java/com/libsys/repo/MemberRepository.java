package com.libsys.repo;

import com.libsys.domain.Member;

public class MemberRepository extends HibernateRepository<Member, Long> {
    public MemberRepository() {
        super(Member.class);
    }
}
