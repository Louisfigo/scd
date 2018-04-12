package org.louis.ms.sc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
public class MovieController {

	private static Logger logger = LoggerFactory.getLogger(MovieController.class);
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	LoadBalancerClient loadBalanceClient;
	
	@GetMapping("/user/{id}")
	public User findById(@PathVariable Long id)
	{
		return this.restTemplate.getForObject("http://MC-PROVIDER-USER:8080/"+id, User.class);
	}
	
	@GetMapping("/log-instance")
	public String logUserInstance()
	{
		ServiceInstance serviceInstance = this.loadBalanceClient.choose("mc-provider-user");
		return serviceInstance.getServiceId()+";"+serviceInstance.getHost()+";"+serviceInstance.getPort();
		
	}
}
