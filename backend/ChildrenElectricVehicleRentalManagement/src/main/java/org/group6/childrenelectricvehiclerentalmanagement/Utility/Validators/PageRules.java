package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;

public final class PageRules {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;

    private PageRules() {
    }

    public static void check(Integer page, Integer limit) throws ValidationException {
        if (page != null && page < 1) {
            throw new ValidationException("page", "Trang phải từ 1 trở lên");
        }
        if (limit != null && (limit < 1 || limit > MAX_LIMIT)) {
            throw new ValidationException("limit", "Số dòng mỗi trang phải từ 1 đến " + MAX_LIMIT);
        }
    }

    public static int page(Integer page) {
        return page == null ? DEFAULT_PAGE : page;
    }

    public static int limit(Integer limit) {
        return limit == null ? DEFAULT_LIMIT : limit;
    }
}
