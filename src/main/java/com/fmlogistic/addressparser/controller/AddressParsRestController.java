package com.fmlogistic.addressparser.controller;

import com.fmlogistic.addressparser.model.Address;
import com.fmlogistic.addressparser.service.ParsingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AddressParsRestController {

    @Autowired
    private ParsingService parsingService;

    @RequestMapping(value = "parsAddress", method = RequestMethod.GET)
    public ResponseEntity<?> createAuthenticationToken(@RequestParam("addressLine") String addressline)  {

        Address address = new Address();
        address.setAddressLine(addressline);

        address = parsingService.parsAddress(addressline);
        if(address != null)
            return ResponseEntity.ok(address);
        else
            return ResponseEntity.badRequest().body("Cannot pars");
    }
}
