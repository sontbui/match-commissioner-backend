package com.sontbui.match_commissioner;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class MatchCommissionerApplication {

	public static void main(String[] args) {
		String activeProfile = System.getenv("SPRING_PROFILES_ACTIVE");
		if (activeProfile == null) {
			activeProfile = "dev";
			System.setProperty("spring.profiles.active", activeProfile);
		}
		if (activeProfile.equalsIgnoreCase("dev") || activeProfile.equalsIgnoreCase("local")) {
			try {
				Dotenv dotenv = Dotenv.configure()
						.directory("./")
						.ignoreIfMissing()
						.load();
				dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
				System.out.println("Loaded .env for " + activeProfile);
			} catch (Exception e) {
				System.out.println("Skipped .env loading. Using environment variables. Error: " + e.getMessage());
			}
		} else {
			System.out.println("Running with profile: " + activeProfile + " (no .env file loaded)");
		}

		SpringApplication.run(MatchCommissionerApplication.class, args);
		
		System.out.println("✅ Running on port: " + System.getenv("PORT"));

		System.out.println("✅ Match Commissioner started with profile: " +
				Arrays.toString(SpringApplication.run(MatchCommissionerApplication.class, args)
						.getEnvironment().getActiveProfiles()));

	}
	// docker rm -f match-commissioner-backend
	// docker build --no-cache -t match-commissioner-backend .

	// docker run -p 8080:8080 --env-file .env match-commissioner-backend
	// docker run -d --name match-commissioner-backend -p 8080:8080 --env-file .env
	// match-commissioner-backend

}
