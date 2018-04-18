package org.louis.ms.sc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import feign.hystrix.FallbackFactory;

@Component
public class FeignClientFallBackFactory implements FallbackFactory<NewUserFeignClient> {

	@Autowired
	NewUserFeignClientFallBack nus;
	
	@Override
	public NewUserFeignClient create(Throwable cause) {
		// TODO Auto-generated method stub
		
		System.out.println("fall in fall back");
		return nus;
	}

}
