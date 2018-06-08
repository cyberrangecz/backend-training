package cz.muni.ics.kypo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import cz.muni.ics.kypo.model.TrainingInstance;
import cz.muni.ics.kypo.repository.TrainingDefinitionRepository;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@SpringBootApplication
@EnableAutoConfiguration
public class SpringBootAutoConfig implements CommandLineRunner {

  @Autowired
  private TrainingDefinitionRepository trainingRepository;

  public static void main(String args[]) {
    SpringApplication.run(SpringBootAutoConfig.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    trainingRepository.save(new TrainingInstance());
  }

}
