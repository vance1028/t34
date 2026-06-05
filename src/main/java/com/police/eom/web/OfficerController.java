package com.police.eom.web;

import com.police.eom.domain.Officer;
import com.police.eom.repo.OfficerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/officers")
public class OfficerController {

    private final OfficerRepository officerRepo;

    public OfficerController(OfficerRepository officerRepo) {
        this.officerRepo = officerRepo;
    }

    @GetMapping
    public List<Officer> list() {
        return officerRepo.findAll();
    }

    @GetMapping("/{id}")
    public Officer get(@PathVariable Long id) {
        return officerRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("民警不存在"));
    }

    @PostMapping
    public ResponseEntity<Officer> create(@RequestBody Officer input) {
        if (input.getPoliceNo() == null || input.getPoliceNo().isBlank()) {
            throw ApiException.badRequest("警号不能为空");
        }
        if (input.getName() == null || input.getName().isBlank()) {
            throw ApiException.badRequest("姓名不能为空");
        }
        if (officerRepo.existsByPoliceNo(input.getPoliceNo())) {
            throw ApiException.conflict("警号已存在");
        }
        input.setId(null);
        Officer saved = officerRepo.save(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
