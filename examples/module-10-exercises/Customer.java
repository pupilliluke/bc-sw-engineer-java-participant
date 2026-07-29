// correlation note: lab-request-001
// domain fixture example: CUS-1001 — Amina Khan — ACTIVE
// plain Java 21 only: no Spring, no JPA, no annotations
//WRITTEN WITH CLAUDE
public record Customer(String id, String fullName, String status) {

    public Customer {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName must not be blank");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status must not be blank");
        }
    }

    public static void main(String[] args) {
        Customer customer = new Customer("CUS-1001", "Amina Khan", "ACTIVE");
        System.out.println(customer.id());
        System.out.println(customer.fullName());
        System.out.println(customer.status());
        System.out.println(customer);
    }
}
