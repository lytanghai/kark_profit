package com.money.kark_profit.controller;


import com.money.kark_profit.service.ConfigurationService;
import com.money.kark_profit.transform.request.ConfigurationRequest;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/config")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    @PostMapping("/create")
    public ResponseEntity<ResponseBuilderUtils> createConfig(@RequestBody ConfigurationRequest configuration) {
        return new ResponseEntity<>(configurationService.createConfig(configuration), HttpStatus.OK);
    }
    @PostMapping("/delete")
    public ResponseEntity<ResponseBuilderUtils> deleteConfiguration(@RequestBody ConfigurationRequest configurationRequest) {
        return new ResponseEntity<>(configurationService.deleteConfiguration(configurationRequest), HttpStatus.OK);
    }
    @PostMapping("/update")
    public ResponseEntity<ResponseBuilderUtils> updateConfiguration(@RequestBody ConfigurationRequest configurationRequest) {
        return new ResponseEntity<>(configurationService.updateConfiguration(configurationRequest), HttpStatus.OK);
    }
    @PostMapping("/list")
    public ResponseEntity<ResponseBuilderUtils> listing(@RequestBody ConfigurationRequest configuration) {
        return new ResponseEntity<>(configurationService.listingConfig(configuration), HttpStatus.OK);
    }

}