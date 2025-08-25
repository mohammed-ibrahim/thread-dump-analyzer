package org.tools.web.ziputil;

import org.springframework.stereotype.Service;
import org.tools.web.model.ThreadDetail;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ZipExtractor {
  public List<File> unZipToTempDirectory(File zipFilePath) {
    String tempDirectoryPath = System.getProperty("java.io.tmpdir");
    Path unzippingDirectory = Paths.get(tempDirectoryPath, UUID.randomUUID().toString());
    unzippingDirectory.toFile().mkdirs();

    try {
      extractZip(zipFilePath.getAbsolutePath(), unzippingDirectory.toString());
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    File[] files = unzippingDirectory.toFile().listFiles();
    return Arrays.asList(files);
  }

  public static void extractZip(String zipFilePath, String destDirectory) throws IOException {
    Path destDir = Paths.get(destDirectory);
    if (!Files.exists(destDir)) {
      Files.createDirectories(destDir);
    }

    try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
      ZipEntry entry;
      while ((entry = zipIn.getNextEntry()) != null) {
        Path filePath = destDir.resolve(entry.getName());

        if (entry.isDirectory()) {
          Files.createDirectories(filePath);
        } else {
          // Create parent directories if needed
          Files.createDirectories(filePath.getParent());

          try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(filePath))) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = zipIn.read(buffer)) != -1) {
              bos.write(buffer, 0, read);
            }
          }
        }
        zipIn.closeEntry();
      }
    }
  }

  public void cleanup(List<File> files) {

    files.forEach(file -> {
      try {
        file.delete();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

  }
}
