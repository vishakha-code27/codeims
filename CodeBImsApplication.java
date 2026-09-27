package com.codeb.ims;

import jakarta.persistence.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SpringBootApplication
public class CodeBImsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeBImsApplication.class, args);
    }
}

// ==================== 1. ENTITIES (Database Tables) ====================

@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String role;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

@Entity
@Table(name = "clients")
class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String company;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
}

@Entity
@Table(name = "invoices")
class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String clientName;
    private double amount;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

// ==================== 2. REPOSITORIES ====================

interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}

interface ClientRepository extends JpaRepository<Client, Long> {}

interface InvoiceRepository extends JpaRepository<Invoice, Long> {}

// ==================== 3. SERVICES ====================

@Service
class ImsService {
    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;

    public ImsService(ClientRepository clientRepository, InvoiceRepository invoiceRepository) {
        this.clientRepository = clientRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public List<Client> getAllClients() { return clientRepository.findAll(); }
    public void saveClient(Client client) { clientRepository.save(client); }
    
    public List<Invoice> getAllInvoices() { return invoiceRepository.findAll(); }
    public void saveInvoice(Invoice invoice) { invoiceRepository.save(invoice); }
}

// ==================== 4. CONTROLLER & UI ====================

@Controller
class ImsController {
    private final ImsService imsService;

    public ImsController(ImsService imsService) {
        this.imsService = imsService;
    }

    @GetMapping("/")
    @ResponseBody
    public String renderDashboard() {
        List<Client> clients = imsService.getAllClients();
        List<Invoice> invoices = imsService.getAllInvoices();

        StringBuilder clientRows = new StringBuilder();
        for (Client c : clients) {
            clientRows.append("<tr><td>").append(c.getId()).append("</td><td>").append(c.getName()).append("</td><td>").append(c.getEmail()).append("</td><td>").append(c.getCompany()).append("</td></tr>");
        }

        StringBuilder invoiceRows = new StringBuilder();
        for (Invoice i : invoices) {
            invoiceRows.append("<tr><td>").append(i.getId()).append("</td><td>").append(i.getClientName()).append("</td><td>$").append(i.getAmount()).append("</td><td>").append(i.getStatus()).append("</td></tr>");
        }

        return "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head><meta charset='UTF-8'><title>Code-B IMS</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background: #f4f6f9; margin: 0; padding: 20px; }" +
                ".container { max-width: 1000px; margin: auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }" +
                "h2 { color: #333; border-bottom: 2px solid #007bff; padding-bottom: 5px; }" +
                "table { width: 100%; border-collapse: collapse; margin-top: 10px; margin-bottom: 30px; }" +
                "th, td { padding: 10px; border: 1px solid #ddd; text-align: left; }" +
                "th { background: #007bff; color: white; }" +
                "form { background: #f8f9fa; padding: 15px; border-radius: 5px; margin-bottom: 20px; display: flex; gap: 10px; flex-wrap: wrap; }" +
                "input, select { padding: 8px; flex: 1; min-width: 150px; border: 1px solid #ccc; border-radius: 4px; }" +
                "button { padding: 8px 15px; background: #28a745; color: white; border: none; border-radius: 4px; cursor: pointer; }" +
                "button:hover { background: #218838; }" +
                "</style></head>" +
                "<body>" +
                "<div class='container'>" +
                "<h1>Code-B Internal Management System (IMS)</h1>" +
                
                "<h2>Clients Management</h2>" +
                "<form action='/add-client' method='POST'>" +
                "<input type='text' name='name' placeholder='Client Name' required>" +
                "<input type='email' name='email' placeholder='Email' required>" +
                "<input type='text' name='phone' placeholder='Phone'>" +
                "<input type='text' name='company' placeholder='Company'>" +
                "<button type='submit'>Add Client</button>" +
                "</form>" +
                "<table><tr><th>ID</th><th>Name</th><th>Email</th><th>Company</th></tr>" + clientRows.toString() + "</table>" +

                "<h2>Billing & Invoices</h2>" +
                "<form action='/add-invoice' method='POST'>" +
                "<input type='text' name='clientName' placeholder='Client Name' required>" +
                "<input type='number' step='0.01' name='amount' placeholder='Amount ($)' required>" +
                "<select name='status'><option value='Pending'>Pending</option><option value='Paid'>Paid</option></select>" +
                "<button type='submit'>Add Invoice</button>" +
                "</form>" +
                "<table><tr><th>ID</th><th>Client</th><th>Amount</th><th>Status</th></tr>" + invoiceRows.toString() + "</table>" +
                
                "</div></body></html>";
    }

    @PostMapping("/add-client")
    public String addClient(@RequestParam String name, @RequestParam String email, @RequestParam String phone, @RequestParam String company) {
        Client client = new Client();
        client.setName(name);
        client.setEmail(email);
        client.setPhone(phone);
        client.setCompany(company);
        imsService.saveClient(client);
        return "redirect:/";
    }

    @PostMapping("/add-invoice")
    public String addInvoice(@RequestParam String clientName, @RequestParam double amount, @RequestParam String status) {
        Invoice invoice = new Invoice();
        invoice.setClientName(clientName);
        invoice.setAmount(amount);
        invoice.setStatus(status);
        imsService.saveInvoice(invoice);
        return "redirect:/";
    }
}