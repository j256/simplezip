Simple Java Zip
===============

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.j256.simplezip/simplezip/badge.svg?style=flat-square)](https://mvnrepository.com/artifact/com.j256.simplezip/simplezip/latest)
[![javadoc](https://javadoc.io/badge2/com.j256.simplezip/simplezip/javadoc.svg)](https://javadoc.io/doc/com.j256.simplezip/simplezip)
[![ChangeLog](https://img.shields.io/github/v/release/j256/simplezip?label=changelog&display_name=release)](https://github.com/j256/simplezip/blob/master/src/main/javadoc/doc-files/changelog.txt)
[![CodeCov](https://img.shields.io/codecov/c/github/j256/simplezip.svg)](https://codecov.io/github/j256/simplezip/)
[![CircleCI](https://circleci.com/gh/j256/simplezip.svg?style=shield)](https://circleci.com/gh/j256/simplezip)
[![GitHub License](https://img.shields.io/github/license/j256/simplezip)](https://github.com/j256/simplezip/blob/master/LICENSE.txt)

This package provides Java classes to read and write Zip files.  There are a number of different libraries that do
this (including one built into the JDK) but I've not found any that gave me precise controls over the Zip file and
directory entries.  This library allows you to control the output in all Zip fields which should allow you to read
and re-write Zip files duplicating all Zip data structures.

Enjoy.  Gray Watson

# Getting Started

## Reading a Zip Files

Here's some simple code that runs through all of the Zip-file parts.  `input` could be a
`File`, file path, or an `InputStream`.

	ZipFileInput zipInput = new ZipFileInput(input);
	// readFileHeader() will return null when no more files
	ZipFileHeader header = zipInput.readFileHeader();
	byte[] buffer = new byte[4096];
	// read into buffers or via InputStream
	long numRead = zipInput.readFileDataPart(buffer);
	...
	// NOTE: descriptor can be null
	DataDescriptor dataDescriptor = zipInput.getCurrentDataDescriptor();
	// read in the central-directory file-headers, null when no more
	CentralDirectoryFileHeader dirHeader = zipInput.readDirectoryFileHeader();
	CentralDirectoryEnd end = zipInput.readDirectoryEnd();
	zipInput.close();

## Writing a Zip File

Here's some equally simple code that allows you to write out a Zip-file.  `output` could be a
`File`, file path, or `OutputStream`.

	ZipFileOutput zipOutput = new ZipFileOutput(output);
	ZipFileHeader header = ZipFileHeader.builder()
		.withFileName("hello.txt")
		.withGeneralPurposeFlags(GeneralPurposeFlag.DEFLATING_MAXIMUM)
		.withLastModifiedDateTime(LocalDateTime.now())
		.build();
	// write a file-header to the zip-file
	zipOutput.writeFileHeader(header);
	// add option central-directory info to the file such as text flag
	zipOutput.addDirectoryFileInfo(
		CentralDirectoryFileInfo.builder().withTextFile(true).build());
	// write file data from file, buffer, or InputStream
	zipOutput.writeFileDataPart(fileBytes);
	...
	// must be called after all file parts written
	zipOutput.finishFileData();
	// can write more file-headers and data here
	...
	// this writes the recorded central-directory data and close zip
	zipOutput.close();

# Maven Configuration

Maven packages are published via [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.j256.simplezip/simplezip/badge.svg?style=flat-square)](https://mvnrepository.com/artifact/com.j256.simplezip/simplezip/latest)

``` xml
<dependency>
	<groupId>com.j256.simplezip</groupId>
	<artifactId>simplezip</artifactId>
	<version>0.8</version>
</dependency>
```

# Dependencies

Simplezip has no direct dependencies.

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
