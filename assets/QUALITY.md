# How Vivid clojure-maven-plugin (CMP) is tested

_Referencing [How SQLite Is Tested](https://www.sqlite.org/testing.html)_

Reliability of CMP is achieved in part by thorough and careful automated testing.
The range of testing covers each CMP Maven goal, it's public configuration, code samples in the documentation, and assumed common execution environments (JDK and Maven versions).



## Regression testing

Defects reported against CMP cannot be considered as resolved until automated tests express the defect and prove remediation, preventing their future re-occurance.



## Release criteria

A VCS commit is considered releasable provided that all of its components satisfy the following criteria:

- Code quality assessment tools don't indicate any outstanding problems, within reason: CI build log warnings, static analysis.
- The documentation is synchronized with the code, including version numbers, and automated testing of all examples.
- The described behavior of code samples from the documentation is confirmed via automated tests.
- All automated tests pass throughout the matrix of supported versions of JDKs and Maven.
- Test coverage from automated testing indicates a near-perfect or better test coverage rate.



## Release checklist

Familiarize yourself with the entirety of this release checklist before proceeding.
It informs you of what you need to be prepared to confirm has already been completed during each phase.

### Before release
- Update [CHANGELOG.md](CHANGELOG.md) to reflect the new version.
  - Replace the ``_Unreleased_`` attribute with the actual date.
- Update project versions in code and documentation.
- Choose a specific VCS commit identifier as the release target.
- Ensure the [release criteria](QUALITY.md) are satisfied.

### Executing the release
- Deploy each component to Clojars.
- In Git, tag the release and push the tag to GitHub.
- In GitHub, change the default branch to the current release.

### Immediately after release
- Smoke test each downloadable deliverable.
- Confirm correctness of:
  - All project URLs.
  - Default branch in GitHub.
  - Versions appearing in current documentation.
- Update [CHANGELOG.md](CHANGELOG.md) to reflect the next version.
    - Note this new version as `_Unreleased_`.
