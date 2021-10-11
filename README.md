# CNL
CNL ("Computable Number Language") is a programing Language 
designed to produce relatively short bytecode, 
especially for algorithms computing computable numbers.<br>

## Usage:

CNL-Assembly code is stored in .cnla Files.
Files with executable code files start
with "CNLA:" followed by the number of Arguments.
Library files start with "CNLA ".

After the File header the files
contains commands separated by whitespaces.

To compile and run a CNL-Assembly file run CNL (Main)
with the arguments
```
-c "[ASSEMBLY_FILE_NAME]" -x "[BINARY_FILE_NAME]"
```

## Examples:
### Hello World:
```
OUT_STR "Hello World"
```
prints Hello World

### e:
```
VAR1 1
[.? GT MULT 2 VAR1 ARG0
ADD VAR0 VAR1
DIV VAR1 INC VAR2
]
OUT_NUMBER_FLOAT_APPROX VAR0 ARG0
```
calculates e with an error of at most ARG0 and 
prints the result


## Basic syntax:
The code consists of objects and operations, 
each operations takes a fixed amount of objects 
and returns a new object.
Operations are evaluated from left to right

Example of Evaluation of a line, 
bold part is evaluation stack:
<pre>
<b>ADD</b> MULT 1 2 DIV 3 4
...
<b>ADD MULT 1 2</b> DIV 3 4
<b>ADD 2</b> DIV 3 4
...
<b>ADD 2 DIV 3 4</b>
<b>ADD 2 3/4</b>
<b>11/4</b>
</pre>

The code is organized in lines, a line ends when
the current root operation gets evaluated

## Control flow:
Control flow blocks consist of the following atoms
A blocks starts with an operations containing
'[' and ends with one containing ']'

- '[' do</li>
- '[?' if not zero
- '[!' if zero
- '[.?' while not zero
- '[.!' while zero 
- '|' else
- '|?' else if not zero
- '|!' else if zero 
- ']' end
- ']?' while not zero 
- ']!' while zero 

### If-else Block:
```
[? <condition>
<if-block>
|? <condition>
<elif-block>
|
<else-block>
]
```
### While Block:

```
[.? <condition>
<while-block>
]
```
### Do-While Block:

```
[ 
<while-block>
]? <condition>
```

## Intentional behaviour that may seem weird:
- 0/0 = 0, 0<sup>0</sup>=0 (necessary for consistent behaviour of element-wise operations)
- realPart, imaginaryPart ,... do not affect the Keys of maps 
- min {} = max {} =0 
- strings are internally represented as integers 
(containing the bytes of their UTF-8 representation)
- matrices and tuples are handled as special cases of maps 
- tuples of value zero may not be displayed
- entries of value 0 in non-tuple maps are interpreted as empty and therefore ignored
- matrix multiplication automatically extends to smaller matrix with zeros to allow calculation 
- REMOVE/REMOVE_KEY do not modify the length of a tuple
