package com.michael.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sun.misc.Unsafe;

@SpringBootApplication
public class App {
  public static void main(String[] args) {
    Unsafe.getUnsafe();
    SpringApplication.run(App.class);
  }
}
