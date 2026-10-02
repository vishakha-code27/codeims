package com.codeb.ims;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
class ImsApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void personalUserWithNullRoleShouldStillLoadAsEmployee() {
		UserRepository repository = mock(UserRepository.class);
		User user = new User();
		user.setUsername("personal");
		user.setEmail("personal@example.com");
		user.setPassword("encoded");
		user.setRole(null);
		user.setEnabled(true);
		when(repository.findByUsername("personal")).thenReturn(user);

		UserDetails details = new SecurityConfiguration().userDetailsService(repository)
				.loadUserByUsername("personal");

		assertEquals("ROLE_EMPLOYEE", details.getAuthorities().iterator().next().getAuthority());
	}

	@Test
	void estimateCreationLinksChainAndCalculatesTotal() {
		ClientRepository clientRepository = mock(ClientRepository.class);
		HierarchyRepository hierarchyRepository = mock(HierarchyRepository.class);
		ChainRepository chainRepository = mock(ChainRepository.class);
		GroupRepository groupRepository = mock(GroupRepository.class);
		InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
		EstimateRepository estimateRepository = mock(EstimateRepository.class);
		SalesEstimateRepository salesEstimateRepository = mock(SalesEstimateRepository.class);
		UserRepository userRepository = mock(UserRepository.class);
		org.springframework.security.crypto.password.PasswordEncoder passwordEncoder =
				mock(org.springframework.security.crypto.password.PasswordEncoder.class);

		Chain chain = new Chain();
		chain.setGroupName("Retail Group");
		chain.setChainName("North Chain");
		chain.setBrandName("Code-B");
		chain.setZoneName("North Zone");
		when(chainRepository.findById(42)).thenReturn(java.util.Optional.of(chain));

		ImsService imsService = new ImsService(clientRepository, hierarchyRepository, chainRepository,
				groupRepository, invoiceRepository, estimateRepository, salesEstimateRepository,
				userRepository, passwordEncoder, mock(org.springframework.mail.javamail.JavaMailSender.class), "noreply@example.test");
		ImsController controller = new ImsController(imsService, mock(AccountService.class));

		assertEquals("redirect:/", controller.addEstimate(42, "Onboarding", 3, 125.50,
				java.time.LocalDate.of(2026, 11, 15), "Remote delivery"));

		org.mockito.ArgumentCaptor<SalesEstimate> estimateCaptor =
				org.mockito.ArgumentCaptor.forClass(SalesEstimate.class);
		verify(salesEstimateRepository).save(estimateCaptor.capture());
		SalesEstimate estimate = estimateCaptor.getValue();
		assertSame(chain, estimate.getChain());
		assertEquals("Retail Group", estimate.getGroupName());
		assertEquals("Code-B", estimate.getBrandName());
		assertEquals(376.50, estimate.getTotalCost());
		assertEquals("Draft", estimate.getStatus());
	}

	@Test
	void estimateInvoiceCopiesEstimateDetailsAndRecordsFullPayment() {
		InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
		SalesEstimateRepository salesEstimateRepository = mock(SalesEstimateRepository.class);
		SalesEstimate estimate = new SalesEstimate();
		Chain chain = new Chain();
		chain.setChainName("North Chain");
		chain.setBrandName("Code-B");
		estimate.setChain(chain);
		estimate.setService("Onboarding");
		estimate.setQty(3);
		estimate.setCostPerUnit(125.50);
		estimate.setTotalCost(376.50);
		estimate.setDeliveryDate(java.time.LocalDate.of(2026, 11, 15));
		estimate.setDeliveryDetails("Remote delivery");
		when(invoiceRepository.existsByInvoiceNo(4321)).thenReturn(false);
		when(salesEstimateRepository.findById(12L)).thenReturn(java.util.Optional.of(estimate));
		when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ImsService imsService = new ImsService(
				mock(ClientRepository.class), mock(HierarchyRepository.class), mock(ChainRepository.class),
				mock(GroupRepository.class), invoiceRepository, mock(EstimateRepository.class), salesEstimateRepository,
				mock(UserRepository.class), mock(org.springframework.security.crypto.password.PasswordEncoder.class),
				mock(org.springframework.mail.javamail.JavaMailSender.class), "noreply@example.test");

		Invoice invoice = imsService.createEstimateInvoice(12L, 4321, "billing@example.test");

		assertEquals(4321, invoice.getInvoiceNo());
		assertSame(estimate, invoice.getEstimate());
		assertSame(chain, invoice.getChain());
		assertEquals("Onboarding", invoice.getServiceDetails());
		assertEquals(3, invoice.getQty());
		assertEquals(125.50, invoice.getCostPerQty());
		assertEquals(376.50, invoice.getAmountPayable());
		assertEquals(0.0, invoice.getBalance());
		assertEquals("billing@example.test", invoice.getEmailId());
		assertEquals("Paid", invoice.getStatus());
	}

	@Test
	void estimateInvoicePdfHasPdfSignature() {
		InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
		SalesEstimate estimate = new SalesEstimate();
		Chain chain = new Chain();
		chain.setChainName("North Chain");
		Invoice invoice = new Invoice();
		invoice.setInvoiceNo(1234);
		invoice.setEstimate(estimate);
		invoice.setChain(chain);
		invoice.setServiceDetails("Onboarding");
		invoice.setQty(1);
		invoice.setCostPerQty(100.0);
		invoice.setAmountPayable(100.0);
		invoice.setBalance(0.0);
		when(invoiceRepository.findById(9L)).thenReturn(java.util.Optional.of(invoice));
		ImsService imsService = new ImsService(
				mock(ClientRepository.class), mock(HierarchyRepository.class), mock(ChainRepository.class),
				mock(GroupRepository.class), invoiceRepository, mock(EstimateRepository.class),
				mock(SalesEstimateRepository.class), mock(UserRepository.class),
				mock(org.springframework.security.crypto.password.PasswordEncoder.class),
				mock(org.springframework.mail.javamail.JavaMailSender.class), "noreply@example.test");

		byte[] pdf = imsService.createInvoicePdf(9L);

		assertEquals("%PDF", new String(pdf, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));
	}

	@Test
	void personalRouteShouldResolveForEmployeeUsers() {
		ClientRepository clientRepository = mock(ClientRepository.class);
		HierarchyRepository hierarchyRepository = mock(HierarchyRepository.class);
		ChainRepository chainRepository = mock(ChainRepository.class);
		GroupRepository groupRepository = mock(GroupRepository.class);
		InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
		EstimateRepository estimateRepository = mock(EstimateRepository.class);
		SalesEstimateRepository salesEstimateRepository = mock(SalesEstimateRepository.class);
		UserRepository userRepository = mock(UserRepository.class);
		org.springframework.security.crypto.password.PasswordEncoder passwordEncoder =
				mock(org.springframework.security.crypto.password.PasswordEncoder.class);

		ImsService imsService = new ImsService(
				clientRepository,
				hierarchyRepository,
				chainRepository,
				groupRepository,
				invoiceRepository,
				estimateRepository,
				salesEstimateRepository,
				userRepository,
				passwordEncoder,
				mock(org.springframework.mail.javamail.JavaMailSender.class),
				"noreply@example.test"
		);
		ImsController controller = new ImsController(imsService, mock(AccountService.class));

		when(clientRepository.findAll()).thenReturn(java.util.List.of());
		when(hierarchyRepository.findAll()).thenReturn(java.util.List.of());
		when(chainRepository.findAll()).thenReturn(java.util.List.of());
		when(invoiceRepository.findAll()).thenReturn(java.util.List.of());
		when(salesEstimateRepository.findAll()).thenReturn(java.util.List.of());
		when(userRepository.findAll()).thenReturn(java.util.List.of());
		when(userRepository.countByEnabledTrue()).thenReturn(0L);

		TestingAuthenticationToken authentication = new TestingAuthenticationToken("personal", "pw", "ROLE_EMPLOYEE");
		CsrfToken csrfToken = new CsrfToken() {
			@Override public String getHeaderName() { return "X-CSRF-TOKEN"; }
			@Override public String getParameterName() { return "_csrf"; }
			@Override public String getToken() { return "test-token"; }
		};

		String html = controller.renderDashboard(authentication, csrfToken);
		assertTrue(html.contains("Signed in as personal") && html.contains("Employee"));
		assertTrue(html.contains("Estimate management") && html.contains("name='chainId'")
				&& html.contains("name='deliveryDate'") && html.contains("Estimated value"));
		assertTrue(html.contains("Manage invoices") && html.contains("invoice-search"));
	}

}
