package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class PackageRuleViolation extends AbstractException {
    public PackageRuleViolation(String userMessage) {
        super(userMessage, "PACKAGE_RULE_VIOLATION", userMessage, 409);
    }
}
