package com.literatura.challenge;

import com.literatura.challenge.Principal.Principal;
import com.literatura.challenge.Repository.AutorRepository;
import com.literatura.challenge.Repository.LibroRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChallengeApplication implements CommandLineRunner {
	@Autowired
	private LibroRepository libroRepository;

	@Autowired
	private AutorRepository autorRepository;

	public static void main(String[] args) {
		SpringApplication.run(ChallengeApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Principal principal = new Principal(autorRepository, libroRepository);
		principal.inicio();
	}
}
