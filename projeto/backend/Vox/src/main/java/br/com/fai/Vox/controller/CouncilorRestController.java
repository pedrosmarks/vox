package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.implementation.service.authentication.helper.AuthenticatedUserHelper;
import br.com.fai.Vox.port.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class CouncilorRestController {

    private final UserService userService;
    private final AuthenticatedUserHelper authHelper;

    public CouncilorRestController(UserService userService,
                                    AuthenticatedUserHelper authHelper) {
        this.userService = userService;
        this.authHelper = authHelper;
    }

    @GetMapping("/councilors")
    public ResponseEntity<List<UserModel>> findCouncilors(HttpServletRequest request) {
        List<UserModel> councilors = userService.findByRole(UserModel.UserRole.COUNCILOR.name());
        return ResponseEntity.ok(councilors);
    }

    @GetMapping("/councilors/{id}")
    public ResponseEntity<UserModel> findCouncilorById(@PathVariable final int id) {
        UserModel user = userService.findByid(id);
        if (user == null || user.getRole() != UserModel.UserRole.COUNCILOR) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}
