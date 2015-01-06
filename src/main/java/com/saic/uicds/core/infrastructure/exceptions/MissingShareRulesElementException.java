package com.saic.uicds.core.infrastructure.exceptions;

@SuppressWarnings("serial")
public class MissingShareRulesElementException extends UICDSException {

    public MissingShareRulesElementException() {
        super(MissingShareRulesElementException.class.getName());
    }

    public MissingShareRulesElementException(String message) {
        super(message);
    }
}
