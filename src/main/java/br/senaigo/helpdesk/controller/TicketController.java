package br.senaigo.helpdesk.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.senaigo.helpdesk.entity.ChangeStatus;
import br.senaigo.helpdesk.entity.Ticket;
import br.senaigo.helpdesk.entity.User;
import br.senaigo.helpdesk.enums.ProfileEnum;
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
			ticket.setUser(ticketCurrent.getUser());
			ticket.setDate(ticketCurrent.getDate());
			ticket.setNumber(ticketCurrent.getNumber());
			
			if (ticketCurrent.getAssignedUser() != null) {
				ticket.setAssignedUser(ticketCurrent.getAssignedUser());
			}
			
			Ticket ticketPersisted = (Ticket) ticketService.createOrUpdate(ticket);
			response.setData(ticketPersisted);
			
			
		} catch (Exception e) {
			response.getErrors().add(e.getMessage());
			return ResponseEntity.badRequest().body(response);	
			// TODO: handle exception
		}
		
		return ResponseEntity.ok(response);
		
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
	
	@GetMapping(value="{id}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
	public ResponseEntity<Response<Ticket>> findById(@PathVariable("id") String id) {
		Response<Ticket> response = new Response<>();
		Ticket ticket = ticketService.findById(id);
		
		if (ticket == null) {
			response.getErrors().add("Register not found id: " + id);
			return ResponseEntity.badRequest().body(response);
		}
		
		List<ChangeStatus> changes = new ArrayList<>();
		Iterable<ChangeStatus> changesCurrent = ticketService.listChangeStatus(ticket.getId());
		for (Iterator<ChangeStatus> iterator = changesCurrent.iterator(); iterator.hasNext();) {
			ChangeStatus changeStatus = (ChangeStatus) iterator.next();
			changeStatus.setTicket(null);
			changes.add(changeStatus);
			
		}
		
		ticket.setChanges(changes);
		response.setData(ticket);
		return ResponseEntity.ok(response);
		
	}
	
	@DeleteMapping(value="{id}")
	@PreAuthorize("hasAnyRole('CUSTOMER')")
	public ResponseEntity<Response<String>> delete(@PathVariable("id") String id) {
		
		Response<String> response = new Response<>();
		Ticket ticket = ticketService.findById(id);
		
		if (ticket == null) {
			response.getErrors().add("Register not found id: " + id);
			return ResponseEntity.badRequest().body(response);
		}
		
		ticketService.delete(id);
		return ResponseEntity.ok(new Response<String>());
		
	}
	
	
	@GetMapping(value="{page}/{count}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
	public ResponseEntity<Response<Page<Ticket>>> findAll(HttpServletRequest request, @PathVariable("page") int page, @PathVariable("count") int count) {
		
		Response<Page<Ticket>> response = new Response<>();
		
		Page<Ticket> tickets = null;
		User userRequest = userFromRequest(request);
		if (userRequest.getProfile().equals(ProfileEnum.ROLE_TECHNICIAN)) {
			tickets = ticketService.listTicket(page, count);
		} else if (userRequest.getProfile().equals(ProfileEnum.ROLE_CUSTOMER)) {
			tickets = ticketService.findByCurrentUser(page, count, userRequest.getId());
		}
		
		response.setData(tickets);
		return ResponseEntity.ok(response);
		
	}
	
	@GetMapping(value="{page}/{count}/{number}/{title}/{status}/{priority}/{assigned}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
	public ResponseEntity<Response<Page<Ticket>>> findByParameters(HttpServletRequest request, 
			@PathVariable("page") int page, 
			@PathVariable("count") int count,
			@PathVariable("number") Integer number,
			@PathVariable("title") String title,
			@PathVariable("status") String status,
			@PathVariable("priority") String priority,
			@PathVariable("assigned") boolean assigned) {
		
		title = title.equals("uniformed") ? "" : title;
		status = status.equals("uniformed") ? "" : status;
		priority = priority.equals("uniformed") ? "" : priority;
		
		Response<Page<Ticket>> response = new Response<>();
		Page<Ticket> tickets = null;
		
		if (number > 0) {
			tickets = ticketService.findByNumber(page, count, number);
		} else {
			User userRequest = userFromRequest(request);
			if (userRequest.getProfile().equals(ProfileEnum.ROLE_TECHNICIAN)) {
				if (assigned) {
					tickets = ticketService.findByParametersAndAssignedUser(page, count, title, status, priority, userRequest.getId());
				} else {
					tickets = ticketService.findByParameters(page, count, title, status, priority);
				}
			} else if (userRequest.getProfile().equals(ProfileEnum.ROLE_CUSTOMER)) {
				tickets = ticketService.findByParametersAndCurrentUser(page, count, title, status, userRequest.getId());
			}
		}
		response.setData(tickets);
		return ResponseEntity.ok(response);
		
	}
		
	@PutMapping(value="{id}/{status}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
	public ResponseEntity<Response<Ticket>> changeStatus(
			@PathVariable("id") String id,
			@PathVariable("status") String status, 
			HttpServletRequest request, 
			@RequestBody Ticket ticket, 
			BindingResult result) {
		
		Response<Ticket> response = new Response<>();
		
		try {
			
			validateChangeStatus(id, status, result);
			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}
			
			Ticket ticketCurrent = ticketService.findById(id);
			ticketCurrent.setStatus(StatusEnum.getStatus(status));
			if (status.equals("Assigned")) {
				ticketCurrent.setAssignedUser(userFromRequest(request));
			}
			
			Ticket ticketPersisted = (Ticket) ticketService.createOrUpdate(ticketCurrent);
			ChangeStatus changeStatus = new ChangeStatus();
			changeStatus.setUserChange(userFromRequest(request));
			changeStatus.setDateChangeStatus(new Date());
			changeStatus.setStatus(StatusEnum.getStatus(status));
			changeStatus.setTicket(ticketPersisted);
			ticketService.createChangeStatus(changeStatus);
			response.setData(ticketPersisted);
			
		} catch (Exception e) {
			response.getErrors().add(e.getMessage());
			return ResponseEntity.ok(response);
		}
		
		return ResponseEntity.ok(response);
		
	}
	
	private void validateChangeStatus(String id, String status, BindingResult result) {
		if (id == null || id.equals("")) {
			result.addError(new ObjectError("Ticket", "Id no information"));
			return;
		}
		if (status == null || status.equals("")) {
			result.addError(new ObjectError("Ticket", "Status no information"));
		}
	}
	
	public ResponseEntity<Response<Summary>> findSummary() {
		
	}
	

}
