# CNL
CNL ("Computable Number Language") is a programing Language 
designed to produce relatively short binary code, 
especially for algorithms computing computable numbers.<br>

## Usage:
The Code of a CNL-Program exists in two forms,
CNL-Binary and CNL-Assembly.

CNL-Assembly code is stored in .cnla Files.
Files with executable code files start
with "CNLA:" followed by the number of Arguments.
Library files start with "CNLA ".

After the File header, the files
contains commands separated by whitespaces.
Comments start with "%" and go to the end of the current line

To compile a CNL-Assembly file to CNL-Binary and 
execute it afterwards, run CNL (Main.main())
with the arguments
```
-c "[ASSEMBLY_FILE_NAME]" -x
```
alternatively run the CNL jar-file with
```
java -jar [CNL-jar] -c "[ASSEMBLY_FILE_NAME]" -x
```

## Examples:
### Hello World:
```
CNLA:0

OUT_STR "Hello World"
```
prints Hello World

### e:
```
CNLA:1
%Arg0: maximal allowed error
VAR1 1
[.? GT MULT 2 VAR1 ARG0
ADD VAR0 VAR1
DIV VAR1 INC VAR2
]
OUT_NUMBER_FLOAT_APPROX VAR0 ARG0
```
calculates e with an error of at most ARG0 and 
prints the result


## Syntax:
For a more detailed explanation on how to use the language
look at the numbered examples in the [examples folder](/examples)

The code consists of objects and operations, 
each operation takes a fixed amount of objects 
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

### Objects:
All numbers are internally treated as 
[Gaussian Rational numbers](https://en.wikipedia.org/wiki/Gaussian_rational)

The only Objects the language directly accepts
as input are positive Integers (of arbitrary size)
and Fractions of positive Integers.
To simplify working with other Objects
the compiler can translate simple Arithmetic 
expressions in a sequence of Integers, Fractions and Operations:

For instance
```
(1/2+(3/-4)i)
```
is interpreted as 
```
CMPLX (1/2) NEG (3/4)
```

### Variables:
Variables are represented by "VAR"
followed by the index of the variable (positive Integer)
i.e. VAR0, VAR1, ...
Variables are initialized with 0 by default

Program (and Function-) Arguments
follow the same syntax but with "ARG" instead of "VAR"

### Operations:
Basis Arithmetic operations:
- NEG < a >: returns -< a >
- INV < a >: returns 1/< a >
- ADD < a > < b >: return < a >+< b >
- SUBT < a > < b >: return < a >-< b >
- MULT < a > < b >: return < a >*< b >
- DIV < a > < b >: return < a >/< b >
- INT_DIV < a > < b >: 
  return q s.t. a=q*b+r, |r|<|b|
- REM < a > < b >:
  return r s.t. a=q*b+r, |r|<|b|
- CMPLX < a > < b >: return < a >+< b >*i

Comparison Operators:
- EQ < a > < b >: return < a > == < b >
- GT < a > < b >: return < a > > < b >
- GE < a > < b >: return < a > >= < b >
- NE < a > < b >: return < a > != < b >
- There are no LT or LE operations since you 
  can simply swap the arguments and use GE/GT

Basis Console IO:
- OUT_NUMBER < x >: prints < x >
- OUT_NUMBER_FLOAT  < x >: prints < x >, 
  converting fractions to floating-point numbers
- OUT_NUMBER_FIXED < x >: print < x >,
  converting fractions to fixed-point numbers
- OUT_NUMBER_FLOAT_APPROX < x > < precision >: 
  prints < x >, 
  converting fractions to floating-point numbers 
  rounded to the given precision
- OUT_NUMBER_FIXED_APPROX < x > < precision >: 
  prints < x >,
  converting fractions to fixed-point numbers
  rounded to the given precision
- OUT_STR < x >: prints < x > as String
- OUT_LN_... adds a new-line after the output



### Control flow blocks:
Control flow blocks consist of the following atoms

- '['   do</li>
- '[?'  if not zero
- '[!'  if zero
- '[.?' while not zero
- '[.!' while zero 
- '|'   else
- '|?'  else if not zero
- '|!'  else if zero 
- ']'   end
- ']?'  while not zero 
- ']!'  while zero

The three primary Blocks are:
#### If-else Block:
```
[? <condition>
<if-block>
|? <condition>
<elif-block>
|
<else-block>
]
```
#### While Block:

```
[.? <condition>
<while-block>
]
```
#### Do-While Block:

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
