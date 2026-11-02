package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;

/** Mỗi DTO request có đúng một validator tương ứng, kiểm tra MỌI field của DTO đó. */
public interface RequestValidator<T> {
    void validate(T request) throws ValidationException;
}
