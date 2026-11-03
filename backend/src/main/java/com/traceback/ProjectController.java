package com.traceback;
import java.io.IOException;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/api/projects")
public class ProjectController {
 private final ProjectTests projects;
 public ProjectController(ProjectTests projects){this.projects=projects;}
 public record TestRequest(boolean downloadDependencies) {}
 @GetMapping public List<ProjectTests.Job> list(){return projects.list();}
 @GetMapping("/runner") public Map<String,Object> runner(){return projects.runner();}
 @PostMapping(consumes="multipart/form-data") public ProjectTests.Job upload(@RequestPart("file") MultipartFile file) throws IOException{return projects.upload(file);}
 @GetMapping("/{id}") public ProjectTests.Job get(@PathVariable UUID id){return projects.get(id);}
 @PostMapping("/{id}/test") public ProjectTests.Job test(@PathVariable UUID id,@RequestBody TestRequest request){return projects.start(id,request.downloadDependencies());}
}
