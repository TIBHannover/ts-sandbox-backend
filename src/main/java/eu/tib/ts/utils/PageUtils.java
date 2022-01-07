package eu.tib.ts.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.NONE)
public class PageUtils {

    public static <T> PageImpl<T> toPage(List<T> list, Pageable pageable) {
        List<T> slice = list.stream()
            .skip((long) pageable.getPageNumber() * pageable.getPageSize())
            .limit(pageable.getPageSize())
            .collect(Collectors.toList());

        return new PageImpl<>(slice, pageable, list.size());
    }
}
