What is it?
===========

It is old-school 3d shooter, indie game developed by small team.
It didn't use hi-end engine, so it looks more like wolf-3d than quake or even doom.
As side effect, it works on low-end phones pretty fast.
It has big sexy square pixels (*love*)... You should really love pixelart (like me :)

  - Code: restorer
  - Free graphics: www.nongnu.org/freedoom, www.lostgarden.com, restorer
  - Free levels: restorer
  - Sound: www.nongnu.org/freedoom

This game is released under MIT License (http://www.opensource.org/licenses/mit-license.php).

Compiling
=========

Compile debug version (apk will be in "tools" folder):

```
./z-build opensource debug
```

Compile and install debug version:

```
./z-build opensource debug install
```

Compile release version (put key.store and key.alias into ant.properties before compiling):

```
./z-build opensource release
```
