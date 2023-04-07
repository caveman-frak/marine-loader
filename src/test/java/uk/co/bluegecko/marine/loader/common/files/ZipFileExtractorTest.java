package uk.co.bluegecko.marine.loader.common.files;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ZipFileExtractorTest {

	private InputStream in;

	@BeforeEach
	void setUp() {
		in = getClass().getClassLoader().getResourceAsStream("data/dummy-data.csv");
	}

	@AfterEach
	void tearDown() throws IOException {
		in.close();
	}

	@Test
	void testFileContents() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

			assertThat(reader.readLine())
					.startsWith("\"id\",")
					.endsWith(",\"country\"");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}