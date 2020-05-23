/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeyForSubmissionTimestamp;
import static app.coronawarn.server.services.distribution.common.Helpers.buildSampleExportConfiguration;
import static java.lang.String.join;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.Export;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import app.coronawarn.server.services.distribution.persistence.domain.ExportBatch;
import app.coronawarn.server.services.distribution.persistence.domain.ExportBatchStatus;
import app.coronawarn.server.services.distribution.persistence.domain.ExportConfiguration;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CryptoProvider.class},
    initializers = ConfigFileApplicationContextInitializer.class)
public class DiagnosisKeysDirectoryTest {

  @Autowired
  CryptoProvider cryptoProvider;

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();

  private File outputFile;
  private Directory<WritableOnDisk> parentDirectory;

  List<Export> export;

  @BeforeEach
  void setupAll() throws IOException {
    outputFolder.create();
    outputFile = outputFolder.newFolder();
    parentDirectory = new DirectoryOnDisk(outputFile);

    // 01.01.1970 - 00:00 UTC
    long startTimestamp = 0;

    // TODO create export from diagnosis keys (builder needed)
    // Generate diagnosis keys covering 30 hours of submission timestamps
    // Until 02.01.1970 - 06:00 UTC -> 1 full day + 6 hours
    List<DiagnosisKey> diagnosisKeys = IntStream.range(0, 30)
        .mapToObj(
            currentHour -> buildDiagnosisKeyForSubmissionTimestamp(startTimestamp + currentHour))
        .collect(Collectors.toList());

    ExportConfiguration exportConfiguration = buildSampleExportConfiguration(1, Instant.now()
            .minus(2, ChronoUnit.DAYS), Instant.now().plus(2, ChronoUnit.DAYS));

    ExportBatch exportBatch = new ExportBatch(exportConfiguration.getFromTimestamp(),
            exportConfiguration.getThruTimestamp(), ExportBatchStatus.OPEN, exportConfiguration);

    export = new ArrayList<Export>();
    export.add(new Export(new HashSet<>(diagnosisKeys), exportBatch));
  }

  @Test
  public void checkBuildsTheCorrectDirectoryStructureWhenNoKeys() {
    export = new ArrayList<>();
    Directory<WritableOnDisk> directory = new DiagnosisKeysDirectory(export, cryptoProvider);
    parentDirectory.addWritable(directory);
    directory.prepare(new ImmutableStack<>());
    directory.write();

    String s = File.separator;
    Set<String> expectedFiles = Set.of(
        join(s, "diagnosis-keys", "country", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "index")
    );

    Set<String> actualFiles = getActualFiles(outputFile);

    assertEquals(expectedFiles, actualFiles);
  }

  @Test
  public void checkBuildsTheCorrectDirectoryStructure() {
    Directory<WritableOnDisk> directory = new DiagnosisKeysDirectory(export, cryptoProvider);
    parentDirectory.addWritable(directory);
    directory.prepare(new ImmutableStack<>());
    directory.write();

    String s = File.separator;
    Set<String> expectedFiles = Set.of(
        join(s, "diagnosis-keys", "country", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "0", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "1", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "2", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "3", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "4", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "5", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "6", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "7", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "8", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "9", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "10", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "11", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "12", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "13", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "14", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "15", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "16", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "17", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "18", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "19", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "20", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "21", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "22", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "23", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-02", "hour", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-02", "hour", "0", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-02", "hour", "1", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-02", "hour", "2", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-02", "hour", "3", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-02", "hour", "4", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-02", "hour", "5", "index")
    );

    Set<String> actualFiles = getActualFiles(outputFile);

    assertEquals(expectedFiles, actualFiles);
  }

  private Set<String> getActualFiles(java.io.File root) {
    Set<String> files = Arrays.stream(Objects.requireNonNull(root.listFiles()))
        .filter(File::isFile)
        .map(File::getAbsolutePath)
        .map(path -> path.substring(outputFile.getAbsolutePath().length() + 1))
        .collect(Collectors.toSet());

    Set<java.io.File> directories = Arrays.stream(Objects.requireNonNull(root.listFiles()))
        .filter(File::isDirectory)
        .collect(Collectors.toSet());

    Set<String> subFiles = directories.stream()
        .map(this::getActualFiles)
        .flatMap(Set::stream)
        .collect(Collectors.toSet());

    files.addAll(subFiles);
    return files;
  }
}