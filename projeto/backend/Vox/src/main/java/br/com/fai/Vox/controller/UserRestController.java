package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.domain.dto.UpdatePasswordDto;
import br.com.fai.Vox.port.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping()
    public ResponseEntity<List<UserModel>> getEntities() {
        List<UserModel> entities = userService.findAll();

        return ResponseEntity.ok(entities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserModel> getEntityById(@PathVariable final int id) {
        UserModel entity = userService.findByid(id);

        return entity == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable final int id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<UserModel> create(@RequestBody final UserModel data) {
        final int id = userService.create(data);

        final URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserModel> update(@PathVariable final int id, @RequestBody final UserModel data) {

        userService.update(id, data);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update-password")
    public ResponseEntity<Void> updatePassword(@RequestBody final UpdatePasswordDto data) {

        final boolean response = userService.updatePassword(
                data.getId(),
                data.getOldPassword(),
                data.getNewPassword());

        return response ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserModel> getEntityByEmail(@PathVariable final String email) {
        final UserModel entity = userService.findByEmail(email);
        if(entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(entity);
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserModel>> getEntityByRole(@PathVariable final String role) {
        final List<UserModel> entity = userService.findByRole(role);
        if(entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(entity);
    }

    @PutMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody final String email) {

        final boolean response = userService.forgotPassword(email);

        return response ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @PutMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody final String token, @RequestBody final String newPassword) {

        final boolean response = userService.resetPassword(token, newPassword);

        return response ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

}
