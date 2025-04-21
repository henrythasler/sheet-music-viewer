# sheet-music-viewer

Show [MEI](https://music-encoding.org/about/)- or MusicXML-Files rendered with [Verovio](https://www.verovio.org/index.xhtml) in an Android App.

## Build Instructions

1. Install [Ninja](https://ninja-build.org/) : `sudo apt install ninja-build`
2. Clone Verovio library submodule: `git submodule update --init`
3. Generate git-commit header: `(cd app/src/main/cpp/verovio/tools && ./get_git_commit.sh)`
4. Open and build with Android Studio