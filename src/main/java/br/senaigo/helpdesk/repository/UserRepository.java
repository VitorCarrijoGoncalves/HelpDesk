package br.senaigo.helpdesk.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.senaigo.helpdesk.entity.User;

public interface UserRepository extends MongoRepository<User, String> {
	
	User findByEmail(String email);

}
