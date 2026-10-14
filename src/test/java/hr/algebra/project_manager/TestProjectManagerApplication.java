package hr.algebra.project_manager;

import org.springframework.boot.SpringApplication;

public class TestProjectManagerApplication {

	public static void main(String[] args) {
		SpringApplication.from(ProjectManagerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
