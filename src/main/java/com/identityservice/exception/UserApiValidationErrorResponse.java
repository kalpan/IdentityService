package com.identityservice.exception;

import java.io.Serializable;

public class UserApiValidationErrorResponse implements Serializable {

	private static final long serialVersionUID = -5755277385377840797L;

	private String errorMessage;
	private String requestDescription;

	public UserApiValidationErrorResponse() {
	}

	public UserApiValidationErrorResponse(String errorMessage, String requestDescription) {
		this.errorMessage = errorMessage;
		this.requestDescription = requestDescription;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getRequestDescription() {
		return requestDescription;
	}

	public void setRequestDescription(String requestDescription) {
		this.requestDescription = requestDescription;
	}
}