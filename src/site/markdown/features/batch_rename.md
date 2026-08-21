## Batch Rename

### Snapshot

![DuplicateFile](https://raw.githubusercontent.com/wiki/AloysHF/ApkToolBoxGUI/BatchRename.png)

### Example

```bash
# Delete some special strings from filename
# original filenames
dir
  dir1/11---aaa[www.xxx.com].mp4
  dir1/12---bbb[www.xxx.com].mp4
  dir2/21---ddd[www.xxx.com].mp4
  dir2/22---eee[www.xxx.com].mp4
# target filenames
  dir1/11aaa.mp4
  dir1/12bbb.mp4
  dir2/21ddd.mp4
  dir2/22eee.mp4
# Use replace rule
replace "---" with ""
replace "[www.xxx.com]" with ""
```

```bash
# Add prefix with directory name
# original filenames
dir
  dir1/test01.pdf
  dir1/test02.pdf
  dir2/test01.pdf
  dir2/test02.pdf
# target filenames
  dir1/dir1_test01.pdf
  dir1/dir1_test02.pdf
  dir2/dir2_test01.pdf
  dir2/dir2_test02.pdf
# Use add rule
add prefix "_"
add prefix with directory name
```