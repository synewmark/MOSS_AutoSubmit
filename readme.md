Params are split for git and moss:

Git arguments are preceded by --g and the following arguments are permitted:


<table>
  <tr>
   <td>Param:
   </td>
   <td>Flags:
   </td>
   <td>Optional [Default]
   </td>
  </tr>
  <tr>
   <td>Host URL
   </td>
   <td>-h, -host
   </td>
   <td>Yes, [https://github.com/]
   </td>
  </tr>
  <tr>
   <td>Repo username
   </td>
   <td>-u, -user, -username
   </td>
   <td>No
   </td>
  </tr>
  <tr>
   <td>Path to repo name file
   </td>
   <td>-r, -repo
   </td>
   <td>No
   </td>
  </tr>
  <tr>
   <td>Subdirectory within repo
   </td>
   <td>-sd, -subdirectory
   </td>
   <td>Yes
   </td>
  </tr>
  <tr>
   <td>Branch
   </td>
   <td>-b, -branch
   </td>
   <td>No
   </td>
  </tr>
  <tr>
   <td>OAuth Token
   </td>
   <td>-o, -oauth, -oauthtoken
   </td>
   <td>Yes
   </td>
  </tr>
  <tr>
   <td>Directory to download to
   </td>
   <td>-d, -dir, -directory
   </td>
   <td>Yes, [temp directory]*
   </td>
  </tr>
  <tr>
   <td>Time Stamp
   </td>
   <td>-t, -time, -timestamp
   </td>
   <td>Yes, [CurrentTime]
   </td>
  </tr>
  <tr>
   <td>Path to list of specific files to download
   </td>
   <td>-f, -files
   </td>
   <td>Yes
   </td>
  </tr>
</table>


*OS specific, operation could fail, would then require declared directory

Text files for path to repo name file require a list of line separated repo names

Text file for path to list of specific files to download requires line separated complete directories starting from the root of the repo (unless combined with the subdirectory param) ending at the specific file. Each node of the directory must be separated by a single forward slash (like in a URL).

Can combine subdirectory and path to list of files params, file path get concatenated to the end of subdirectory param. For example if wanting to pull test.txt from: https://github.com/[username]/[repo]/tree/[branch]/dir1/dir2/dir3/test.txt the correct param would be -subdirectory dir1/dir2 and -files linking to a file with the line dir3/test.txt. Using -subdirectory dir1/dir2 _and_ dir1/dir2/dir3/test.txt would result in the API looking for your file at https://github.com/[username]/[repo]/tree/[branch]/dir1/dir2/dir1/dir2/dir3/test.txt 

Under the hood your files may be downloaded using jGit.Clone or a series of requests to the GitHub traversal API + GitHub RawFile requests. The latter option will download just the files you requested with the subdirectory and specific files params, while the former will temporarily download all the files in the repo before deleting the files not specified. The handling of empty directories both in the GitHub repo and already downloaded directories made empty by the aforementioned delete operations is unspecified due the same being inconsistent by the actual Git semantics. 

Moss arguments are preceded by --m and accept the following arguments 


<table>
  <tr>
   <td>Param: 
   </td>
   <td>Flags: 
   </td>
   <td>Optional:
   </td>
  </tr>
  <tr>
   <td>Language
   </td>
   <td>-l, -language
   </td>
   <td>No
   </td>
  </tr>
  <tr>
   <td>Student File Directory
   </td>
   <td>-sfd, -studentfiledirectory
   </td>
   <td>No, unless preceded by --g call
   </td>
  </tr>
  <tr>
   <td>Base File Directory
   </td>
   <td>-bfd, -basefiledirectory
   </td>
   <td>Yes
   </td>
  </tr>
  <tr>
   <td>Moss ID
   </td>
   <td>-i, -id
   </td>
   <td>No
   </td>
  </tr>
</table>

Codequiry arguments are preceded by --g and accept the following arguments 


<table>
  <tr>
   <td>Param: 
   </td>
   <td>Flags: 
   </td>
   <td>Optional:
   </td>
  </tr>
  <tr>
   <td>Language
   </td>
   <td>-l, -language
   </td>
   <td>No, unless preceded by --m call
   </td>
  </tr>
  <tr>
   <td>Text file that contains list of Student directories
   </td>
   <td>-sd -studentdirectories
   </td>
   <td>No, unless preceded by --g call
   </td>
  </tr>
  <tr>
   <td>Request name
   </td>
   <td>-n, -name
   </td>
   <td>Yes
   </td>
  </tr>
  <tr>
   <td>Codequiry API Key
   </td>
   <td>-key, -apikey
   </td>
   <td>No
   </td>
  </tr>
</table>

Note the difference between MOSS and Codequiry when submitting the student files: MOSS accepts entire directories distinguishing between different students implicitly based on directory structure so, the directory can be the root of **all** student files whereas Codeqiry requires distinct zip files for each student requiring a list of individual students to be passed to AutoSubmit

All params that take a text file must be in the form of text file to a list of new line character separated relative or absolute directories. The file must also have a terminating new line. See \TestResources for valid examples.

Examples of valid commands include 

--g -u synewmark-resources -r "C:\Users\ahome\OneDrive\Desktop\Students.txt" -b studentCode -b main --c -l java -apikey *codequiry_api_key*

--c -l java -apikey *codequiry_api_key* -sd "C:\Users\ahome\OneDrive\Desktop\StudentDirectories.txt"

--m -l java -apikey *MOSS API key* -sfd "C:\Users\ahome\OneDrive\Desktop\StudentFiles\