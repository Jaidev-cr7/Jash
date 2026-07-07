# Jash

> A lightweight Unix-like shell written in Java.

## Project Banner

![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Status](https://img.shields.io/badge/Status-In%20Progress-orange)
![License](https://img.shields.io/badge/License-MIT-green)

## Introduction

Jash is a shell implementation written entirely in Java to explore how command interpreters work internally. The project focuses on building a shell from the ground up, starting with a small but functional command loop and expanding toward a richer interactive environment.

The current implementation covers core interactive behavior, builtin commands, executable lookup through `PATH`, and directory navigation. The project is intentionally small in scope today and grows incrementally as new shell features are added.

## Features

- Interactive REPL
- Shell prompt (`$`)
- Builtin commands: `echo`, `pwd`, `cd`, `type`, `exit`
- External executable execution through the `PATH` environment variable
- Working directory management
- Relative and absolute directory navigation
- Home directory expansion with `~`
- Canonical path resolution
- Executable file detection
- Process execution via Java `ProcessBuilder`
- Error handling for invalid commands and missing directories

## Current Implementation

The current codebase includes the following implemented behavior:

- Interactive command loop with a visible shell prompt
- Builtin command dispatch for `echo`, `pwd`, `cd`, `type`, and `exit`
- Lookup and execution of external programs found on `PATH`
- Directory changes using relative paths, absolute paths, and `~`
- Canonical filesystem resolution for stable path handling
- Detection of executable files before launch
- Graceful error messages for unsupported commands and invalid locations

## Architecture

Jash currently relies on a small set of standard Java building blocks:

- Java File API for path and filesystem operations
- Java `ProcessBuilder` for starting external programs
- Environment variables for command resolution and shell state
- `PATH` lookup for locating executables
- A `Scanner`-based input loop for reading interactive commands
- Canonical filesystem resolution to normalize directory navigation

## Example Terminal Session

```text
$ pwd
/home/jash
$ echo Hello
Hello
$ type java
java is /usr/bin/java
$ cd ..
$ ls
README.md  Main.java
$ exit
```

## Project Structure

The repository is intentionally small at this stage:

```text
.
├── README.md
└── Main.java
```

## Installation

1. Install a recent Java runtime and compiler. Java 17 or newer is recommended.
2. Clone the repository.
3. Open a terminal in the repository root.

No additional dependencies are required beyond the standard Java toolchain.

## Running the Project

Compile and run the shell from the repository root:

```bash
javac Main.java
java Main
```

After launch, Jash starts an interactive prompt and accepts commands until you exit the session.

## Roadmap

### Core Shell

- [x] Print a prompt
- [x] Handle invalid commands
- [x] Implement a REPL
- [x] Implement exit
- [x] Implement echo
- [x] Implement type
- [x] Locate executable files
- [x] Run external programs

### Navigation

- [x] pwd builtin
- [x] cd (absolute paths)
- [x] cd (relative paths)
- [x] cd (home directory)

### Quoting

- [ ] Single quotes
- [ ] Double quotes
- [ ] Backslash escaping
- [ ] Execute quoted executables

### Redirection

- [ ] Redirect stdout
- [ ] Redirect stderr
- [ ] Append stdout
- [ ] Append stderr

### Command Completion

- [ ] Builtin completion
- [ ] Executable completion
- [ ] Partial completion
- [ ] Filename completion
- [ ] Nested completion
- [ ] Multiple matches

### Programmable Completion

- [ ] Register completions
- [ ] Unregister completions
- [ ] Completion candidates
- [ ] Longest common prefix

### Background Jobs

- [ ] jobs builtin
- [ ] Run background jobs
- [ ] Job management
- [ ] Job cleanup

### Pipelines

- [ ] Two-command pipelines
- [ ] Builtin pipelines
- [ ] Multi-command pipelines

### History

- [ ] history builtin
- [ ] Arrow navigation
- [ ] Execute previous commands

### History Persistence

- [ ] Read history file
- [ ] Write history file
- [ ] Append history
- [ ] Startup loading
- [ ] Exit saving

### Parameter Expansion

- [ ] declare builtin
- [ ] Shell variables
- [ ] Variable expansion
- [ ] Brace expansion

## Design Decisions

Jash is designed to stay small and understandable while the shell is being built out feature by feature. The implementation favors straightforward Java standard library APIs over abstractions that would hide the mechanics of command parsing, path resolution, or process execution.

A few practical choices shape the current design:

- Builtins are handled explicitly so shell behavior stays easy to trace
- External commands are resolved through `PATH` rather than hard-coded locations
- Directory handling uses canonical resolution to keep navigation consistent
- The shell stays interactive-first, with a simple REPL as the core execution model

## Learning Objectives

This project is intended to help answer questions such as:

- How does a shell read and interpret commands?
- How are builtin commands different from external executables?
- How do command lookup and process execution work together?
- How does directory navigation affect shell state?
- What additional machinery is needed for quoting, redirection, history, and completion?

## Future Improvements

Planned work will expand Jash toward a more capable shell while keeping the implementation incremental and readable. The main areas on the roadmap are quoting, redirection, completion, background jobs, pipelines, history, and parameter expansion.

As the project evolves, the goal is to add each feature carefully and keep the behavior well documented with focused examples and tests.

## License

This project is licensed under the MIT License.
