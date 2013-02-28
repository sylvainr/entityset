EntitySet
=========

A close cousin of the .NET DataSet/DataTable/DataRow family, in pure Java.

The idea
--------

EntitySet supports most of the nice stuff that Microsoft built back a long time ago 
when they designed the ADO.NET's DataSet classes in order to supported what they called "disconnected architecture".

Although it surely looks uncool, it has a few applications where it excels compared to Pojo and ORMs: 
mostly when we do not care about any complex domain modeling, but mostly about loading relational data. 
No ORM also mean no "lost in translation" between the O world and the R world, which simplifies things quite a bit.

Examples
--------

TODO

Extensions
----------
- NatTable adapter
- DataAdapter-like.
