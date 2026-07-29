// Experiment 4 transcript. Prompt sent to Claude Code (not Copilot):
//   "build the entire CRM service layer"
// No other context given. Everything below arrived in one response. It is a
// transcript, not code to use: seven classes in one shot, separated by
// "file:" comments because Chat proposed them as separate files. Never
// compiled, never copied into src/. See ai-review-notes.md experiment 4.

// file: com/northstar/crm/repository/CustomerRepository.java
// interface CustomerRepository {
//     Customer save(Customer customer);
//     Optional<Customer> findById(String id);
//     List<Customer> findAll();
//     List<Customer> findByStatus(CustomerStatus status);
//     boolean deleteById(String id);
//     boolean existsById(String id);
// }

// file: com/northstar/crm/repository/InMemoryCustomerRepository.java
// class InMemoryCustomerRepository implements CustomerRepository {
//     private final Map<String, Customer> store = new ConcurrentHashMap<>();
//     ... save/findById/findAll/findByStatus/deleteById/existsById over the map
// }

// file: com/northstar/crm/exception/CustomerNotFoundException.java
// class CustomerNotFoundException extends RuntimeException { ... }

// file: com/northstar/crm/exception/DuplicateCustomerException.java
// class DuplicateCustomerException extends RuntimeException { ... }

// file: com/northstar/crm/service/CustomerIdGenerator.java
// class CustomerIdGenerator {
//     private final AtomicLong counter = new AtomicLong(1000);
//     String nextId() { return "CUS-" + counter.incrementAndGet(); }
// }

// file: com/northstar/crm/service/CustomerValidator.java
// class CustomerValidator {
//     private static final Pattern EMAIL = Pattern.compile("^[^@]+@[^@]+$");
//     void validate(Customer c) { ... blank name, email format, phone format }
// }

// file: com/northstar/crm/service/CustomerService.java
// class CustomerService {
//     private final CustomerRepository repository;
//     private final CustomerValidator validator;
//     private final CustomerIdGenerator idGenerator;
//     Customer createCustomer(String fullName, String email, String phone) {
//         ... validates, generates id, defaults PROSPECT, saves
//     }
//     Customer getCustomer(String id) { ... throws CustomerNotFoundException }
//     List<Customer> listCustomers() { ... }
//     List<Customer> listByStatus(CustomerStatus status) { ... }
//     Customer activate(String id) { ... }
//     Customer suspend(String id) { ... }
//     Customer close(String id) { ... }
//     boolean deleteCustomer(String id) { ... }
// }
