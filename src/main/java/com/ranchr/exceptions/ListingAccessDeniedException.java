package com.ranchr.exceptions;

public class ListingAccessDeniedException extends RuntimeException {
	public ListingAccessDeniedException(String message) {
		super(message);
	}
}
