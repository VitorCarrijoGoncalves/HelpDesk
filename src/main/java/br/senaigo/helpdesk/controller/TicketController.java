package br.senaigo.helpdesk.controller;

import java.util.Date;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.senaigo.helpdesk.entity.Ticket;
import br.senaigo.helpdesk.entity.User;
import br.senaigo.helpdesk.enums.StatusEnum;
import br.senaigo.helpdesk.response.Response;
import br.senaigo.helpdesk.security.jwt.JwtTokenUtil;
import br.senaigo.helpdesk.service.TicketService;
import br.senaigo.helpdesk.service.UserService;

@RestController
@RequestMapping("/api/ticket")
@CrossOrigin(origins="*")
public class TicketController {
	
	@Autowired
	private TicketService ticketService;
	
	@Autowired
	protected JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	private UserService userService;
	
	@PostMapping()
	@PreAuthorize("hasAnyRole('CUSTOMER')")
	public ResponseEntity<Response<Ticket>> createOrUpdate(HttpServletRequest request, 
			@RequestBody Ticket ticket, BindingResult result) {
		Response<Ticket> response = new Response<>();
		
		try {
			validateCreateTicket(ticket, result);
			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}
			ticket.setStatus(StatusEnum.getStatus("New"));
			ticket.setUser(userFromRequest(request));
			ticket.setDate(new Date());
			ticket.setNumber(generateNumber());		
			Ticket ticketPersisted = (Ticket) ticketService.createOrUpdate(ticket);
			response.setData(ticketPersisted);
			} catch (Exception e) {
			
			response.getErrors().add(e.getMessage());
			return ResponseEntity.badRequest().body(response);	
			
		}
		
		return ResponseEntity.ok(response);
		
	}
	
	private void validateCreateTicket(Ticket ticket, BindingResult result) {
		
		if (ticket.getTitle() == null) {
			result.addError(new ObjectError("Ticket", "Title no information"));
			return;
		}
		
	}
	
	public User userFromRequest(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		String email = jwtTokenUtil.getUsernameFromToken(token);
		return userService.findByEmail(email);
	}
	
	private Integer generateNumber() {
		Random random = new Random();
		return random.nextInt(9999);
	}
	
	@PutMapping
	@PreAuthorize("hasAnyRole('CUSTOMER')")
	public ResponseEntity<Response<Ticket>> update(HttpServletRequest request, 
			@RequestBody Ticket ticket, BindingResult result) {
		Response<Ticket> response = new Response<>();
		
		try {
			
			validateUpdateTicket(ticket, result);
			
			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}
			
			Ticket ticketCurrent = ticketService.findById(ticket.getId());
			ticket.setStatus(ticketCurrent.getStatus());
			
		} catch (Exception e) {
			response.getErrors().add(e.getMessage());
			return ResponseEntity.badRequest().body(response);	
			// TODO: handle exception
		}
		
		
		
	}
	
	private void validateUpdateTicket(Ticket ticket, BindingResult result) {
		
		if (ticket.getId() == null) {
			result.addError(new ObjectError("Ticket", "Id no information"));
			return;
		}
		if (ticket.getTitle() == null) {
			result.addError(new ObjectError("Ticket", "Title no information"));
			return;
		}
		
	}
	
	
	

}
