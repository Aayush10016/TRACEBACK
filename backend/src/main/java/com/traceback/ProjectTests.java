package com.traceback;

import static org.springframework.http.HttpStatus.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProjectTests {
  public record Counts(int tests, int failures, int errors, int skipped) {}
  public record Job(UUID id, String filename, Instant createdAt, String status, String projectRoot,
      List<String> files, String command, String output, Counts counts, String message) {}
  private final Path root;
  private final String image;
  private final JdbcTemplate jdbc;
  private final ObjectMapper json;
  private final AtomicBoolean busy = new AtomicBoolean();
  private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {var t = new Thread(r, "project-test-runner");t.setDaemon(true);return t;});

  public ProjectTests(@Value("${traceback.projects.directory}") String directory,
      @Value("${traceback.projects.runner-image}") String image, JdbcTemplate jdbc, ObjectMapper json) throws IOException {
    root = Path.of(directory).toAbsolutePath().normalize();
    this.image = image; this.jdbc = jdbc; this.json = json;
    Files.createDirectories(root);
  }
  @EventListener(ApplicationReadyEvent.class)
  public void recover() {
    for (var job : list()) if (job.status().equals("RUNNING"))
      save(update(job, "INTERRUPTED", job.output(), null, "Server restarted during execution. Upload again or rerun; no passing result is assumed."));
  }
  public List<Job> list() { return jdbc.query("select payload from project_test_job order by created_at desc limit 20", (rs,n) -> decode(rs.getString(1))); }
  public Job get(UUID id) {
    var rows = jdbc.query("select payload from project_test_job where id=?", (rs,n) -> decode(rs.getString(1)), id);
    if(rows.isEmpty()) throw new ApiException(NOT_FOUND,"PROJECT_NOT_FOUND","Uploaded project was not found.");
    return rows.get(0);
  }
  private Job decode(String value) {try{return json.readValue(value,Job.class);}catch(Exception e){throw new IllegalStateException("Stored project result could not be read.");}}
  private synchronized void save(Job j) {
    try { String value=json.writeValueAsString(j); int changed=jdbc.update("update project_test_job set payload=? where id=?",value,j.id());
      if(changed==0) jdbc.update("insert into project_test_job(id,created_at,payload) values(?,?,?)",j.id(),java.sql.Timestamp.from(j.createdAt()),value);
    } catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new IllegalStateException("Project result could not be saved.");}
  }
  private Job update(Job j,String status,String output,Counts counts,String message) {return new Job(j.id(),j.filename(),j.createdAt(),status,j.projectRoot(),j.files(),"mvn -o -B -ntp -Dmaven.repo.local=/cache clean verify",output,counts,message);}

  public Job upload(MultipartFile file) throws IOException {
    String name=Optional.ofNullable(file.getOriginalFilename()).orElse("project.zip").replace('\\','/');
    name=name.substring(name.lastIndexOf('/')+1);
    if(file.isEmpty() || file.getSize()>20*1024*1024 || !name.toLowerCase(Locale.ROOT).endsWith(".zip"))
      throw new ApiException(BAD_REQUEST,"INVALID_ZIP","Upload a nonempty Java/Maven project ZIP up to 20 MB.");
    UUID id=UUID.randomUUID();Path source=root.resolve(id.toString()).resolve("source");Files.createDirectories(source);
    try {
      List<String> files=extract(file.getInputStream(),source);
      List<Path> poms=files.stream().filter(f->f.equals("pom.xml")||f.endsWith("/pom.xml")).map(source::resolve).sorted(Comparator.comparingInt(Path::getNameCount)).toList();
      if(poms.isEmpty()) throw new ApiException(BAD_REQUEST,"MAVEN_REQUIRED","No pom.xml found. Upload a Java 17 Maven/Spring project ZIP, including its pom.xml and tests. Gradle and standalone files are not supported by this runner.");
      Path project=poms.get(0).getParent();
      if(poms.stream().anyMatch(p->!p.startsWith(project))) throw new ApiException(BAD_REQUEST,"AMBIGUOUS_PROJECT","The ZIP contains separate Maven roots. Upload one project ZIP at a time; Maven multi-module projects with a parent pom.xml are supported.");
      String relative=source.relativize(project).toString().replace('\\','/');
      var job=new Job(id,name,Instant.now(),"READY",relative.isEmpty()?".":relative,files,"", "",null,"Project uploaded. Review the file inventory, then run its existing Maven tests.");save(job);return job;
    }catch(Exception e){removeTree(source.getParent());throw e;}
  }
  static List<String> extract(InputStream input,Path source) throws IOException {
    var files=new ArrayList<String>();var seen=new HashSet<String>();long total=0;int entries=0;
    try(var zip=new ZipInputStream(input,StandardCharsets.UTF_8)) {
      ZipEntry entry;
      while((entry=zip.getNextEntry())!=null) {
        if(++entries>3000) throw invalid("ZIP contains more than 3,000 entries.");
        String name=entry.getName().replace('\\','/');
        if(name.length()>240 || name.startsWith("/") || name.contains(":") || Arrays.asList(name.split("/")).contains("..")) throw invalid("ZIP contains an unsafe file path.");
        String lower=name.toLowerCase(Locale.ROOT);
        if(Arrays.stream(lower.split("/")).anyMatch(s->s.equals(".env")||s.startsWith(".env.")||s.equals(".ssh")||s.equals("id_rsa")) && !lower.endsWith(".env.example")) throw invalid("Remove .env files and private credentials before uploading a project.");
        Path dest=source.resolve(name).normalize();if(!dest.startsWith(source)||!seen.add(name))throw invalid("ZIP contains duplicate or unsafe paths.");
        if(entry.isDirectory()){Files.createDirectories(dest);continue;}
        Files.createDirectories(dest.getParent());
        try(var out=Files.newOutputStream(dest,StandardOpenOption.CREATE_NEW)) {byte[] buffer=new byte[8192];int n;while((n=zip.read(buffer))!=-1){total+=n;if(total>100L*1024*1024)throw invalid("ZIP expands beyond the 100 MB limit.");out.write(buffer,0,n);}}
        files.add(name);
      }
    }catch(ZipException e){throw invalid("The uploaded ZIP is invalid or damaged.");}
    if(files.isEmpty())throw invalid("The ZIP contains no files.");files.sort(String::compareTo);return List.copyOf(files);
  }
  private static ApiException invalid(String message){return new ApiException(BAD_REQUEST,"UNSAFE_ZIP",message);}
  static void removeTree(Path path) throws IOException { if(Files.exists(path)){try(var walk=Files.walk(path)){for(Path p:walk.sorted(Comparator.reverseOrder()).toList())Files.delete(p);}} }

  public Map<String,Object> runner() {
    try { var check=process(List.of("docker","image","inspect",image,"--format","{{.Id}}"),root.resolve("runner-check.log"),20);
      return Map.of("available",check==0,"image",image,"message",check==0?"Docker runner ready · Maven 3.9.16 / Java 17":"Docker runner unavailable. Start Docker and build Dockerfile.runner.");
    } catch(Exception e) {return Map.of("available",false,"image",image,"message","Docker runner unavailable. Start Docker and build Dockerfile.runner.");}
  }
  public Job start(UUID id,boolean dependencies) {
    Job j=get(id);
    if(!busy.compareAndSet(false,true))throw new ApiException(CONFLICT,"RUNNER_BUSY","Another project is running. Wait for it to finish.");
    if(!(Boolean)runner().get("available")){busy.set(false);throw new ApiException(SERVICE_UNAVAILABLE,"RUNNER_UNAVAILABLE","Start Docker and build the Java runner image before testing uploads.");}
    var started=update(j,"RUNNING","",null,dependencies?"Preparing dependencies with network access, then running tests offline.":"Running existing Maven tests offline. Dependencies must already be cached for this upload.");
    try {save(started);executor.submit(()->execute(started,dependencies));return started;}catch(RuntimeException e){busy.set(false);throw e;}
  }
  private void execute(Job j,boolean dependencies) {
    String prefix="traceback-job-"+j.id();Path dir=root.resolve(j.id().toString());StringBuilder output=new StringBuilder();
    try {
      if(dependencies){int code=stage(j,prefix+"-prepare",true,dir,output,null);if(code!=0){save(update(j,code==124?"TIMED_OUT":"DEPENDENCY_FAILED",output.toString(),null,"Dependency preparation failed. Review output; tests were not reported as passing."));return;}}
      int code=stage(j,prefix+"-test",false,dir,output,null);
      for(int attempt=0; dependencies && code!=0 && code!=124 && attempt<2; attempt++) {
        var missing=java.util.regex.Pattern.compile("([A-Za-z0-9_.-]+):([A-Za-z0-9_.-]+):jar:([A-Za-z0-9_.-]+) \\(absent\\)").matcher(readBounded(dir.resolve("tests.log")));
        if(!missing.find())break;
        String artifact=missing.group(1)+":"+missing.group(2)+":"+missing.group(3);
        output.append("\nPreparing a dynamically requested Maven test dependency: ").append(artifact).append("\n");
        int downloaded=stage(j,prefix+"-dynamic-"+attempt,true,dir,output,artifact);
        if(downloaded!=0){code=downloaded;break;}
        code=stage(j,prefix+"-test",false,dir,output,null);
      }
      Counts counts=readReports(dir.resolve("reports"));
      String status=classify(code,counts);
      save(update(j,status,output.toString(),counts,status.equals("PASSED")?"Maven verify completed and reported tests passed. This covers the uploaded project's configured tests, not every possible defect.":status.equals("NO_TESTS")?"Build completed but no executed test reports were found. No test pass is claimed.":"Execution did not pass. Review the real Maven output and test counts."));
    } catch(Exception e) {save(update(j,"ERROR",output.toString(),null,"Runner could not finish. Check Docker resources and runner installation; no passing result is assumed."));}
    finally {try { process(List.of("docker","volume","rm",prefix+"-work",prefix+"-reports"),dir.resolve("volume-cleanup.log"),20); } catch(Exception ignored) {} busy.set(false);}
  }
  private int stage(Job j,String name,boolean prepare,Path dir,StringBuilder output,String artifact) throws Exception {
    String prefix="traceback-job-"+j.id();String rootPart=j.projectRoot().equals(".")?"":"/"+j.projectRoot();
    // Only a quoted positional argument receives the uploaded project root; no uploaded text is shell code.
    String command="cp -R /input/. /workspace/; cd \"$1\" || exit 2; result=0; "+(prepare?"timeout 300s mvn -U -B -ntp -Dmaven.repo.local=/cache dependency:go-offline dependency:resolve":"timeout 600s mvn -o -B -ntp -Dmaven.repo.local=/cache clean verify")+" || result=$?; "+(!prepare?"find /workspace -type f \\( -path '*/surefire-reports/TEST-*.xml' -o -path '*/failsafe-reports/TEST-*.xml' \\) -exec cp --parents {} /reports/ \\; ; ":"")+"exit $result";
    if(artifact!=null){if(!artifact.matches("[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+"))throw new IOException("Invalid dependency coordinates"); command=command.replace("dependency:go-offline dependency:resolve","dependency:get -Dartifact="+artifact);}
    command=command.replace("cp -R /input/. /workspace/;", "cp -R /input/. /workspace/; mkdir -p /workspace/.tmp;");
    // Clean old generated results on rerun; never use uploaded target directories as evidence.
    command="rm -rf /reports/*; find /workspace -mindepth 1 -maxdepth 1 -exec rm -rf -- {} +; "+command.replace("; result=0;", "; find /workspace -type d -name target -prune -exec rm -rf -- {} +; result=0;");
    var args=new ArrayList<>(List.of("docker","create","--name",name,"--read-only","--cap-drop=ALL","--security-opt=no-new-privileges","--memory=1536m","--cpus=2","--pids-limit=256","-e","MAVEN_CONFIG=/cache","-e","JAVA_TOOL_OPTIONS=-Djava.io.tmpdir=/workspace/.tmp -Djansi.tmpdir=/workspace/.tmp","--mount","type=volume,source="+name+"-input,target=/input","--network",prepare?"bridge":"none","--tmpfs","/tmp:rw,nosuid,nodev,size=256m","--mount","type=volume,source="+prefix+"-work,target=/workspace","--mount","type=volume,source="+prefix+"-cache,target=/cache","--mount","type=volume,source="+prefix+"-reports,target=/reports",image,"sh","-c",command,"runner","/workspace"+rootPart));
    Path log=dir.resolve(prepare?"dependencies.log":"tests.log");
    process(List.of("docker","rm","-f",name),dir.resolve("cleanup.log"),20);
    if(process(args,dir.resolve("create.log"),30)!=0)throw new IOException("Runner create failed");
    try {
      if(process(List.of("docker","cp",dir.resolve("source").toString()+"/.",name+":/input"),dir.resolve("copy.log"),60)!=0)throw new IOException("Runner source copy failed");
      int code=process(List.of("docker","start","-a",name),log,prepare?300:600);
      if(code==124)process(List.of("docker","kill",name),dir.resolve("kill.log"),20);
      output.append(prepare?"DEPENDENCY PREPARATION (network enabled)\n":"MAVEN VERIFICATION (network disabled)\n").append(readBounded(log)).append("\n");
      if(!prepare) {
        Path reports=dir.resolve("reports");removeTree(reports);Files.createDirectories(reports);
        process(List.of("docker","cp",name+":/reports/.",reports.toString()),dir.resolve("reports-copy.log"),60);
      }
      return code;
    } finally {process(List.of("docker","rm","-f",name),dir.resolve("cleanup.log"),20);process(List.of("docker","volume","rm",name+"-input"),dir.resolve("input-cleanup.log"),20);}
  }
  static String classify(int code, Counts counts) {
    if(code==124)return "TIMED_OUT";
    if(counts.failures()>0 || counts.errors()>0)return "TESTS_FAILED";
    if(code!=0)return "BUILD_FAILED";
    return counts.tests()-counts.skipped()<=0?"NO_TESTS":"PASSED";
  }
  static Counts readReports(Path path) throws Exception {
    int tests=0,failures=0,errors=0,skipped=0;
    if(!Files.exists(path))return new Counts(0,0,0,0);
    var factory=DocumentBuilderFactory.newInstance();factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD,"");factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA,"");
    try(var files=Files.walk(path)) {for(Path p:files.filter(p->p.getFileName().toString().startsWith("TEST-")&&p.toString().endsWith(".xml")).toList()) {
      if(Files.size(p)>5*1024*1024)throw new IOException("Test report too large");
      var node=factory.newDocumentBuilder().parse(p.toFile()).getDocumentElement();
      tests+=Integer.parseInt(node.getAttribute("tests"));failures+=Integer.parseInt(node.getAttribute("failures"));errors+=Integer.parseInt(node.getAttribute("errors"));String skip=node.getAttribute("skipped");skipped+=skip.isBlank()?0:Integer.parseInt(skip);
    }}return new Counts(tests,failures,errors,skipped);
  }
  static int process(List<String> args,Path log,int seconds) throws Exception {
    var p=new ProcessBuilder(args).redirectErrorStream(true).start();
    Thread drain=new Thread(()->{try(var input=p.getInputStream();var out=Files.newOutputStream(log)){byte[] buf=new byte[8192];int n,remaining=1024*1024;boolean truncated=false;while((n=input.read(buf))!=-1){int keep=Math.min(n,remaining);if(keep>0){out.write(buf,0,keep);remaining-=keep;}if(n>keep)truncated=true;}if(truncated)out.write("\n[Output truncated at 1 MB]".getBytes(StandardCharsets.UTF_8));}catch(IOException ignored){}});
    drain.start(); boolean finished=p.waitFor(seconds,TimeUnit.SECONDS);if(!finished){p.destroyForcibly();p.waitFor(5,TimeUnit.SECONDS);}drain.join(5000);return finished?p.exitValue():124;
  }
  static String readBounded(Path log) throws IOException {try(var in=Files.newInputStream(log)){var data=in.readNBytes(1024*1024);return new String(data,StandardCharsets.UTF_8)+(in.read()!=-1?"\n[Output truncated at 1 MB]":"");}}
}
