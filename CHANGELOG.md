## [1.2.5.1]
### Fixed
- Fix "'getId' is deprecated" error when installing with new version IDEA

## [1.2.5]
### Added
- Add Robot file type to File --> New
- Add Robot Run configuration
- Allow to edit run configuration 
### Fixed
- Correct run configuration name with multiple files

## [1.2.4]
### Added
- Run multiple selected files
- Run all test cases with name contain selected text
### Fixed
- Compatible IntelliJ IDEA Ultimate IU-233.8264.8 (2023.3 (eap))

## [1.2.3.1]
### Fixed
- Fix config for pabot

## [1.2.3]
### Added
- Support run with pabot
- Add run action to project view context menu
### Fixed
- Fix some small bugs

## [1.2.2]
### Added
- Add run suite line marker
- Add run for multiple profile
- Add output config
- Quick open log file: Add open in browser button in running console toolbar
- Run with tag
### Changed
- Store config at application level (**Will lose old configurations**)
### Fixed
- Fix for not show line marker for more than one test case heading
- Fix ElementAccessException

## [1.2.0.1]
### Fixed
- Fix run testcase contain special chars in name

## [1.2.0]
### Added
- Refactoring variables and keywords
- Spell check for keywords
- Add FOR, IF, TRY syntax template suggestion
### Changed
- Optimize cache & search file
### Fixed
- Fix NPE
- Fix run long keyword

## [1.1.0]
### Added
- support for Tasks
- add option to control whether you do findChildrenClass.
- add option to control when expand 2 spaces to 4 spaces when type
- add predefined variables configuration
### Changed
- optimize performance 
- space expand will be invoked for robot file only
- improve the lexical analyzer behaviour in variable table when type ${
- enable resolve variable which using extended variable syntax. e.g. ${var * 10 + 3}
- improve python lib search
### Fixed
- not regard ${123} ${-123} as variable
- solve Project Disposed exception when switch between projects
- fix bug of Library WITH NAME
- resolve exception if variable text is "${}"
- process FOR loop

## [1.0.1]
### Added
- Run using Robot Runner Plugin (if enabled)
- Add variable config 
### Changed
- Update execute command

## [0.0.2]
### Fixed
- Fix jump to file issue
- Fix incorrect find import file

## [0.0.1]
### Added
- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
