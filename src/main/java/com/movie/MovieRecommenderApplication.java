package com.movie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MovieRecommenderApplication {
	public static void main(String[] args) {
		SpringApplication.run(MovieRecommenderApplication.class, args);
		System.out.println("Movie Recommender STARTED on port 8080!");

	}
}