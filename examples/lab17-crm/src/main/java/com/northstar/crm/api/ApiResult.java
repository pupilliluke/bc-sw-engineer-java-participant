package com.northstar.crm.api;

import com.northstar.crm.dto.CustomerResponseDTO;
import com.northstar.crm.exception.ErrorResponse;

/**
 * The facade's return channel. Lab 15 returned a DTO and threw on failure, so
 * every caller needed a try/catch and the demo printed stack traces; a result
 * with two cases makes the failure a value.
 *
 * Sealed, so a switch over Ok and Fail is exhaustive without a default branch.
 */
public sealed interface ApiResult {

    record Ok(CustomerResponseDTO body) implements ApiResult {}

    record Fail(ErrorResponse error) implements ApiResult {}
}
