package eu.tib.ts.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;

import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.NONE)
public class PageUtils {

    public static <T> PageImpl<T> toPage(List<T> list, Pageable pageable) {
        List<T> slice = list.stream()
                .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                .limit(pageable.getPageSize())
                .toList();

        return new PageImpl<>(slice, pageable, list.size());
    }

    public static <T, M extends RepresentationModel<?>> PagedModel<M> toPagedModel(
            Page<T> page,
            Class<M> resourceType,
            PagedResourcesAssembler<T> pagedResourcesAssembler,
            RepresentationModelAssembler<T, M> modelAssembler
    ) {

        return page.isEmpty()
                ? (PagedModel<M>) pagedResourcesAssembler.toEmptyModel(page, resourceType)
                : pagedResourcesAssembler.toModel(page, modelAssembler);
    }
}