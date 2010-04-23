Setup instructions for Invisible Jacc Version 1.1


----- Directory Structure -----

Invisible Jacc is organized into three directory trees, as follows:

    Directory     Contents
    ---------     --------
    src           Source code:
                  *.java  = Java source file
                  *.jacc  = Invisible Jacc grammar specification
                  *.gen   = Invisible Jacc generated file
                  *.txt   = Sample input file
                  *.out   = Sample output file

    classes       Executable code:
                  *.class = Java class file

    doc           Documentation:
                  *.pdf   = Documentation in Adobe Acrobat format
                  *.txt   = Readme text file

You should copy the files onto your hard disk, in the appropriate locations
for your Java development environment.

Some Java development environments (for example, Borland's JBuilder) keep the
source and executable in separate directory trees.  Others (for example,
Microsoft's Visual J++) keep them mixed together in a single directory tree.
You should modify the directory trees as required for your Java system.


----- Hard-Coded Directory Paths -----

In the sample code, there are two places that have hard-coded directory paths:

    src\invisible\jacc\ex1\Ex1Input.txt
        Contains the path "invisible\jacc\ex1\Ex1Include.txt" about halfway
        through the file.

    src\invisible\jacc\ex5\Ex5Compiler.java
        Contains the path "invisible\jacc\ex5\Ex5Grammar.gen" near the start
        of the constructor.

The two paths assume that you are using Microsoft Windows, and that your
working directory is the "src" directory.  If these assumptions are not
correct, you need to modify the directory paths in order to execute the
sample code.

Note that this affects only the sample code, not Invisible Jacc itself.


----- System Requirements -----

To use Invisible Jacc, you need a Java development environment, including a
Java compiler.

Invisible Jacc version 1.1 is designed to work with any Java 1.1 compliant
compiler.  We have tested it with both Borland's JBuilder and Microsoft's
Visual J++.  But Invisible Jacc is written in standard Java 1.1, and so
you should be able to use any Java 1.1 compiler.

If you don't already have a Java development environment, we highly
recommend Borland's JBuilder.  You can download a personal copy for free
from the following address:
    http://www.borland.com


----- Reading the Documentation -----

The PDF documentation file is ijacc.pdf.  You need an Adobe Acrobat reader to
view or print the file.  If you don't have an Acrobat reader, you can
download it for free from the following address:
    http://www.adobe.com


---- Reading the License Agreement ----

The license agreement is in file LICENSE.TXT.  Be sure that you read and
agree to the license agreement before using Invisible Jacc.


----- Contacting Invisible Software -----

If you need to contact Invisible Software, you can find us at the following
addresses:
    Web site:    http://www.invisiblesoft.com/jacc
    Email:       invisoft@invisiblesoft.com
                  
