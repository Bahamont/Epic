package com.epic;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping("/")
	public Map<String, String> home() {
		return Map.of(
				"message", "Bienvenido a Epic",
				"status", "ok"
		);
	}

	@GetMapping("/api/hello")
	public Map<String, String> hello() {
		return Map.of("message", "Hola desde Spring Boot");
	}
}
