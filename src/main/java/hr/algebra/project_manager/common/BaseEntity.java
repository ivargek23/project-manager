package hr.algebra.project_manager.common;

import org.hibernate.Hibernate;

// Shared equals and hashCode for all entities
// Condition of equality: two entities are equal when they are of the same class and have same id
// a new entity, while its id is still null, is only equal to itself
public abstract class BaseEntity {

    public abstract Long getId();

    @Override
    public boolean equals(Object o) {
        boolean result = false;

        if (this == o) result = true;

        else if (o instanceof BaseEntity other && Hibernate.getClass(this) == Hibernate.getClass(other))
            result = (getId() != null) && getId().equals(other.getId());

        return result;
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
