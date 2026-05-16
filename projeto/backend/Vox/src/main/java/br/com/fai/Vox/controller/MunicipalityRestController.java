package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.Municipality;
import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.port.service.municipality.MunicipalityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/municipality")
public class MunicipalityRestController {

    private final MunicipalityService municipalityService;

    public MunicipalityRestController(MunicipalityService municipalityService) {
        this.municipalityService = municipalityService;
    }

    @PostMapping
    public ResponseEntity<Municipality> create(@RequestBody final Municipality data) {
        final int id = municipalityService.create(data);

        final URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping()
    public ResponseEntity<List<Municipality>> getEntities() {
        List<Municipality> entities = municipalityService.findAll();

        return ResponseEntity.ok(entities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Municipality> getEntityById(@PathVariable final int id) {
        Municipality entity = municipalityService.findByid(id);

        return entity == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(entity);
    }
}
