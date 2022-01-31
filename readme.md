**Git:**


<table>
  <tr>
   <td>Flag
   </td>
   <td>Long option
   </td>
   <td>Name
   </td>
   <td>Optional (Default)
   </td>
  </tr>
  <tr>
   <td>-g
   </td>
   <td>--git
   </td>
   <td>Github Request/Oauth Token
   </td>
   <td>N/A
   </td>
  </tr>
  <tr>
   <td>-u
   </td>
   <td>--username
   </td>
   <td>Username
   </td>
   <td>No
   </td>
  </tr>
  <tr>
   <td>-r
   </td>
   <td>--repo
   </td>
   <td>Path to repo txt file
   </td>
   <td>No
   </td>
  </tr>
  <tr>
   <td>-b
   </td>
   <td>--branch
   </td>
   <td>Branch
   </td>
   <td>No
   </td>
  </tr>
  <tr>
   <td>-d
   </td>
   <td>--directory
   </td>
   <td>Directory to download to
   </td>
   <td>Yes (Temp dir)*
   </td>
  </tr>
  <tr>
   <td>
   </td>
   <td>--subdirectory
   </td>
   <td>Subdirectory of repo to download
   </td>
   <td>Yes
   </td>
  </tr>
  <tr>
   <td>
   </td>
   <td>--files
   </td>
   <td>Specific files to download
   </td>
   <td>Yes
   </td>
  </tr>
  <tr>
   <td>
   </td>
   <td>--timestamp
   </td>
   <td>Timestamp
   </td>
   <td>Yes (Current time)
   </td>
  </tr>
</table>


_*OS specific, operation could fail, would then require declared directory_

If executing a Git request without an Oauth token include the -g flag but add a space in quotes at the end:

_-g “ “_

Can combine subdirectory and path to list of files params, file path get concatenated to the end of subdirectory param. For example if wanting to pull test.txt from: https://github.com/[username]/[repo]/tree/[branch]/dir1/dir2/dir3/test.txt the correct param would be -subdirectory dir1/dir2 and -files linking to a file with the line dir3/test.txt. Using -subdirectory dir1/dir2 _and _dir1/dir2/dir3/test.txt would result in the API looking for your file at https://github.com/[username]/[repo]/tree/[branch]/dir1/dir2/dir1/dir2/dir3/test.txt 

**MOSS:**


<table>
  <tr>
   <td>Flag
   </td>
   <td>Long option
   </td>
   <td>Name
   </td>
   <td>Optional (Default)
   </td>
  </tr>
  <tr>
   <td>-m
   </td>
   <td>--moss
   </td>
   <td>Moss Request/ID
   </td>
   <td>N/A
   </td>
  </tr>
  <tr>
   <td>-l
   </td>
   <td>--language
   </td>
   <td>Language of files
   </td>
   <td>No
   </td>
  </tr>
  <tr>
   <td>-d
   </td>
   <td>--directory
   </td>
   <td>Directory of student files
   </td>
   <td>No unless preceded by Git request
   </td>
  </tr>
  <tr>
   <td>
   </td>
   <td>--basefiles
   </td>
   <td>Directory of basefiles
   </td>
   <td>Yes
   </td>
  </tr>
</table>


**Codequiry:**


<table>
  <tr>
   <td>Flag
   </td>
   <td>Long option
   </td>
   <td>Name
   </td>
   <td>Optional (Default)
   </td>
  </tr>
  <tr>
   <td>-c
   </td>
   <td>--codequiry
   </td>
   <td>Codequiry Request/API
   </td>
   <td>N/A
   </td>
  </tr>
  <tr>
   <td>-l
   </td>
   <td>--language
   </td>
   <td>Language of files
   </td>
   <td>No
   </td>
  </tr>
  <tr>
   <td>-d
   </td>
   <td>--directory
   </td>
   <td>Directory of student files
   </td>
   <td>No unless preceded by Git request
   </td>
  </tr>
  <tr>
   <td>
   </td>
   <td>--name
   </td>
   <td>Name of request
   </td>
   <td>Yes
   </td>
  </tr>
</table>


For Codequiry ensure that the directory passed corresponds to the immediate parent of all student files/directories. It will treat every subfolder and file as its own student. If you have a folder with 10 student folders and a readme.md it will treat it as having 11 students, with one named “readme”.

The Directory flag is identical for Git, MOSS and Codequiry. It’s inclusion will set the directory for all the operations. Git will download to a temporary directory which MOSS and Codequiry operations will access.

Likewise the language flag is identical for MOSS and Codequiry it’s inclusion will set the language for both operations.

Examples of valid commands include:

-g “ ” -u synewmark-resources -r "C:\Users\ahome\OneDrive\Desktop\Students.txt" --subdirectory StudentCode -b main -c *codequiry_api_key* -l java *codequiry_api_key*

--codequiry *Codequiry API key* -l java -apikey *codequiry_api_key* --directory "C:\Users\ahome\OneDrive\Desktop\StudentDirectories\"

--moss *MOSS API key* --language java --directory "C:\Users\ahome\OneDrive\Desktop\StudentFiles\
