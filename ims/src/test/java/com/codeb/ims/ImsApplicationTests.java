package com.codeb.ims;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
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
	void personalRouteShouldResolveForEmployeeUsers() {
		ClientRepository clientRepository = mock(ClientRepository.class);
		HierarchyRepository hierarchyRepository = mock(HierarchyRepository.class);
		GroupRepository groupRepository = mock(GroupRepository.class);
		InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
		EstimateRepository estimateRepository = mock(EstimateRepository.class);
		UserRepository userRepository = mock(UserRepository.class);
		org.springframework.security.crypto.password.PasswordEncoder passwordEncoder =
				mock(org.springframework.security.crypto.password.PasswordEncoder.class);

		ImsService imsService = new ImsService(
				clientRepository,
				hierarchyRepository,
				groupRepository,
				invoiceRepository,
				estimateRepository,
				userRepository,
				passwordEncoder
		);
		ImsController controller = new ImsController(imsService, mock(AccountService.class));

		when(clientRepository.findAll()).thenReturn(java.util.List.of());
		when(hierarchyRepository.findAll()).thenReturn(java.util.List.of());
		when(invoiceRepository.findAll()).thenReturn(java.util.List.of());
		when(estimateRepository.findAll()).thenReturn(java.util.List.of());
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
	}

}
