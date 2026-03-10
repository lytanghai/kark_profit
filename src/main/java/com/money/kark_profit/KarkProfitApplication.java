package com.money.kark_profit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KarkProfitApplication {

	public static void main(String[] args) {
		SpringApplication.run(KarkProfitApplication.class, args);
	}

}

//TODO
/**
 * 1. Update JWT Secret Key to Config
 * 2. Update Request & Response of Auth Api
 * 3. Convert XML to DTO class
 * 4. Update Try Catch to display error message on Auth Api
 * 5. Impl Circuit Breaker and set config in table
 *     url: jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres
 *     username: postgres.akqbuskviaxgpmzogytu
 *     password: 309927605Hai@
 * */