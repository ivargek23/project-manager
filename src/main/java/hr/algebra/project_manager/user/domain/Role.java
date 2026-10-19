package hr.algebra.project_manager.user.domain;

import java.util.Set;

public enum Role {
    ADMIN,
    PROJECT_LEAD,
    MEMBER;

    public static final Set<Role> CAN_BE_A_PROJECT_LEAD = Set.of(PROJECT_LEAD, ADMIN);
}
