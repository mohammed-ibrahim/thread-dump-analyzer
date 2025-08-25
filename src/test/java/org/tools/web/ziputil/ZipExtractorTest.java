package org.tools.web.ziputil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.testng.Assert.*;

public class ZipExtractorTest {

  @Test
  public void testExtract() throws IOException {
    File genereatedZipFile = createTempFilesAndAddToZip();

    ZipExtractor zipExtractor = new ZipExtractor();
    List<File> files = zipExtractor.unZipToTempDirectory(genereatedZipFile);
    List<String> fileNames = files.stream().map(File::getName).collect(Collectors.toList());

    //check whether seeded filezips are available in the archieve
    assertTrue(fileNames.contains("a.txt"));
    assertTrue(fileNames.contains("b.txt"));
    assertTrue(fileNames.contains("c.txt"));

    zipExtractor.cleanup(files);

    files.forEach(fl -> {
      assertFalse(fl.exists());
    });

    genereatedZipFile.deleteOnExit();
  }

  private File createTempFilesAndAddToZip() throws IOException {
    String tempDirectoryPath = System.getProperty("java.io.tmpdir");
    Path tempDirectory = Paths.get(tempDirectoryPath, UUID.randomUUID().toString());
    tempDirectory.toFile().mkdirs();

    Path fileA = Paths.get(tempDirectoryPath, "a.txt");
    Path fileB = Paths.get(tempDirectoryPath, "b.txt");
    Path fileC = Paths.get(tempDirectoryPath, "c.txt");

    try {
      FileUtils.writeStringToFile(fileA.toFile(), "dummy", StandardCharsets.UTF_8);
      FileUtils.writeStringToFile(fileB.toFile(), "dummy", StandardCharsets.UTF_8);
      FileUtils.writeStringToFile(fileC.toFile(), "dummy", StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    Path zipArchive = Paths.get(tempDirectoryPath, "archive.txt");
    createZip(zipArchive.toString(), Arrays.asList(fileA.toFile(), fileB.toFile(), fileC.toFile()));
    return zipArchive.toFile();
  }

  private void createZip(String zipFilePath, List<File> files) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(zipFilePath);
         ZipOutputStream zos = new ZipOutputStream(fos)) {

      byte[] buffer = new byte[4096];

      for (File file : files) {
        if (!file.exists() || !file.isFile()) {
          System.out.println("Skipping invalid file: " + file.getAbsolutePath());
          continue;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
          ZipEntry zipEntry = new ZipEntry(file.getName());
          zos.putNextEntry(zipEntry);

          int len;
          while ((len = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
          }

          zos.closeEntry();
        }
      }
    }
  }
}