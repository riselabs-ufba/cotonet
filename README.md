![#cotonet](https://github.com/riselabs-ufba/cotonet/blob/master/img/cotonet.png "cotonet logo")
==========

## What?
A tool to build conflict-based networks.

## Wait, what?
Cotonet is a tool that collects information form _Git_ repositories concerning branch merges' conflicts to build developer collaboration's networks.

## How?
The implementation relies on JGit to clone repositories and extract project specific informations regarding its merge scenarios. 

The networks are created in considering two different approaches based either in the entire _File_ (_i.e._, all conflicting files) or in the conflicting _Chunk_ (_i.e._, all conflicting chunks).

## Tooling
1. [**JGit**](http://www.eclipse.org/jgit/): Java implementation of Git;
2. [**Gradle**](http://gradle.org): a build tool with a focus on build automation and support for multi-language development.
3. [**MySQL**](http://www.mysql.com): open source database.

## Main Contributors:

* [**Alcemir R. Santos**](http://github.com/alcemirsantos): _Ph.D._ student at the Federal University of Bahia (UFBA).
