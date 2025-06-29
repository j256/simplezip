### 2.3 - 2024-07-??
* Added ZipFileInput.readFileDataAll() for reading all of the file data at once.
* Fixed some javadocs.

### 2.2 - 2024-06-19
* Fixed the size specified of the Zip64 extra-field.
* Renamed the code package to be codec which does the encoding/decoding.
* Renamed the ZipFileDataInfo to be ZipDataInfo.
* Added default version-needed field of 4.5 for Zip64 support.
* Added missing ZipFileInput.readRawFileDataToFile() methods.
* Changed ZipFileInput.assignDirectoryFileEntryPermissions() to take a ZipCentralDirectoryFileEntry argument.

### 2.1 - 2024-06-14
* Fixed a problem when reading file headers and getting null twice.
* Fixed a possible problem when the internal buffer sizes are increasing.
* Added more Zip64 support around central-directory entries and the offset and disk-number.
* Improved test coverage which help catch some possible issues that were addressed.

### 2.0 - 2024-06-13
* Fixed problem with buffering that would generate an improper or unnecessary data-descriptor.
* Fixed problem when finishing Zip output file without any data.
* Removed the ZipVersion which seemed artificial.  Replaced with major/minor numbers.
* Renamed some methods around the version-needed and version-made getters and setters.
* Renamed the ZipFileOutput.enableBufferedOutput() method to enableFileBuffering().
* Changed some of the sizes returned to be longs to better handle 32-bit numbers.
* Changed (and added) some fields in the ZipCentralFileEntry to support Zip64 widths.
* Added some more fields to the ZipCentralDirectoryEndInfo class to support Zip64.
* Added some fields to the ZipCentralFileEntry to support Zip64 widths. 
* Added some methods to extract the ZipFileHeader and the ZipCentralDirectoryFileInfo fields from a File.
* Added some support for Zip64 extra fields.
* Added Zip64 central-directory end support and Zip64 end-locator object support.
* Added iterators for the ZipFileHeader and ZipCentralDirectoryFileEntry sections.
* Renamed the ZipFileInput.readFileData(File) methods to be readFileDataToFile().
* Added support for file-header and directory-file-entry iterators.

### 1.1 - 2024/05/23
* Fixed some of the logic to allow us to control all bytes in the output Zip.
* Fixed some of the logic which determined if a data-descriptor was necessary.
* Added ZipCentralDirectoryEndInfo to be able to control the end structure.
* Added ZipFileCopy example program which writes identical Zip files.
* Added some default values to couple of the structures that can be overridden.

### 1.0: 2024/5/22
* Fixed a problem with early stream-closing.  Surprised I didn't catch this earlier.
* Started to add some code examples.

### 0.9: 5/21/2024
* Added auto-skipping of file-data in ZipFileInput if you didn't read any data or didn't read to the end.
* Added initial documentation.  Caused a lot of class renaming.
* Renamed some modified date/time fields in the ZipFileHeader and ZipCentralDirectoryFileEntry.
* Renamed DataDescriptor to ZipDataDescriptor.
* Renamed CentralDirectoryFileHeader to ZipCentralDirectoryFileEntry.
* Renamed CentralDirectoryFileInfo to ZipCentralDirectoryFileInfo.
* Renamed CentralDirectoryEnd to ZipCentralDirectoryEnd.

### 0.8: 5/21/2024
* Renamed ZipFileReader and ZipFileWriter to ZipFileInput and ZipFileOutput.
* Added assigning permissions to files written by ZipFileInput.

### 0.7: 5/20/2024
* Added better support for Unix and MSDOS file permissions.
* Added reading from ZipFileReader and writing to a File directly.
* Added support for writing file-data to the ZipFileWriter via an OutputStream.
* Added support for reading file-data from the ZipFileReader via an InputStream.
* Added with...() method chaining to some of the builder classes.
* Fixed problems with streaming Zip files inside of Zip files because of possibility of reading past the inner one.
* Fixed problems with compression level flags.

### 0.6: 5/16/2024
* Added ZipFileReader.readRaw... and ZipFileWriter.writeRaw... methods for dealing with unencoded data.
* Some method and field renames as things stabilize and test coverage grows.
* Added buffering to the ZipFileWriter which can be enabled to fill out ZipFileHeader before writing file data.

### 0.5: 5/14/2024
* Improved the test coverage and normalized some of the method names in the process.

### 0.4: 5/14/2024
* Fixed sizes and crc which were wrong.  Was tricked by a bad test.  Whoops. 

### 0.3: 5/14/2024
* Increased the ZipFileWriter intelligence significantly.
* Data-descriptor is now written automagically.
* Central-directory file-header and end are now written automagically.

### 0.2: 5/13/2024
* Initial released version mostly just to get the maven repo online.  Not ready for use yet.
* Reader is pretty useful but the writer is very naive.  Missing a ton of logic.
* The extra information bytes right now is a work in progress.

### 0.1: 5/7/2024
* Initial commit.
