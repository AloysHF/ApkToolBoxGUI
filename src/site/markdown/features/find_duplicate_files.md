## Find Duplicate Files

The duplicate file finder helps you identify and manage duplicate files efficiently.

### Features

* **Smart Detection**
  * Pre-filters files by size before detailed comparison
  * Multi-threaded processing for better performance
  * Progress tracking with cancellation support
  
* **Flexible Comparison Methods**
  * File size (always enabled)
  * File name
  * MD5 hash
  * Last modified time
  
* **Search Options**
  * Recursive directory scanning
  * Hidden files inclusion/exclusion
  * File extension filtering
  
* **File Management**
  * View file location
  * Delete individual duplicate files
  * Delete duplicates in same directory
  * Delete duplicates recursively in subdirectories

### Usage

1. **Add Files/Directories**
   * Click "Add Files" or "Add Directory" to select files/folders to scan
   * Use "Remove" to remove items from the list

2. **Configure Search**
   * Select comparison methods (Size is always enabled)
   * Choose search options (recursive, hidden files)
   * Optionally specify file extensions (e.g. "java,xml")

3. **Start Search**
   * Click "Search" to begin
   * Progress bar shows current status
   * Use "Cancel" to stop the search

4. **View Results**
   * Results are grouped by duplicate sets
   * Each group shows:
     * File location
     * File name
     * Extension
     * Size
     * Last modified time
   * Right-click menu for file operations

### Screenshots

![DuplicateFile](https://raw.githubusercontent.com/wiki/AloysHF/ApkToolBoxGUI/DuplicateFile_01.png)
![DuplicateFile](https://raw.githubusercontent.com/wiki/AloysHF/ApkToolBoxGUI/DuplicateFile_02.png)

### Performance Tips

1. Start with size comparison only (fastest)
2. Add filename comparison for better accuracy
3. Use MD5 only when absolute certainty is needed
4. Specify file extensions to reduce search scope
5. Use recursive search carefully on large directory trees
