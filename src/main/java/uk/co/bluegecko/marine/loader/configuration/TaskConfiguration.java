package uk.co.bluegecko.marine.loader.configuration;

import org.springframework.boot.task.TaskExecutorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskConfiguration {

	@Bean
	public TaskExecutorCustomizer taskExecutorCustomizer() {
		return taskExecutor -> taskExecutor.setDaemon(true);
	}

}