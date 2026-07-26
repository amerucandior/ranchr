package com.ranchr.exceptions;

import java.time.Instant;

public record ApiError(int status, String message, Instant timestamp) {

}
