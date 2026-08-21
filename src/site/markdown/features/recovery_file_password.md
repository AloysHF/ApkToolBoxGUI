## Recovery File Password

### WARNING

You can use this function only when you have the express authorization of opening and operating the encrypted file.

I am only providing a generic tool, you are the one who decides what to do with it and you should take the responsibility of using it!

### Feature

* Support password recovery from `PDF/ZIP/RAR/7Z...` files
* Support password recovery from Office files like: `doc/docx/ppt/pptx/xls/xlsx`
* Multi-thread processing, high recovery efficiency
* Support progress showing
* Support brute force recovery and dictionary recovery (more methods will be supported in the future)
* Brute force recovery supports customized character set and password length
* Dictionary recovery supports customized dictionaries

### Usage

#### Open the window

Open the window by click menu: `File->Recover File Password`

![Recover_Password](https://raw.githubusercontent.com/wiki/AloysHF/ApkToolBoxGUI/Recover_Password_01.png)

#### Select the `Checker`
   
A checker is the tool to attempt each possible password. Different file type needs different checker.

* `Archive Checker(Using 7z.exe)`: Support common archive files like: `7z/rar/zip/...`. It needs to config the path of `7z.exe` by `Configuration->Confinguration`.
* `Archive Checker(Using WinRar.exe)`: Support almost all archive files, but it's checking speed is slower. It needs to config the path of `WinRAR.exe` by `Configuration->Confinguration`.
* `RAR Checker(Using Rar.exe)`: Support `rar` only, but it's checking speed is quicker. It needs to config the path of `Rar.exe` by `Configuration->Confinguration`.
* `ZIP Checker(Not support Non-ASCII password)`: Support `zip` only. It's checking speed is quicker but can not check password with Non-ASCII character. It doesn't need to config anything.
* `RAR Checker(Not support RAR5+)`: Support `rar` with before `RAR5+`. It's checking speed is quicker, and it doesn't need to config anything.
* `7Zip Checker`: Support `7z` only. It's checking speed is quicker, and it doesn't need to config anything.
* `PDF Checker`: Support `pdf` only. It doesn't need to config anything.
* `Office File Checker(XML-based formats)`: Support `XML-based` office formats like: docx/pptx/xlsx.
* `Office File Checker(Binary formats)`: Support `Binary-based` office formats like: doc/ppt/xls.

![Recover_Password](https://raw.githubusercontent.com/wiki/AloysHF/ApkToolBoxGUI/Recover_Password_02.png)

WARNING: Some checkers need to config the path of tool!

![Recover_Password](https://raw.githubusercontent.com/wiki/AloysHF/ApkToolBoxGUI/Recover_Password_03.png)

#### Select the file which need to be recovered

Select the file which need to be recovered, the file format is need to be matched with the chosen checker.

![Recover_Password](https://raw.githubusercontent.com/wiki/AloysHF/ApkToolBoxGUI/Recover_Password_04.png)

#### Select the type of generating possible passwords

There are two types of generating possible passwords: brute-force and dictionary. The latter is more efficient, but you need select one password dictionary. 
`ApkToolBoxGUI` does not provide any dictionary for legal reasons. However, it is quite easy to find and download the dictionary on Internet.

If you select dictionary:

* Select on password dictionary
* Set thread number depend on your PC power

![Recover_Password](https://raw.githubusercontent.com/wiki/AloysHF/ApkToolBoxGUI/Recover_Password_05.png)

If you select brute-force:

* Select/Unselect the character type of possible password
* Set the length of the possible password

![Recover_Password](https://raw.githubusercontent.com/wiki/AloysHF/ApkToolBoxGUI/Recover_Password_06.png)

#### Start the recovery

Click the `Start` button and wait the result(Found or Not Found). It will show a dialog whatever the result is.

![Recover_Password](https://raw.githubusercontent.com/wiki/AloysHF/ApkToolBoxGUI/Recover_Password_07.png)
