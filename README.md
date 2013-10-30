Description
===========

This work is a fork of the facelets 1.x code base intended to provide some optimization fixes.
It is only useful for people stuck with JSF 1.2.

Rationale
=========

* In development mode, keep as much information as possible for debugging purposes.
* In production mode, spare memory as much as possible even if it means losing debugging information.

Usage
=====

If the value of the **facelets.DEVELOPMENT** parameter is **true**, optimizations are disabled. Otherwise, they are enabled.

Optimizations
=============

# Reuse the same string literal in TagAttribute instances.
  This results in megabytes of savings
