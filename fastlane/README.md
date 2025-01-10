fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

### checkGitStatus

```sh
[bundle exec] fastlane checkGitStatus
```

Check Git Status

### setup

```sh
[bundle exec] fastlane setup
```

Setup

### build_and_publish

```sh
[bundle exec] fastlane build_and_publish
```



### build_flavor

```sh
[bundle exec] fastlane build_flavor
```



### get_changelog

```sh
[bundle exec] fastlane get_changelog
```



----


## Android

### android write_changelog

```sh
[bundle exec] fastlane android write_changelog
```



### android get_version

```sh
[bundle exec] fastlane android get_version
```



### android upload_store

```sh
[bundle exec] fastlane android upload_store
```



### android build

```sh
[bundle exec] fastlane android build
```

Build the Android application.

### android github

```sh
[bundle exec] fastlane android github
```

Ship to Github.

### android fdroid

```sh
[bundle exec] fastlane android fdroid
```

build for fdroid.

### android beta

```sh
[bundle exec] fastlane android beta
```

Ship to Android Playstore Beta.

### android alpha

```sh
[bundle exec] fastlane android alpha
```

Ship to Android Playstore Alpha.

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
