package at.fhv.eventmangement.presentation.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(@RequestParam(required = false) String location,
                       @RequestParam(required = false) String date,
                       Model model) {
                        /* location, date and model are not used yet */
        return "homepage";
    }
}