Description
===========

This work is a fork of the facelets 1.x code base intended to provide some optimizations and fixes.
It is only useful for people stuck with JSF 1.2 and RichFaces 3.3, especially when using its AJAX features.

Problem
=======

Check this great blog post: https://blog.codecentric.de/en/2010/08/richfaces-sessions-eating-memory-analysis-of-a-memory-leak/

In short, each user session is taking megabytes, 90% of it by RichFaces' AjaxStateHolder.

Rationale
=========

* In development mode, keep as much information as possible for debugging purposes.
* In production mode, spare as much memory as possible, even if it means losing debugging information.

Usage
=====

If the value of the **facelets.DEVELOPMENT** parameter is **true**, optimizations are disabled. Otherwise, they are enabled.

Optimizations
=============

Reuse the same string literal in TagAttribute instances
-------------------------------------------------------

This alone can result in megabytes of savings depending of the complexity of your views.
In my use case, gains were between 25% to 36%.
