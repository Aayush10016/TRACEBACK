package com.traceback;
import static org.assertj.core.api.Assertions.*;
import java.io.*;
import java.nio.file.*;
import java.util.zip.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
class ProjectTestsTest {
 @TempDir Path temp;
 byte[] zip(String name,String content) throws Exception {var bytes=new ByteArrayOutputStream();try(var z=new ZipOutputStream(bytes)){z.putNextEntry(new ZipEntry(name));z.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));z.closeEntry();}return bytes.toByteArray();}
 @Test void extractsAndInventoriesSource() throws Exception {var files=ProjectTests.extract(new ByteArrayInputStream(zip("project/src/App.java","class App {}")),temp);assertThat(files).containsExactly("project/src/App.java");assertThat(Files.readString(temp.resolve(files.get(0)))).contains("class App");}
 @Test void rejectsTraversal() throws Exception {assertThatThrownBy(()->ProjectTests.extract(new ByteArrayInputStream(zip("../escaped.java","x")),temp)).isInstanceOf(ApiException.class);}
 @Test void rejectsWindowsAbsolutePaths() throws Exception {assertThatThrownBy(()->ProjectTests.extract(new ByteArrayInputStream(zip("C:\\secret.txt","x")),temp)).isInstanceOf(ApiException.class);}
 @Test void rejectsCredentials() throws Exception {assertThatThrownBy(()->ProjectTests.extract(new ByteArrayInputStream(zip("project/.env","fake=test")),temp)).isInstanceOf(ApiException.class);}
 @Test void rejectsEmptyZip() throws Exception {var b=new ByteArrayOutputStream();new ZipOutputStream(b).close();assertThatThrownBy(()->ProjectTests.extract(new ByteArrayInputStream(b.toByteArray()),temp)).isInstanceOf(ApiException.class);}
 @Test void readsRealJUnitCounts() throws Exception {Files.writeString(temp.resolve("TEST-Example.xml"),"<testsuite tests=\"3\" failures=\"1\" errors=\"0\" skipped=\"1\"/>");assertThat(ProjectTests.readReports(temp)).isEqualTo(new ProjectTests.Counts(3,1,0,1));}
 @Test void emptyReportDirectoryDoesNotInventTests() throws Exception {assertThat(ProjectTests.readReports(temp).tests()).isZero();}
 @Test void ignoredMavenFailuresCannotPass() {assertThat(ProjectTests.classify(0,new ProjectTests.Counts(2,1,0,0))).isEqualTo("TESTS_FAILED");}
 @Test void entirelySkippedSuiteCannotPass() {assertThat(ProjectTests.classify(0,new ProjectTests.Counts(2,0,0,2))).isEqualTo("NO_TESTS");}
 @Test void compilationFailureCannotPass() {assertThat(ProjectTests.classify(1,new ProjectTests.Counts(0,0,0,0))).isEqualTo("BUILD_FAILED");}
 @Test void blocksExternalXmlEntities() throws Exception {Files.writeString(temp.resolve("TEST-malicious.xml"),"<!DOCTYPE x [<!ENTITY a SYSTEM 'file:///etc/passwd'>]><testsuite tests=\"1\" failures=\"0\" errors=\"0\" skipped=\"0\">&a;</testsuite>");assertThatThrownBy(()->ProjectTests.readReports(temp)).isInstanceOf(Exception.class);}
}
