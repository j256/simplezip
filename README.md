Simple Java Zip
===============

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.j256.simplezip/simplezip/badge.svg?style=flat-square)](https://mvnrepository.com/artifact/com.j256.simplezip/simplezip/latest)
[![javadoc](https://javadoc.io/badge2/com.j256.simplezip/simplezip/javadoc.svg)](https://javadoc.io/doc/com.j256.simplezip/simplezip)
[![ChangeLog](https://img.shields.io/github/v/release/j256/simplezip?label=changelog&display_name=release)](https://github.com/j256/simplezip/blob/master/src/main/javadoc/doc-files/changelog.txt)
[![Documentation](https://img.shields.io/github/v/release/j256/simplezip?label=documentation&display_name=release)](https://htmlpreview.github.io/?https://github.com/j256/simplezip/blob/master/src/main/javadoc/doc-files/simplezip.html)
[![CodeCov](https://img.shields.io/codecov/c/github/j256/simplezip.svg)](https://codecov.io/github/j256/simplezip/)
[![CircleCI](https://circleci.com/gh/j256/simplezip.svg?style=shield)](https://circleci.com/gh/j256/simplezip)
[![GitHub License](https://img.shields.io/github/license/j256/simplezip)](https://github.com/j256/simplezip/blob/master/LICENSE.txt)

This package provides Java classes to read and write Zip files.  There are a number of different libraries that do
this (including one built into the JDK) but I've not found any that gave me precise controls over the Zip file and
directory entries.

Enjoy.  Gray Watson

# Getting Started

## Reading a Zip Files

Here's some simple code that runs through all of the Zip-file parts.

	ZipFileReader zipFile = new ZipFileReader(input);
	// readFileHeader() will return null when no more files
	ZipFileHeader header = zipFile.readFileHeader();
	byte[] buffer = new byte[8192];
	// read into buffers or via InputStream
	long numRead = zipFile.readFileData(buffer);
	...
	// NOTE: descriptor can be null
	DataDescriptor dataDescriptor = zipFile.getCurrentDataDescriptor();
	// read in the central-directory file-headers, null when no more
	CentralDirectoryFileHeader dirHeader = zipFile.readDirectoryFileHeader();
	CentralDirectoryEnd end = zipFile.readDirectoryEnd();

## Writing a Zip File

Here's some equally simple code that allows you to write out a Zip-file.

	ZipFileWriter zipWriter = new ZipFileWriter(baos);
	ZipFileHeader.Builder fileBuilder = ZipFileHeader.builder();
	fileBuilder.setGeneralPurposeFlags(
		GeneralPurposeFlag.DEFLATING_NORMAL, GeneralPurposeFlag.DATA_DESCRIPTOR);
	fileBuilder.setLastModifiedDateTime(LocalDateTime.now());
	fileBuilder.setFileName("hello.txt");
	// write a file-header to the zip-file
	zipWriter.writeFileHeader(fileBuilder.build());
	// can add additional central-directory info to the file
	zipWriter.addDirectoryFileInfo(fileInfo);
	// write file data from buffer or InputStream
	zipWriter.writeFileDataPart(fileBytes);
	...
	// must be called after all parts written
	zipWriter.finishFileData();
	// can write more file-headers and data here
	...
	// this writes the central-directory data
	zipWriter.finishZip();
	zipWriter.close();

# Maven Configuration

Maven packages are published via [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.j256.simplezip/simplezip/badge.svg?style=flat-square)](https://mvnrepository.com/artifact/com.j256.simplezip/simplezip/latest)

``` xml
<dependency>
	<groupId>com.j256.simplezip</groupId>
	<artifactId>simplezip</artifactId>
	<version>0.4</version>
</dependency>
```

# Dependencies

Simplezip has no direct dependencies.

# ChangeLog Release Notes

See the [![ChangeLog](https://img.shields.io/github/v/release/j256/simplezip?label=changelog)](https://github.com/j256/simplezip/blob/master/src/main/javadoc/doc-files/changelog.txt)
