package com.northstar.crm.api;

import com.northstar.crm.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
  private final CustomerService customerService;

  public CustomerController(CustomerService customerService) {
    this.customerService = customerService;
  }


  // Sort matches ix_customer_status_created (status, created_at DESC,
  // customer_id DESC) so the page is an index scan rather than a sort, and is
  // fixed here rather than read from the query string.
  @GetMapping("/page")
  public Page<CustomerResponse> page(
      @RequestParam(defaultValue = "ACTIVE") String status,
      @RequestParam(name = "page", defaultValue = "0") int number,
      @RequestParam(name = "size", defaultValue = "20") int size) {
    int safeSize = Math.min(Math.max(size, 1), 100);
    Pageable page = PageRequest.of(
        number,
        safeSize,
        Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
    return customerService.listByStatus(status, page);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CustomerResponse create(
      @RequestBody CreateCustomerRequest request,
      @RequestHeader(value = "X-Correlation-Id", defaultValue = "lab-request-001") String correlationId) {
    return customerService.create(request, correlationId);
  }

  @GetMapping("/{id}")
  public CustomerResponse get(@PathVariable String id) {
    return customerService.get(id);
  }

  @GetMapping
  public List<CustomerResponse> list() {
    return customerService.list();
  }

  @PutMapping("/{id}")
  public CustomerResponse update(
      @PathVariable String id,
      @RequestBody CreateCustomerRequest request,
      @RequestHeader(value = "X-Correlation-Id", defaultValue = "lab-request-001") String correlationId) {
    return customerService.update(id, request, correlationId);
  }

  @PatchMapping("/{id}/status")
  public CustomerResponse updateStatus(
      @PathVariable String id,
      @RequestBody StatusUpdateRequest request,
      @RequestHeader(value = "X-Correlation-Id", defaultValue = "lab-request-001") String correlationId) {
    return customerService.updateStatus(id, request.getStatus(), correlationId);
  }
}
