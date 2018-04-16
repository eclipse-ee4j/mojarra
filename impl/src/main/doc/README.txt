WARNING:  

 -- Do *not* edit the "*-attrs.xml" files by hand.  They should
    be recreated (by running "ant attributes") whenever a change is made to
    one of the corresponding "*-props.xml" files.

 -- If any changes are made to any of the included documents or
    to standard-html-renderkit-base.xml, call the ant
    target 'create.standard.xml' to recreate standard-html-renderkit.xml.
    The OS utility xmllint must be present on the system when the
    target is called.

    NOTE: In order for xmllint to work as expected, the system that is
    used to call the task should have libxml2 version 2.6.8 or later.
    The source for can be obtained here: ftp://xmlsoft.org/

    Check the README included with the source bundle for instructions
    on buidling and installing the library (this should work for
    win32 as well - see win32/Readme.txt for details).




