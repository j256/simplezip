Simple Java Zip
===============

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.j256.simplezip/simplezip/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.j256.simplezip/simplezip/)
[![javadoc](https://javadoc.io/badge2/com.j256.simplezip/simplezip/javadoc.svg)](https://javadoc.io/doc/com.j256.simplezip/simplezip)
[![ChangeLog](https://img.shields.io/github/v/release/j256/simplezip?label=changelog&display_name=release)](https://github.com/j256/simplezip/blob/master/src/main/javadoc/doc-files/changelog.txt)
[![CodeCov](https://img.shields.io/codecov/c/github/j256/simplezip.svg)](https://codecov.io/github/j256/simplezip/)
[![CircleCI](https://circleci.com/gh/j256/simplezip.svg?style=shield)](https://circleci.com/gh/j256/simplezip)
[![GitHub License](https://img.shields.io/github/license/j256/simplezip)](https://github.com/j256/simplezip/blob/master/LICENSE.txt)

This package provides Java classes to read and write Zip files.  There are a number of different libraries that do
this (including one built into the JDK) but I've not found any that gave me precise controls over the Zip internal, persisted data structures.  This library allows you to control the output of all Zip data and should allow you to
read and write Zip files with full precision.

Enjoy.  Gray Watson

# Getting Started

## Reading a Zip File

The following code runs through all of the Zip-file parts.  `input` could be a file path,
`File`, or an `InputStream`.

	ZipFileInput zipInput = new ZipFileInput(input);
	// readFileHeader() will return null when no more files to read
	ZipFileHeader header = zipInput.readFileHeader();
	byte[] buffer = new byte[4096];
	// read into buffers or via InputStream until it returns -1
	long numRead = zipInput.readFileDataPart(buffer);
	...
	// can also call readFileData(File) to write out a file from input
	// NOTE: descriptor can be null if none in the zip
	ZipDataDescriptor dataDescriptor = zipInput.getCurrentDataDescriptor();
	// repeat reading file headers and data until readFileHeader() returns null
	// read in the optional central-directory file-headers, null when no more
	ZipCentralDirectoryFileEntry dirEntry = zipInput.readDirectoryFileEntry();
	// read in the optional central-directory end data
	ZipCentralDirectoryEnd end = zipInput.readDirectoryEnd();
	zipInput.close();

## Writing a Zip File

The following code writes out a Zip-file.  `output` could be a file path, `File`,
or `OutputStream`.

	ZipFileOutput zipOutput = new ZipFileOutput(output);
	ZipFileHeader header = ZipFileHeader.builder()
		.withFileName("hello.txt")
		.withGeneralPurposeFlags(GeneralPurposeFlag.DEFLATING_MAXIMUM)
		.build();
	// write a file-header to the zip-file
	zipOutput.writeFileHeader(header);
	// add optional central-directory info to the file such as text flag
	// this will be written to disk at the end of the zip
	zipOutput.addDirectoryFileInfo(
		ZipCentralDirectoryFileInfo.builder().withTextFile(true).build());
	// write file data from file, buffer, or InputStream
	zipOutput.writeFileDataPart(fileBytes);
	...
	// must be called after all file parts written
	zipOutput.finishFileData();
	// can write more file-headers and data here
	...
	// this writes the recorded central-directory entries, end, and closes tbe zip
	zipOutput.close();

# Maven Configuration

Maven packages are published via [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.j256.simplezip/simplezip/badge.svg?style=flat-square)](https://mvnrepository.com/artifact/com.j256.simplezip/simplezip/latest)

``` xml
<dependency>
	<groupId>com.j256.simplezip</groupId>
	<artifactId>simplezip</artifactId>
	<version>2.2</version>
</dependency>
```

# Dependencies

SimpleZip has no runtime dependencies.

# ChangeLog Release Notes

See the [![ChangeLog](https://img.shields.io/github/v/release/j256/simplezip?label=changelog)](https://github.com/j256/simplezip/blob/master/src/main/javadoc/doc-files/changelog.txt)

# References Used to Write this Library

Here are some of the reference documents that were used to write this library.

* [PKWare ZIP File Format Specification](https://pkwaredownloads.blob.core.windows.net/pem/APPNOTE.txt)
* [Wikipedia Zip file format](https://en.wikipedia.org/wiki/ZIP_(file_format))
* [DOS/Windows file attributes](http://justsolve.archiveteam.org/wiki/DOS/Windows_file_attributes)
* [Zip format's external file attribute](https://unix.stackexchange.com/questions/14705/the-zip-formats-external-file-attribute)
* [Structure of a PKZip file](https://users.cs.jmu.edu/buchhofp/forensics/formats/pkzip.html)
* [Known types of Zipfile extra fields](https://libzip.org/specifications/extrafld.txt)
