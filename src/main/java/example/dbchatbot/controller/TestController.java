// package example.dbchatbot.controller;
package example.dbchatbot.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}