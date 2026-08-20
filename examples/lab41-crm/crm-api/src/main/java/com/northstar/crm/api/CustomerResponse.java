package com.northstar.crm.api;

/**
 * Step 9 response DTO. Carries publicId as id and full_name as name, the shape
 * the UI has used since lab 34. The surrogate customer_id and the @Version
 * counter stay inside the entity and never reach the API.
 */
public record CustomerResponse(
    String id,
    String name,
    String email,
    String status) {
}
