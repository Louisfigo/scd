package org.louis.ms.sc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Contract;

@Configuration
public class MyFeignConfig {
	
	@Bean
	public Contract feignContract()
	{
		return new feign.Contract.Default();
	}

}
