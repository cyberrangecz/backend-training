package cz.muni.ics.kypo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import cz.muni.ics.kypo.model.InfoLevel;
import cz.muni.ics.kypo.repository.InfoLevelRepository;

@SpringBootApplication
public class SpringBootRun implements CommandLineRunner {

  @Autowired
  private InfoLevelRepository infoLevelRepository;

  public static void main(String[] args) {
    SpringApplication.run(SpringBootRun.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    InfoLevel il = new InfoLevel();
    il.setContent("test".getBytes());
    // infoLevelRepository.save(il);
  }

}
