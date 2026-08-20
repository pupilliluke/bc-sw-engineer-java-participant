package com.northstar.crm.api;

/**
 * Step 9 request DTO. A record, so request.email() is the accessor the guide's
 * stub calls. publicId is supplied by the caller because it is the business key
 * the API has spoken since lab 34; customer_id is the database's to assign.
 */
public record CreateCustomerRequest(
    String publicId,
    String name,
    String email,
    String status) {
}
