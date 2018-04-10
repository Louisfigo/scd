package org.louis.ms.sc.dao;

import org.louis.ms.sc.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	
}
