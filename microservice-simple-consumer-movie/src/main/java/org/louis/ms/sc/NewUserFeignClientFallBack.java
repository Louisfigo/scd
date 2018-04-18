package org.louis.ms.sc;

import org.springframework.stereotype.Component;

@Component
public class NewUserFeignClientFallBack implements  NewUserFeignClient{

	@Override
	public User findByUserId(Long id) {
	User user = new User();
	user.setId(-1l);
		return user;
	}

}
