package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.MunicipalityModel;
import br.com.fai.Vox.port.service.municipality.MunicipalityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/municipality")
public class MunicipalityRestController {

    private final MunicipalityService municipalityService;

    public MunicipalityRestController(MunicipalityService municipalityService) {
        this.municipalityService = municipalityService;
    }

    @GetMapping()
    public ResponseEntity<List<MunicipalityModel>> getEntities() {
        List<MunicipalityModel> entities = municipalityService.findAll();

        return ResponseEntity.ok(entities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MunicipalityModel> getEntityById(@PathVariable final int id) {
        MunicipalityModel entity = municipalityService.findByid(id);

        return entity == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(entity);
    }
}
