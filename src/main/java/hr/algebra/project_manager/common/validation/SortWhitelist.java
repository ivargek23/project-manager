package hr.algebra.project_manager.common.validation;

import hr.algebra.project_manager.common.exception.InvalidSortException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class SortWhitelist {

    private SortWhitelist() {
    }

    public static Pageable restrict(Pageable pageable, Set<String> allowed) {
        for (Sort.Order order : pageable.getSort()) {
            if (!allowed.contains(order.getProperty())) {
                throw new InvalidSortException(order.getProperty(), allowed);
            }
        }
        return pageable;
    }
}
