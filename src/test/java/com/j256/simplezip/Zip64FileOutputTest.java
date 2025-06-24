package com.j256.simplezip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Ignore;
import org.junit.Test;

import com.j256.simplezip.format.CompressionMethod;
import com.j256.simplezip.format.GeneralPurposeFlag;
import com.j256.simplezip.format.Zip64CentralDirectoryEnd;
import com.j256.simplezip.format.Zip64CentralDirectoryEndLocator;
import com.j256.simplezip.format.ZipCentralDirectoryEnd;
import com.j256.simplezip.format.ZipCentralDirectoryEndInfo;
import com.j256.simplezip.format.ZipCentralDirectoryFileEntry;
import com.j256.simplezip.format.ZipCentralDirectoryFileInfo;
import com.j256.simplezip.format.ZipFileHeader;
import com.j256.simplezip.format.extra.Zip64ExtraField;

public class Zip64FileOutputTest {

	@Test
	public void testZip64End() throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		output.enableFileBuffering(1024000, 1024000);
		output.writeFileHeader(ZipFileHeader.builder().withFileName("foo.txt").build());
		output.writeFileDataAll(new byte[] { 1, 2, 3 });
		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();
		builder.setVersionMade(2);
		output.addDirectoryFileInfo(builder.build());
		int versionMade = 1221;
		output.finishZip(ZipCentralDirectoryEndInfo.builder().withVersionMade(versionMade).build());
		output.close();

		byte[] zipBytes = baos.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(zipBytes);
		// now try to read it back in with the jdk stuff
		ZipFileInput input = new ZipFileInput(bais);
		assertNotNull(input.readFileHeader());
		input.readFileData(new ByteArrayOutputStream());
		assertNull(input.readFileHeader());
		ZipCentralDirectoryFileEntry entry = input.readDirectoryFileEntry();
		assertNotNull(entry);
		assertNull(input.readDirectoryFileEntry());
		Zip64CentralDirectoryEnd zip64End = input.readZip64DirectoryEnd();
		assertNotNull(zip64End);
		assertEquals(versionMade, zip64End.getVersionMade());
		Zip64CentralDirectoryEndLocator zip64EndLocator = input.readZip64DirectoryEndLocator();
		assertNotNull(zip64EndLocator);
		ZipCentralDirectoryEnd end = input.readDirectoryEnd();
		assertNotNull(end);
		input.close();
	}

	@Test
	public void testZip64Extra() throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String name = "foo.txt";
		byte[] bytes = new byte[] { 1, 2, 3 };
		try (ZipFileOutput output = new ZipFileOutput(baos);) {
			output.enableFileBuffering(1024000, 1024000);
			Zip64ExtraField zip64Entry = Zip64ExtraField.builder()
					.withUncompressedSize(bytes.length)
					.withCompressedSize(bytes.length)
					.build();
			output.writeFileHeader(ZipFileHeader.builder()
					.withFileName(name)
					.withCompressionMethod(CompressionMethod.STORED)
					.withCompressedSize(-1)
					.withUncompressedSize(-1)
					.withZip64ExtraField(zip64Entry)
					.build());
			output.writeFileDataAll(new byte[] { 1, 2, 3 });
			ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();
			builder.setVersionMade(2);
			output.addDirectoryFileInfo(builder.build());
			int versionMade = 1221;
			output.finishZip(ZipCentralDirectoryEndInfo.builder().withVersionMade(versionMade).build());
		}

		/*
		 * Now try to read it back in with the jdk stuff. I'm not 100% sure that this actually tests the zip64 directory
		 * entries which may be be obeyed at all.
		 */
		try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
				ZipInputStream zis = new ZipInputStream(bais);) {
			ZipEntry entry = zis.getNextEntry();
			assertNotNull(entry);
			assertEquals(name, entry.getName());
			byte[] buf = new byte[1024];
			int numRead = zis.read(buf);
			assertEquals(bytes.length, numRead);
			assertNull(zis.getNextEntry());
		}
	}

	@Ignore
	@Test
	public void testZip64BigWrite() throws IOException, InterruptedException {

		// create our blocking stream that works with the input stream
		BlockingOutputStream bos = new BlockingOutputStream();
		// fork our thread
		ZipReader zipReader = new ZipReader(bos.getInputStream());
		Thread thread = new Thread(zipReader, "zipReader");
		thread.start();

		ZipFileOutput zipOutput = new ZipFileOutput(bos);
		String fileName = "foo.txt3";
		byte[] buf = new byte[4096000];
		int times = 1000;
		zipOutput.writeFileHeader(ZipFileHeader.builder()
				.withFileName(fileName)
				.withGeneralPurposeFlag(GeneralPurposeFlag.DEFLATING_SUPER_FAST)
				.build());
		Random random = new Random();
		for (int i = 0; i < times; i++) {
			random.nextBytes(buf);
			System.out.print(i + " ");
			zipOutput.writeFileDataPart(buf);
		}
		System.out.println();
		long offset = zipOutput.finishFileData();
		assertTrue(offset > Integer.MAX_VALUE);
		zipOutput.close();

		// wait for the ZipInputStream to read everything
		thread.join();
		List<ZipEntry> entries = zipReader.entries;
		assertTrue(!entries.isEmpty());
		assertEquals(fileName, entries.get(0).getName());
		assertNull(zipReader.exception);
	}

	@Test
	public void testZip64BigWriteZeros() throws IOException, InterruptedException {

		// create our blocking stream that works with the input stream
		BlockingOutputStream bos = new BlockingOutputStream();
		// fork our thread
		ZipReader zipReader = new ZipReader(bos.getInputStream());
		Thread thread = new Thread(zipReader, "zipReader");
		thread.start();

		ZipFileOutput zipOutput = new ZipFileOutput(bos);
		String fileName = "foo.txt3";
		byte[] buf = new byte[4096000];
		zipOutput.writeFileHeader(ZipFileHeader.builder().withFileName(fileName).build());
		long size = 0;
		for (int i = 0; size <= 0xFFFFFFFFL; i++) {
			System.out.print(i + " ");
			zipOutput.writeFileDataPart(buf);
			size += buf.length;
		}
		System.out.println();
		zipOutput.finishFileData();
		zipOutput.close();

		// wait for the ZipInputStream to read everything
		thread.join();
		List<ZipEntry> entries = zipReader.entries;
		assertTrue(!entries.isEmpty());
		assertEquals(fileName, entries.get(0).getName());
		assertNull(zipReader.exception);
	}

	public static void main(String[] args) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		output.enableFileBuffering(1024000, 1024000);
		output.writeFileHeader(
				ZipFileHeader.builder().withFileName("foo.txt").withVersionNeededMajorMinor(4, 5).build());
		output.writeFileDataAll(new byte[] { 1, 2, 3 });
		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();
		builder.setVersionMade(2);
		output.addDirectoryFileInfo(builder.build());
		int versionMade = 1221;
		output.finishZip(ZipCentralDirectoryEndInfo.builder().withVersionMade(versionMade).build());
		output.close();

		byte[] zipBytes = baos.toByteArray();
		String path = "target/small.zip64";
		FileOutputStream fos = new FileOutputStream(path);
		fos.write(zipBytes);
		fos.close();
		System.out.println("wrote " + path);

		/*
		 * Large.
		 */

		ZipFileOutput zipOutput = new ZipFileOutput("target/large.zip64");
		byte[] buf = new byte[4096000];
		zipOutput.writeFileHeader(ZipFileHeader.builder()
				.withFileName("foo.txt")
				.withGeneralPurposeFlag(GeneralPurposeFlag.DEFLATING_SUPER_FAST)
				.build());
		Random random = new Random();
		long size = 0;
		for (int i = 0; size <= 0xFFFFFFFFL; i++) {
			random.nextBytes(buf);
			System.out.print(i + " ");
			zipOutput.writeFileDataPart(buf);
			size += buf.length;
		}
		System.out.println();
		long offset = zipOutput.finishFileData();
		assertTrue(offset > Integer.MAX_VALUE);
		zipOutput.writeFileHeader(ZipFileHeader.builder()
				.withFileName("foo2.txt")
				.withGeneralPurposeFlag(GeneralPurposeFlag.DEFLATING_SUPER_FAST)
				.build());
		zipOutput.writeFileDataAll("wow lookie another file".getBytes());
		zipOutput.close();
	}

	/**
	 * Designed to run in a thread to process our zip output.
	 */
	public class ZipReader implements Runnable {

		private final ZipInputStream zis;
		private final List<ZipEntry> entries = new ArrayList<>();
		private Exception exception;

		public ZipReader(InputStream inputStream) {
			this.zis = new ZipInputStream(inputStream);
		}

		@Override
		public void run() {
			try {
				doRun();
			} catch (IOException ioe) {
				exception = ioe;
			}
		}

		public List<ZipEntry> getEntries() {
			return entries;
		}

		private void doRun() throws IOException {
			byte[] buffer = new byte[4096];
			while (true) {
				ZipEntry entry = zis.getNextEntry();
				if (entry == null) {
					break;
				}
				entries.add(entry);
				while (true) {
					if (zis.read(buffer) < 0) {
						break;
					}
				}
			}
		}
	}

	/**
	 * Output stream that is linked to the input stream.
	 */
	public class BlockingOutputStream extends OutputStream {

		private final byte[] EOF_INTS = new byte[0];

		private final BlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(100);
		private final InputStream inputStream = new BlockingInputStream();
		private final byte[] buffer = new byte[1024];
		private int bufferOffset;

		@Override
		public void write(int b) {
			buffer[bufferOffset++] = (byte) b;
			if (bufferOffset >= buffer.length) {
				flush();
			}
		}

		@Override
		public void write(byte[] from, int fromOffset, int fromLength) {
			while (fromLength > 0) {
				int writeLen = Math.min(buffer.length - bufferOffset, fromLength);
				System.arraycopy(from, fromOffset, buffer, bufferOffset, writeLen);
				fromOffset += writeLen;
				fromLength -= writeLen;
				bufferOffset += writeLen;
				if (bufferOffset >= buffer.length) {
					flush();
					// if what we have left is larger than the buffer size then just put the rest directly
					if (fromLength >= buffer.length) {
						byte[] bytes = Arrays.copyOfRange(from, fromOffset, fromOffset + fromLength);
						putBuffer(bytes);
						return;
					}
				}
			}
		}

		@Override
		public void flush() {
			if (bufferOffset > 0) {
				byte[] bytes = Arrays.copyOf(buffer, bufferOffset);
				putBuffer(bytes);
				bufferOffset = 0;
			}
		}

		@Override
		public void close() {
			flush();
			putBuffer(EOF_INTS);
		}

		public InputStream getInputStream() {
			return inputStream;
		}

		private void putBuffer(byte[] bytes) {
			try {
				queue.put(bytes);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}

		/**
		 * Input stream which reads from the queued byte buffers.
		 */
		private class BlockingInputStream extends InputStream {

			private byte[] readBuffer;
			private int readOffset;
			private boolean eof;

			@Override
			public int read() {
				if (maybeGetMoreData()) {
					int val = (int) (readBuffer[readOffset++] & 0xFF);
					return val;
				} else {
					return -1;
				}
			}

			@Override
			public int read(byte[] buf, int offset, int length) {
				if (!maybeGetMoreData()) {
					return -1;
				}
				int readLength = Math.min(length, readBuffer.length - readOffset);
				System.arraycopy(readBuffer, readOffset, buf, offset, readLength);
				readOffset += readLength;
				return readLength;
			}

			private boolean maybeGetMoreData() {
				if (eof) {
					return false;
				}
				while (readBuffer == null || readOffset >= readBuffer.length) {
					try {
						readBuffer = queue.take();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return false;
					}
					if (readBuffer == EOF_INTS) {
						eof = true;
						return false;
					}
					readOffset = 0;
				}
				return true;
			}
		}
	}
}
