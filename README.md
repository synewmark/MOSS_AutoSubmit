# MOSS_AutoSubmit
Params are split for git and moss:

Git arguments are preceded by --g and the following arguments are permitted:

+--------------------------+--------------------------+--------------------------+ 
| Param:                   | Flags:                   | Optional [Default]       | 
+--------------------------+--------------------------+--------------------------+ 
| Host URL                 | -h, -host                | Yes,                     |
|                          |                          | [https://github.com/]    |
+--------------------------+--------------------------+--------------------------+
| Repo username            | -u, -user, -username     | No                       |
+--------------------------+--------------------------+--------------------------+
| Path to repo name file   | -r, -repo                | No                       |
+--------------------------+--------------------------+--------------------------+
| Subdirectory within repo | -sd, -subdirectory       | Yes                      |
+--------------------------+--------------------------+--------------------------+
| Branch                   | -b, -branch              | No                       |
+--------------------------+--------------------------+--------------------------+
| OAuth Token              | -o, -oauth, -oauthtoken  | Yes                      |
+--------------------------+--------------------------+--------------------------+
| Directory to download to | -d, -dir, -directory     | Yes, [temp directory]\*  |
+--------------------------+--------------------------+--------------------------+
| Time Stamp               | -t, -time, -timestamp    | Yes, [CurrentTime]       |
+--------------------------+--------------------------+--------------------------+
| Path to list of specific | -f, -files               | Yes                      |
| files to download        |                          |                          |
+--------------------------+--------------------------+--------------------------+

\*OS specific, operation could fail, would then require declared
directory

Text files for path to repo name file require a list of line separated repo names

Text file for path to list of specific files to download requires line separated complete directories starting from the root of the repo (unless combined with the subdirectory param) ending at the specific file. Each node of the directory must be separated by a single forward slash (like in a URL).

Can combine subdirectory and path to list of files params, file path get concatenated to the end of subdirectory param. For example if wanting to pull test.txt from: https://github.com/[username]/[repo]/tree/[branch]/dir1/dir2/dir3 test.txt the correct param would be -subdirectory dir1/dir2 and -files linking to a file with the line dir3/test.txt. Using -subdirectory dir1/dir2 andÂ dir1/dir2/dir3/test.txt would result in the API looking for your file at https://github.com/[username]/[repo]/tree/[branch]/dir1/dir2/dir1/dir2/dir3/test.txt

Under the hood your files may be downloaded using jGit.Clone or a series of requests to the GitHub traversal API + GitHub RawFile requests. The latter option will download just the files you requested with the subdirectory and specific files params, while the former will temporarily download all the files in the repo before deleting the files not specified. The handling of empty directories both in the GitHub repo and already downloaded directories made empty by the aforementioned delete operations is unspecified due the same being inconsistent by the actual Git semantics.

* * * * *

Moss arguments are preceded by --m and accept the following arguments

+--------------------------+--------------------------+--------------------------+
| Param:                   | Flags:                   | Optional:                |
+--------------------------+--------------------------+--------------------------+
| Language                 | -l, -language            | No                       |
+--------------------------+--------------------------+--------------------------+
| Student File Directory   | -sfd,                    | No, unless preceded by   |
|                          | -studentfiledirectory    | --g call                 |
+--------------------------+--------------------------+--------------------------+
| Base File Directory      | -bfd, -basefiledirectory | Yes                      |
+--------------------------+--------------------------+--------------------------+
| Moss ID                  | -i, -id                  | No                       |
+--------------------------+--------------------------+--------------------------+
