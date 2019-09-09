package com.own.ownauth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/test")
public class testController {

    @GetMapping("/{ids}")
    public String test(@PathVariable("ids") Long ids){
      log.info("ids {}",ids);
      return String.valueOf(ids);
    }
}
