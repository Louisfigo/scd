package org.louis.ms.sc.controller;

import org.louis.ms.sc.dao.UserRepository;
import org.louis.ms.sc.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("/{id}")
	public User findById(@PathVariable Long id)
	{
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		if(principal instanceof UserDetails)
		{
			UserDetails userDetails = (UserDetails) principal;
			
			userDetails.getAuthorities().forEach(au->{
				System.out.println(userDetails.getUsername()+"|"+au.getAuthority());
			});
		}
		
		User uo = this.userRepository.findOne(id);
		System.out.print(this.userRepository.count());
		return uo;
		
	}

}
