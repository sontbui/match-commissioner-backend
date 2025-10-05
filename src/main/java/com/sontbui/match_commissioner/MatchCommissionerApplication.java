package com.sontbui.match_commissioner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class MatchCommissionerApplication {

	public static void main(String[] args) {
		String env = System.getenv("SPRING_PROFILES_ACTIVE");
		if (env == null || env.equalsIgnoreCase("dev")) {
			try {
				Dotenv dotenv = Dotenv.configure()
						.directory("./")
						.ignoreIfMissing()
						.load();
				dotenv.entries().forEach(
						entry -> System.setProperty(entry.getKey(), entry.getValue()));
				System.out.println("Loaded .env for local");

			} catch (Exception e) {
				System.out.println("Skipped .env loading. Using enviroment variable || Error message " + e.getMessage());
			}
		}

		SpringApplication.run(MatchCommissionerApplication.class, args);
	}
	// docker run -p 8080:8080 --env-file .env match-commissioner-backend
}
