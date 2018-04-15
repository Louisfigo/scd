package org.louis.ms.sc;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


public interface NewUserFeignClient {
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public User findByUserId(@PathVariable("id") Long id);
	  
}
