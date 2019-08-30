package br.senaigo.helpdesk.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.senaigo.helpdesk.entity.ChangeStatus;

public interface ChangeStatusRepository extends MongoRepository<ChangeStatus, String> {
	
	Iterable<ChangeStatus> findByTicketIdOrderByDateChangeStatusDesc(String ticketId);

}
