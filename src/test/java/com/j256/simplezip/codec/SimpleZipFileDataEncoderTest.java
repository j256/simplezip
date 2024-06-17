package com.j256.simplezip.codec;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import com.j256.simplezip.ZipFileInput;
import com.j256.simplezip.ZipFileOutput;
import com.j256.simplezip.format.CompressionMethod;
import com.j256.simplezip.format.ZipFileHeader;

public class SimpleZipFileDataEncoderTest {

	@Test
	public void testStuff() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SimpleZipFileDataEncoder encoder = new SimpleZipFileDataEncoder(baos);
		Random random = new Random();
		int numWrites = 1000 + random.nextInt(1000);
		ByteArrayOutputStream expected = new ByteArrayOutputStream();
		byte[] buf = new byte[10240];
		long totalBytes = 0;
		for (int i = 0; i < numWrites; i++) {
			random.nextBytes(buf);
			int size = random.nextInt(buf.length);
			encoder.encode(buf, 0, size);
			expected.write(buf, 0, size);
			totalBytes += size;
		}
		encoder.encode(buf, 0, 0);
		encoder.close();
		assertTrue(encoder.getBytesWritten() > totalBytes);

		SimpleZipFileDataDecoder decoder = new SimpleZipFileDataDecoder(new ByteArrayInputStream(baos.toByteArray()));
		ByteArrayOutputStream results = new ByteArrayOutputStream();
		buf = new byte[buf.length / 3];
		while (true) {
			int size = decoder.decode(buf, 0, buf.length);
			if (size < 0) {
				break;
			}
			results.write(buf, 0, size);
		}
		assertEquals(-1, decoder.decode(buf, 0, buf.length));
		decoder.close();
		byte[] expectedBytes = expected.toByteArray();
		assertArrayEquals(expectedBytes, results.toByteArray());
		assertEquals(expectedBytes.length, decoder.getBytesWritten());
		assertEquals(encoder.getBytesWritten(), decoder.getBytesRead());
	}

	@Test(expected = EOFException.class)
	public void testTruncated() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SimpleZipFileDataEncoder encoder = new SimpleZipFileDataEncoder(baos);
		byte[] buf = new byte[1024];
		encoder.encode(buf, 0, buf.length);
		encoder.close();

		byte[] outputBytes = baos.toByteArray();
		byte[] truncatedBytes = Arrays.copyOf(outputBytes, outputBytes.length - 100);

		SimpleZipFileDataDecoder decoder = new SimpleZipFileDataDecoder(new ByteArrayInputStream(truncatedBytes));
		while (true) {
			if (decoder.decode(buf, 0, buf.length) < 0) {
				break;
			}
		}
		decoder.close();
	}

	@Test
	public void testZipIO() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput zipOutput = new ZipFileOutput(baos);
		byte[] buf = new byte[1024];
		new Random().nextBytes(buf);
		zipOutput.writeFileHeader(ZipFileHeader.builder()
				.withFileName("foo21.bin")
				.withCompressionMethod(CompressionMethod.SIMPLEZIP)
				.build());
		zipOutput.writeFileDataAll(buf);
		zipOutput.close();

		ZipFileInput zipInput = new ZipFileInput(new ByteArrayInputStream(baos.toByteArray()));
		ZipFileHeader header = zipInput.readFileHeader();
		assertNotNull(header);
		assertEquals(CompressionMethod.SIMPLEZIP, header.getCompressionMethodAsEnum());
		assertNull(zipInput.readFileHeader());
		zipInput.close();
	}
}
