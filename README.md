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

## Build Steps
This project was built with [Maven](https://maven.apache.org/).
- download source code
- make sure you have installed Maven
- run `mvn install` in the root directory of the project,
    in order to install the dependencies and generate Lexer and Parser
- run `mvn package` in the root directory of the project,
    in order to generate the executable jar file

### optional
- run `mvn test` in the root directory of the project,
    in order to run the test cases

## Progress
- [x] Lexical Analysis
- [x] Syntax Analysis
- [ ] Semantic Analysis
- [ ] IR Generation
- [ ] IR Optimization
- [ ] Instruction Selection
- [ ] Instruction Scheduling
- [ ] Register Allocation
- [ ] Code Generation
- [ ] Test