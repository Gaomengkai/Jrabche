# Jrabche
## Description
A Java implementation of the [Vrabche](https://github.com/Gaomengkai/Vrabche)

An _Vrabche_ is a compiler for Sysy language, which is a subset of C language. It can compile Sysy source code into RISC-V and ARMv7 assembly code.

The _Vrabche_ author is [GammaMilk](https://github.com/GammaMilk), 
which is a group of students from Huazhong University of Science and Technology, including
- [Gao Mengkai(Merky Gao)](https://github.com/Gaomengkai)
- [Luo Yuanze(lao-ye-zi)](https://github.com/lao-ye-zi)
- [Zhang Jiale(zzzcola)](https://github.com/zzzcola)
- [Chen Hongkun(daido1008)](https://github.com/daiduo1008)

,lead by Gao Mengkai.

## Java Implementation
This project is a Java implementation of the _Vrabche_.

At first, this project is to be submitted as a competition in 全国计算机系统能力大赛（全国赛区）.

## Dependencies
- [Clang/LLVM](https://llvm.org/)
- [ANTLR4](https://www.antlr.org/)
- [Junit5](https://junit.org/junit5/)
- [Maven](https://maven.apache.org/)

## Build Steps
This project was built with [Maven](https://maven.apache.org/).
- download source code
- make sure you have installed Maven
- run `mvn clean install -DskipTests` in the root directory of the project,
    in order to install the dependencies and generate Lexer and Parser
- run `mvn package -DskipTests` in the root directory of the project,
    in order to generate the executable jar file

### optional
- run `mvn test` in the root directory of the project,
    in order to run the test cases

## Run

This project is a command-line tool.

You can run the jar file with the following command:

```shell
java -jar target/jrabche-1.0-SNAPSHOT-jar-with-dependencies.jar [options] [source file]
```

while the options are:

- `-o [output file]`: specify the output file
- `-S [riscv|arm]`: specify the target architecture
- `-O [0-3]`: specify the optimization level
- `-h`: show the help message
- `-v`: show the verbose message

## Test
This project uses JUnit5 as the test framework.

The test cases are in the `src/test/java` directory.

If you want to run the test cases, make sure you have installed:

- `clang`
- `mingw-w64`(if windows)

Now, only the front-end test cases are available. And the cases
were all passed.

(**IMPORTANT**)
If you wanna test on your own, please modify the config 
[file](src/test/java/icu/merky/jrabche/fe/visitor/FETestConfig.java):
```
src/test/java/icu/merky/jrabche/fe/visitor/FETestConfig.java
```
## Progress
- [x] Lexical Analysis
- [x] Syntax Analysis
- [x] Semantic Analysis
- [x] IR Generation
- [x] IR Optimization
- [x] Instruction Selection
- [x] Instruction Scheduling
- [x] Register Allocation
- [x] Code Generation
- [x] Test Front-end
- [x] Test Optimizer
- [x] Test Back-end
- [x] Jar Released