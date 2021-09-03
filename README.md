# CNL
CNL ("Computable Number Language") is a programing Language 
designed to produce relatively short bytecode, 
especially for algorithms that represent computable numbers.<br>
<br>
Intentional behaviour that may seem weird:
<li> 0/0 = 0, 0<sup>0</sup>=0 (necessary for consistent behaviour of element-wise operations)</li>
<lI> realPart, imaginaryPart ,... do not affect the Keys of maps </lI>
<li> min{} = max{} =0 </li>
<li> strings are internally represented as integers 
(containing the bytes of their UTF-8 representation)</li>
<li> matrices and tuples are handled as special cases of maps </li>
<li> entries of value 0 in maps are interpreted as empty and therefore ignored
(except for the last entry in a tuple, which is saved to allow the usage of tuples as stacks) </li>

