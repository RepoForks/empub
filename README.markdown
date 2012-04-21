EmPub: Embedded Digital Book Reader
===================================

EmPub can display the contents of an EPUB file, packaged with
EmPub itself in an APK.

EmPub is packaged as an Android library project, so a hosting
project can define things like error reporting, self-updating
policies, and so forth. You will find a `demo/` sub-project
that serves as a host, with some sample material serving as a book.

This project is largely undocumented at the present time, though
that will be rectified in the coming months.

Usage
-----
TBD

Dependencies
------------
This project relies upon [ActionBarSherlock](http://actionbarsherlock.com),
so you will need to adjust the EmPub library project to have a reference
to your own copy of the ActionBarSherlock library project.

This library at present requires Android 2.2 (API Level 8) or
higher. To *build* the library, you will need API Level 14, to be able
to compile ActionBarSherlock.

Version
-------
This is version v0.1 of this module, meaning that it is brand-spankin' new.

Demo
----
In the `demo/` sub-project you will find
a sample activity that demonstrates the use of EmPub.

License
-------
The code in this project is licensed under the Apache
Software License 2.0, per the terms of the included LICENSE
file.

Questions
---------
If you have questions regarding the use of this code, please post a question
on [StackOverflow](http://stackoverflow.com/questions/ask) tagged with `commonsware` and `android`.
Be sure to include source code and stack traces if you are encountering crashes.

If you have encountered what is clearly a bug, or a feature request,
please post an [issue](https://github.com/commonsguy/empub/issues).
Be certain to include complete steps for reproducing the issue.

Do not ask for help via Twitter.

Also, if you plan on hacking
on the code with an eye for contributing something back,
please open an issue that we can use for discussing
implementation details. Just lobbing a pull request over
the fence may work, but it may not.

Who Made This?
--------------
<a href="http://commonsware.com">![CommonsWare](http://commonsware.com/images/logo.png)</a>

Release Notes
-------------
- v0.1.0: initial release

