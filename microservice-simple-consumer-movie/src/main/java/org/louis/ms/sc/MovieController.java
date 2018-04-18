package org.louis.ms.sc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.feign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import feign.Client;
import feign.Contract;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.hystrix.HystrixFeign;

@Import(FeignClientsConfiguration.class)
@RestController
public class MovieController {

	
	private NewUserFeignClient userFeignClient;
	
	
	private NewUserFeignClient adminFeignClient;
	

	@Autowired
	public  MovieController(Decoder decoder,Encoder encoder,Client client,Contract contract) {

	    this.userFeignClient = HystrixFeign.builder().client(client).encoder(encoder).decoder(decoder).contract(contract)
	            .requestInterceptor(new BasicAuthRequestInterceptor("user", "password1")).target(NewUserFeignClient.class, "http://MC-PROVIDER-USER/",new FeignClientFallBackFactory());
	            
	            //target(NewUserFeignClient.class, "http://MC-PROVIDER-USER/",NewUserFeignClientFallBack.class);
	            
	        this.adminFeignClient = HystrixFeign.builder().client(client).encoder(encoder).decoder(decoder).contract(contract)
	            .requestInterceptor(new BasicAuthRequestInterceptor("admin", "password2"))
	            .target(NewUserFeignClient.class, "http://MC-PROVIDER-USER/",new FeignClientFallBackFactory());
	}
	
	@HystrixCommand(fallbackMethod="findIdFallBack")
	@RequestMapping(value="/user/{id}",method=RequestMethod.GET)
	public User findByUserId(@PathVariable Long id)
	{
		return userFeignClient.findByUserId(id);
	}
	
	@RequestMapping(value="/admin/{id}",method=RequestMethod.GET)
	public User findByAdminId(@PathVariable Long id)
	{
		return adminFeignClient.findByUserId(id);
	}
	
	public User findIdFallBack(Long id)
	{
		User user =  new User();
		
		user.setId(-1L);
		
		return user;
	}
	

}
